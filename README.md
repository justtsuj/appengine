通过修改rokid音箱的核心应用rkengine，使之在原有功能基础上实现接入大模型，接入HA，听歌和查天气。

[视频演示【复活若琪】](https://www.bilibili.com/video/BV1rov6BuEta/?share_source=copy_web&vd_source=354b501bae3c07397047f2b3b75a3d53)

[工作原理](docs/工作原理.md)

# 安装

## 准备配置文件
1. 申请智谱key，[https://docs.bigmodel.cn/cn/guide/start/quick-start](https://docs.bigmodel.cn/cn/guide/start/quick-start)
2. 申请和风天气key，[https://dev.qweather.com/docs/start/](https://dev.qweather.com/docs/start/)
3. 申请HA key，[https://developers.home-assistant.io/docs/auth_api/#long-lived-access-token](https://developers.home-assistant.io/docs/auth_api/#long-lived-access-token)和[https://www.home-assistant.io/docs/authentication/](https://www.home-assistant.io/docs/authentication/)
4. 部署xiaomusic，`docker push danmuuu/xiaomusic:arm64-latest`，创建容器的参数参考原仓库[https://github.com/hanxi/xiaomusic](https://github.com/hanxi/xiaomusic)。

将上述配置添到配置文件对应的配置项中。四个模块需要哪个模块就配置，不需要就留空，互相之间不影响。

```properties
# Hook的app ids，程序内置了一些基础的app ids，所以这部分不用填，供后续功能拓展用
# 若琪技能的app id，如果有多个用逗号分割
# chat-app.ids=
# weather-app.ids=
# smarthome-app.ids=
# music-app.ids=
# 智谱服务需要的配置
# zhipu.token=
# zhipu.system.prompt=
# zhipu.model=glm-4.6
# 和风天气需要的配置
# qweather.url=
# qweather.token=
# homeassistant需要的配置
# ha.url=
# ha.token=
# xiaomusic需要的配置
# xiaomusic.url=
```

## 推送文件到若琪

1. 下载[adb](https://developer.android.google.cn/tools/releases/platform-tools?hl=ca)并加入PATH环境变量。

2. 下载仓库根目录下的`push_file_to_rokid.ps1`脚本到本地并执行。

`PS> .\push_file_to_rokid.ps1 -ConfigPath [配置文件路径] -ApkPath https://github.com/justtsuj/appengine/releases/latest/app-release.apk -Devices [若琪ip]:5555`

执行上面的命令需要若琪和执行命令的电脑在同一局域网下。如果你使用usb线连接电脑和若琪时，可以不用指定设备（默认使用第一个设备）。

`PS> .\push_file_to_rokid.ps1 -ConfigPath [配置文件路径] -ApkPath https://github.com/justtsuj/appengine/releases/latest/app-release.apk`

# 功能实现
基础能力使用rokid本身的stt，tts和意图识别

## 核心功能
1. **和大模型聊天**使用智谱ai，默认使用glm-4.6，没有调用任何工具，没有开思考
2. **查天气**使用和风天气，可以查今天，明天，后天三天的天气
3. **接入HA**控制家里的设备，单纯把识别的语音指令交给HA的Conversion接口处理并接受响应。
4. **听音乐**使用[xiaomusic](https://github.com/hanxi/xiaomusic)，在原仓库基础上增加了searchmusicinfo和randommusic接口供rokid使用，可以实现播放指定歌曲和随机放一首歌曲。

# 贡献
目前只是基础版本，功能比较简陋，没有经过充分测试。如果你有好的意见和建议欢迎pr。

# 感谢
[xiaomusic](https://github.com/hanxi/xiaomusic)

