package com.rokid.rkengine.scheduler;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.rokid.rkengine.service.RkEngineService;
import com.rokid.rkengine.utils.CloudAppCheckConfig;
import com.rokid.rkengine.utils.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import rokid.os.IRKExecutor;
import rokid.os.RKSystemProperties;
import rokid.rkengine.IAppStateCallback;
import rokid.rkengine.IRKAppEngineAppContextChangeCallback;
import rokid.rkengine.scheduler.AppException;
import rokid.rkengine.scheduler.AppInfo;
import rokid.services.util.RemoteServiceHelper;

/* loaded from: classes.dex */
public class AppStateManager extends IAppStateCallback.Stub {
    private static final String LAUNCHER_APPID = "com.rokid.system.launcher";
    private static final String PICK_UP_SWITCH = "persist.sys.rokid.pickupswitch";
    private static final String PICK_UP_SWITCH_OFF = "close";
    private static final String PICK_UP_SWITCH_ON = "open";
    private static final String TAG = "NativeAppClientCallback";
    private ArrayList<IRKAppEngineAppContextChangeCallback> mContextChangeCallbacks = new ArrayList<>();

    public void onAppError(AppException appException) throws RemoteException {
        if (appException == null) {
            Logger.m0d("onAppError appInfo == null");
            return;
        }
        Logger.m0d(TAG, "exception with " + appException.errCode + " what " + appException.what);
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onAppError(appException);
            }
        }
    }

    public void onAppInstalledSuccess(AppInfo appInfo) throws RemoteException {
        if (appInfo == null) {
            Logger.m0d("onAppInstalledSuccess appInfo == null");
            return;
        }
        Logger.m0d(TAG, "app " + appInfo.appId + " installed");
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onAppInstalledSuccess(appInfo);
            }
        }
    }

    public void onAppUninstalledSuccess(AppInfo appInfo) throws RemoteException {
        if (appInfo == null) {
            Logger.m0d("onAppUninstalledSuccess appInfo == null");
            return;
        }
        Logger.m0d(TAG, "app " + appInfo.appId + " uninstalled");
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onAppUninstalledSuccess(appInfo);
            }
        }
    }

    public void onCreate(AppInfo appInfo) throws RemoteException {
        if (appInfo == null || TextUtils.isEmpty(appInfo.appId)) {
            Logger.m0d("onCreate appInfo is null");
            return;
        }
        Logger.m0d(TAG, "app " + appInfo.appId + " onCreate");
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onCreate(appInfo);
            }
        }
    }

    public void onPause(AppInfo appInfo) throws RemoteException {
        if (appInfo == null || TextUtils.isEmpty(appInfo.appId)) {
            Logger.m0d("onPause appInfo is null");
            return;
        }
        Logger.m0d(TAG, "app " + appInfo.appId + " onPause");
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onPause(appInfo);
            }
        }
    }

    public void onResume(AppInfo appInfo) throws RemoteException {
        if (appInfo == null || TextUtils.isEmpty(appInfo.appId)) {
            Logger.m0d("onResume appInfo is null");
            return;
        }
        Logger.m0d(TAG, "onResume  " + appInfo.appId);
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onResume(appInfo);
            }
        }
    }

    public void onStart(AppInfo appInfo) throws RemoteException {
        if (appInfo == null || TextUtils.isEmpty(appInfo.appId)) {
            Logger.m0d("onStart appInfo is null");
            return;
        }
        Logger.m0d(TAG, "onStart " + appInfo.appId);
    }

    public void onStop(AppInfo appInfo) throws RemoteException {
        AppInfo appInfoPeekApp;
        if (appInfo == null || TextUtils.isEmpty(appInfo.appId)) {
            Logger.m0d("onStop appInfo is null or appInfo.appId is empty");
            return;
        }
        Logger.m0d(TAG, "onStop " + appInfo.appId);
        CloudAppCheckConfig.removeCloudAppId(appInfo.appId);
        Iterator<IRKAppEngineAppContextChangeCallback> it = this.mContextChangeCallbacks.iterator();
        while (it.hasNext()) {
            IRKAppEngineAppContextChangeCallback next = it.next();
            if (next != null) {
                next.onStop(appInfo);
            }
        }
        if (!PICK_UP_SWITCH_ON.equals(RKSystemProperties.getProperties(PICK_UP_SWITCH))) {
            Logger.m0d(" system pickup_switch is off ");
            return;
        }
        if (!SystemStateMachine.STATE_APP_RUNNING.equals(SystemStateMachine.getInstance().getCurrentState())) {
            Logger.m0d("system not is in app_running state ");
            return;
        }
        if (CloudAppCheckConfig.isCloudApp(appInfo.appId) || LAUNCHER_APPID.equals(appInfo.appId) || appInfo.type != 2 || (appInfoPeekApp = AppStack.getInstance().peekApp()) == null) {
            return;
        }
        Logger.m0d("in app_running topApp appId : " + appInfoPeekApp.appId + " ");
        if (!appInfo.appId.equals(appInfoPeekApp.appId)) {
            Logger.m0d("topApp : " + appInfoPeekApp.appId + " not the same with appInfo " + appInfo.appId + " so don't open siren !");
            return;
        }
        if (appInfo.ignoreFromCDomain || isSmartSceneRunning()) {
            return;
        }
        openActivation();
    }

    private void openActivation() {
        Logger.m0d("openActivation ");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.rokid.activation", "com.rokid.activation.service.CoreService"));
        intent.putExtra("FromType", "pickup");
        intent.putExtra("InputAction", "confirmEvent");
        Bundle bundle = new Bundle();
        bundle.putBoolean("isConfirm", true);
        bundle.putInt("durationInMilliseconds", 6000);
        intent.putExtra("intent", bundle);
        RkEngineService.getEngineService().startService(intent);
    }

    public void setOnAppContextChangeListener(final IRKAppEngineAppContextChangeCallback iRKAppEngineAppContextChangeCallback) {
        Logger.m0d(TAG, "setOnAppStateCallbackDeathListener in AppStateManager");
        try {
            iRKAppEngineAppContextChangeCallback.asBinder().linkToDeath(new IBinder.DeathRecipient() { // from class: com.rokid.rkengine.scheduler.AppStateManager.1
                @Override // android.os.IBinder.DeathRecipient
                public void binderDied() {
                    Logger.m2i("callback " + iRKAppEngineAppContextChangeCallback.toString() + " is dead...");
                    AppStateManager.this.mContextChangeCallbacks.remove(iRKAppEngineAppContextChangeCallback);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.m2i("add callback " + iRKAppEngineAppContextChangeCallback.toString());
        this.mContextChangeCallbacks.add(iRKAppEngineAppContextChangeCallback);
    }

    private void sendPackageNameToActivation(String str) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.rokid.activation", "com.rokid.activation.service.CoreService"));
        intent.putExtra("InputAction", "onAppStackChange");
        Bundle bundle = new Bundle();
        bundle.putString("topPackageName", str);
        intent.putExtra("intent", bundle);
        RkEngineService.getEngineService().startService(intent);
    }

    public boolean isSmartSceneRunning() {
        IRKExecutor iRKExecutor = (IRKExecutor) RemoteServiceHelper.getService("rkruntime");
        if (iRKExecutor == null) {
            return false;
        }
        try {
            return iRKExecutor.isSmartSceneRunning();
        } catch (Exception e) {
            Logger.m4w("stopSmartScene exception: " + e.getCause());
            return false;
        }
    }
}
