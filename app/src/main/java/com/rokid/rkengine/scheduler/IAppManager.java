package com.rokid.rkengine.scheduler;

import android.content.Context;
import rokid.rkengine.scheduler.AppInfo;

/* loaded from: classes.dex */
public interface IAppManager {
    void bindService(Context context);

    void pauseApp(AppInfo appInfo);

    AppInfo queryAppInfoByID(String str);

    void resumeApp(AppInfo appInfo, String str);

    void setAppStateCallBack(AppStateManager appStateManager);

    void startApp(AppInfo appInfo, String str);

    void startAppWithAction(AppInfo appInfo, String str, String str2);

    void stopAllApp();

    void stopApp(AppInfo appInfo);

    void unBindService();
}
