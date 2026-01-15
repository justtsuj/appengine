<#
.SYNOPSIS
推送文件到设备

.DESCRIPTION
该脚本用于将APK文件和配置文件推送到Android设备，支持通过配置文件、指定APK路径和设备地址进行操作。

.PARAMETER ConfigPath
配置文件的路径

.PARAMETER ApkPath
APK文件的路径

.PARAMETER Device
设备地址（格式：ip:port，flag参数，可选）
#>

param(
    [Parameter(Mandatory=$false)]
    [string]$ConfigPath,
    
    [Parameter(Mandatory=$false)]
    [string]$ApkPath,
    
    [Parameter(Mandatory=$false)]
    [string]$Device
)


# 远程设备上的文件路径
$RemoteAPKDir = '/system/app/appengine'
$RemoteAPKPath = '/system/app/appengine/appengine.apk'
$RemoteConfigDir = '/sdcard/Android/data/com.rokid.rkengine/files'
$RemoteConfigPath = '/sdcard/Android/data/com.rokid.rkengine/files/config.properties'

# 失败函数
function Fail([string]$msg) {
    Write-Host "[ERROR] $msg" -ForegroundColor Red
    throw $msg
}

# 临时文件路径（用于存储从URL下载的文件）
$tempApkPath = $null

# 定义清理临时文件的函数
function Remove-TempFiles {
    if (-not [string]::IsNullOrWhiteSpace($tempApkPath) -and (Test-Path $tempApkPath)) {
        Write-Host "正在清理临时文件..." -ForegroundColor Yellow
        try {
            Remove-Item -Path $tempApkPath -Force
            Write-Host "临时文件已清理： $tempApkPath" -ForegroundColor Green
        } catch {
            Write-Warning "清理临时文件失败: $_"
        }
    }
}

# 使用try-catch-finally确保临时文件总是被清理
try {
    # 首先检查是否至少提供了一个文件路径
    if ([string]::IsNullOrWhiteSpace($ConfigPath) -and [string]::IsNullOrWhiteSpace($ApkPath)) {
        Fail "请至少提供一个文件路径（配置文件或APK文件）"
    }
    
    # 处理配置文件路径
    if (-not [string]::IsNullOrWhiteSpace($ConfigPath)) {
        $pathInfo = Resolve-Path -Path $ConfigPath -ErrorAction SilentlyContinue
        if (-not $pathInfo) {
            Fail "本地配置文件不存在或路径错误"
        }
        $ConfigPath = $pathInfo.Path
        Write-Host "本地配置文件路径： $ConfigPath" -ForegroundColor Green
    }
    
    # 处理APK文件路径
    if (-not [string]::IsNullOrWhiteSpace($ApkPath)) {
        # 检查ApkPath是否为URL
        $isUrl = $ApkPath -match '^(https)://[^\s/$.?#].[^\s]*$'
        
        if ($isUrl) {
            Write-Host "检测到APK文件URL： $ApkPath" -ForegroundColor Green
            
            # 创建临时文件路径
            $tempDir = [System.IO.Path]::GetTempPath()
            $tempFileName = [System.IO.Path]::GetRandomFileName() + ".apk"
            $tempApkPath = Join-Path -Path $tempDir -ChildPath $tempFileName
            
            # 下载文件
            Write-Host "正在从URL下载APK文件..." -ForegroundColor Yellow
            try {
                Invoke-WebRequest -Uri $ApkPath -OutFile $tempApkPath -ErrorAction Stop
                Write-Host "APK文件下载成功，保存到临时位置： $tempApkPath" -ForegroundColor Green
                $ApkPath = $tempApkPath
            } catch {
                Fail "从URL下载APK文件失败: $_"
            }
        } else {
            # 处理本地文件路径
            $pathInfo = Resolve-Path -Path $ApkPath -ErrorAction SilentlyContinue
            if (-not $pathInfo) {
                Fail "本地APK文件不存在或路径错误"
            }
            $ApkPath = $pathInfo.Path
            Write-Host "本地APK文件路径： $ApkPath" -ForegroundColor Green
        }
    }

    # 检查adb是否可用
    if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
        Fail "找不到 adb，请确认 adb 已安装且在 PATH 中。"
    }

    # 获取设备列表
    $devicesRaw = & adb devices
    if ($LASTEXITCODE -ne 0) {
        Fail "执行 'adb devices' 失败，检查 adb 连接与驱动。"
    }

    # 解析设备列表
    $devices = @()
    $lines = $devicesRaw -split "`n"
    foreach ($l in $lines) {
        $l = $l.Trim()
        if ($l -and -not $l.StartsWith("List of devices")) {
            $parts = $l -split "\s+"
            if ($parts.Count -ge 2 -and $parts[1] -eq "device") {
                $devices += $parts[0]
            }
        }
    }

    # 处理设备地址参数
    $deviceSerial = $null

    # 优先使用用户指定的设备地址
    if (-not [string]::IsNullOrWhiteSpace($Device)) {
        # 检查指定设备是否在线
        $deviceOnline = $devices | Where-Object { $_ -eq $Device }
        if ($deviceOnline) {
            $deviceSerial = $Device
            Write-Host "使用指定设备： $deviceSerial" -ForegroundColor Green
        } else {
            # 尝试连接指定设备
            Write-Host "尝试连接指定设备： $Device" -ForegroundColor Yellow
            $connectOutput = & adb connect $Device
            if ($connectOutput -match 'connected') {
                $deviceSerial = $Device
                Write-Host "设备连接成功： $deviceSerial" -ForegroundColor Green
            } else {
                Fail "无法连接到指定设备： $Device"
            }
        }
    } else {
        # 处理设备数量情况
        if ($devices.Count -eq 0) {
            Fail "未检测到在线设备。请通过 USB 或网络连接设备并启用调试（USB Debugging）。"
        }

        if ($devices.Count -gt 1) {
            Write-Host '[WARN] 检测到多个设备：' -ForegroundColor Yellow
            $devices | ForEach-Object { Write-Host "  $_" }
            Write-Host "默认将使用第一个设备： $($devices[0])" -ForegroundColor Yellow
        }

        $deviceSerial = $devices[0]
    }

    # 显示操作信息
    Write-Host "准备推送文件到设备： $deviceSerial" -ForegroundColor Green

    # 标记是否需要root权限（只有推送APK到/system分区时才需要）
    $needRoot = -not [string]::IsNullOrWhiteSpace($ApkPath)

    # 如果需要root权限（推送APK）
    if ($needRoot) {
        # 获取root权限
        Write-Host "正在获取root权限..."
        & adb -s $deviceSerial root
        if ($LASTEXITCODE -ne 0) {
            Fail "获取 root 用户权限失败。"
        }
        Write-Host "设备已root。" -ForegroundColor Green
        
        # 重新挂载/system分区（只在推送APK时需要）
        Write-Host "正在重新挂载/system分区..."
        & adb -s $deviceSerial shell "mount -o remount,rw /system"
        if ($LASTEXITCODE -ne 0) {
            Fail "重新挂载/system分区失败。"
        }
        Write-Host "重新挂载/system分区成功。" -ForegroundColor Green
    }

    # 推送配置文件（如果提供了）
    if (-not [string]::IsNullOrWhiteSpace($ConfigPath)) {
        Write-Host "开始推送配置文件..."
        Write-Host "  本地： $ConfigPath"
        Write-Host "  设备： $RemoteConfigPath"
        
        # 检查配置文件的远程目录是否存在
        $checkConfigDirCmd = @"
if [ -d "$RemoteConfigDir" ]; then echo "exists"; else mkdir -p "$RemoteConfigDir" && echo "created"; fi
"@
        
        Write-Host "正在检查/创建配置文件的远程目录..."
        $configDirStatus = & adb -s $deviceSerial shell $checkConfigDirCmd
        
        if ($configDirStatus -like "*exists*" -or $configDirStatus -like "*created*") {
            Write-Host "配置文件的远程目录已准备好： $RemoteConfigDir" -ForegroundColor Green
            
            # 推送配置文件
            & adb -s $deviceSerial push $ConfigPath $RemoteConfigPath
            if ($LASTEXITCODE -ne 0) {
                Fail "推送配置文件失败。"
            }
            Write-Host '[OK] 配置文件推送完成。' -ForegroundColor Green
        } else {
            Fail "$configDirStatus\r\n无法创建配置文件的远程目录: $RemoteConfigDir"
        }
    }

    # 推送APK文件（如果提供了）
    if (-not [string]::IsNullOrWhiteSpace($ApkPath)) {
        Write-Host "开始推送APK文件..."
        Write-Host "  本地： $ApkPath"
        Write-Host "  设备： $RemoteAPKPath"
        
        # 检查APK文件的远程目录是否存在
        $checkApkDirCmd = @"
if [ -d "$RemoteAPKDir" ]; then echo "exists"; else echo "not_exists"; fi
"@
        
        Write-Host "正在检查APK文件的远程目录是否存在..."
        $apkDirExists = & adb -s $deviceSerial shell $checkApkDirCmd
        
        if ($apkDirExists -like "*exists*") {
            Write-Host "APK文件的远程目录存在： $RemoteAPKDir" -ForegroundColor Green
			
			# 检查是否存在备份，没有则把当前文件备份一下
            $checkBackupCmd = @"
if [ -f "$RemoteAPKPath.bak" ]; then echo "backup_exists"; else echo "backup_not_exists"; fi
"@

            Write-Host "正在检查是否存在备份文件..."
            $backupStatus = & adb -s $deviceSerial shell $checkBackupCmd
            if ($backupStatus -like "*backup_not_exists*") {
                Write-Host "没有备份文件，将备份当前文件..."
                & adb -s $deviceSerial shell "cp $RemoteAPKPath $RemoteAPKPath.bak"
            } else {
                Write-Host "备份文件已存在，将直接覆盖..."
            }
            
            # 推送APK文件
            & adb -s $deviceSerial push $ApkPath $RemoteAPKPath
            if ($LASTEXITCODE -ne 0) {
                Fail "推送APK文件失败。"
            }
            Write-Host '[OK] APK文件推送完成。' -ForegroundColor Green

        } else {
            Fail "APK文件的远程目录不存在: $RemoteAPKDir"
        }
    }

    Write-Host "所有文件推送操作完成。" -ForegroundColor Green

    # 重启设备
    Write-Host "正在重启设备..."
    & adb -s $deviceSerial reboot
    if ($LASTEXITCODE -ne 0) {
        Fail "重启设备失败。"
    }
    Write-Host "设备已重启。" -ForegroundColor Green
} catch {
    # 捕获Fail函数抛出的异常
    exit 1
} finally {
    # 确保临时文件总是被清理
    Remove-TempFiles
}