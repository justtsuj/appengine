package com.rokid.rkengine.scheduler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import com.rokid.rkengine.utils.Logger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import rokid.os.IRKExecutor;
import rokid.rkengine.IAppManagerProxy;
import rokid.rkengine.IRKAppEngineAppContextChangeCallback;
import rokid.rkengine.IRKAppEngineDomainChangeCallback;
import rokid.rkengine.scheduler.AppInfo;
import rokid.services.util.RemoteServiceHelper;

/* loaded from: classes.dex */
public class AppManagerImp implements IAppManager {
    private static final String PACKAGE_NAME = "com.rokid.runtime";
    private static final String SERVICE_NAME = "com.rokid.runtime.openvoice.RKNativeAppClientService";
    private IAppManagerProxy appManagerProxy;
    private AppStack appStack;
    private AppStateManager appStateManager;
    private ServiceConnection conn;
    private Context context;
    private Map<String, String> nlpMaps;

    private AppManagerImp() {
        this.appStateManager = new AppStateManager();
        this.appStack = AppStack.getInstance();
        this.nlpMaps = new ConcurrentHashMap();
        this.conn = new ServiceConnection() { // from class: com.rokid.rkengine.scheduler.AppManagerImp.1
            @Override // android.content.ServiceConnection
            public void onServiceDisconnected(ComponentName componentName) {
            }

            @Override // android.content.ServiceConnection
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Logger.m0d("onServiceConnected ");
                AppManagerImp.this.appManagerProxy = IAppManagerProxy.Stub.asInterface(iBinder);
                AppManagerImp appManagerImp = AppManagerImp.this;
                appManagerImp.setAppStateCallBack(appManagerImp.appStateManager);
            }
        };
    }

    public static AppManagerImp getInstance() {
        return SingleHolder.instance;
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void bindService(Context context) {
        this.context = context;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
        Logger.m0d("isBind RemoteService " + context.bindService(intent, this.conn, 1));
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void unBindService() {
        Context context = this.context;
        if (context == null) {
            return;
        }
        context.unbindService(this.conn);
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void setAppStateCallBack(AppStateManager appStateManager) {
        if (this.appManagerProxy == null) {
            return;
        }
        Logger.m0d("appManager setAppStateCallBack");
        try {
            this.appManagerProxy.setAppStateCallBack(appStateManager);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public AppInfo queryAppInfoByID(String str) {
        if (this.appManagerProxy == null) {
            return null;
        }
        Logger.m0d("appManager queryAppInfo appId " + str);
        try {
            return this.appManagerProxy.queryAppInfoByID(str);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void startApp(AppInfo appInfo, String str) {
        Logger.m0d("appManager startApp appInfo : " + appInfo.appId + " extra : " + str);
        IAppManagerProxy iAppManagerProxy = this.appManagerProxy;
        if (iAppManagerProxy == null) {
            return;
        }
        try {
            iAppManagerProxy.startApp(appInfo, str);
            Logger.m0d("appManagerProxy.startApp");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void startAppWithAction(AppInfo appInfo, String str, String str2) {
        Logger.m0d("appManager startAppWithAction appInfo : " + appInfo.appId + " extra - nlp: " + str + ", action: " + str2);
        IAppManagerProxy iAppManagerProxy = this.appManagerProxy;
        if (iAppManagerProxy == null) {
            return;
        }
        try {
            iAppManagerProxy.startAppWithAction(appInfo, str, str2);
            Logger.m0d("appManagerProxy.startAppWithAction");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void pauseApp(AppInfo appInfo) {
        IAppManagerProxy iAppManagerProxy = this.appManagerProxy;
        if (iAppManagerProxy == null) {
            return;
        }
        try {
            iAppManagerProxy.pauseApp(appInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void resumeApp(AppInfo appInfo, String str) {
        IAppManagerProxy iAppManagerProxy = this.appManagerProxy;
        if (iAppManagerProxy == null) {
            return;
        }
        try {
            iAppManagerProxy.resumeApp(appInfo, str);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void stopApp(AppInfo appInfo) {
        if (this.appManagerProxy == null) {
            return;
        }
        if (appInfo == null) {
            Logger.m0d("appInfo is null !");
        } else {
            new AppStarter().startLauncher();
            AppStack.getInstance().exitSessionDomain(appInfo.appId);
        }
    }

    @Override // com.rokid.rkengine.scheduler.IAppManager
    public void stopAllApp() {
        if (this.appManagerProxy == null) {
            return;
        }
        new AppStarter().startLauncher();
        if (AppStack.getInstance().getAppStack().isEmpty()) {
            Logger.m0d(" stack is empty !");
        } else {
            SystemStateMachine.getInstance().setCurrentState(SystemStateMachine.STATE_DORMANCY);
            AppStack.getInstance().clearAppStack();
        }
    }

    public int getAppNum() {
        return this.appStack.getAppNum();
    }

    public AppInfo getTopApp() {
        return this.appStack.peekApp();
    }

    public AppInfo getLastApp() {
        return this.appStack.getLastApp();
    }

    public void clearAppStack() {
        this.appStack.clearAppStack();
    }

    public void storeNLP(String str, String str2) {
        if (TextUtils.isEmpty(str)) {
            Logger.m0d("key is null !");
        } else {
            this.nlpMaps.put(str, str2);
        }
    }

    public String getNLP(String str) {
        return this.nlpMaps.get(str);
    }

    public void setOnDomainChangedListener(IRKAppEngineDomainChangeCallback iRKAppEngineDomainChangeCallback) {
        this.appStack.setOnDomainChangedListener(iRKAppEngineDomainChangeCallback);
    }

    private static class SingleHolder {
        private static final AppManagerImp instance = new AppManagerImp();

        private SingleHolder() {
        }
    }

    public void setOnAppContextChangeListener(IRKAppEngineAppContextChangeCallback iRKAppEngineAppContextChangeCallback) {
        AppStateManager appStateManager = this.appStateManager;
        if (appStateManager != null) {
            appStateManager.setOnAppContextChangeListener(iRKAppEngineAppContextChangeCallback);
        }
    }

    public List<String> queryDomainState() {
        return this.appStack.queryDomainState();
    }

    public void checkForSmartScene(AppInfo appInfo, String str) {
        IRKExecutor iRKExecutor = (IRKExecutor) RemoteServiceHelper.getService("rkruntime");
        if (iRKExecutor == null) {
            return;
        }
        try {
            if (iRKExecutor.isSmartSceneRunning()) {
                String smartSceneRunningAppId = iRKExecutor.getSmartSceneRunningAppId();
                Logger.m2i("checkForSmartScene smartSceneRunningAppId: " + smartSceneRunningAppId);
                Logger.m2i("checkForSmartScene realAppId: " + str);
                if (!str.equals(smartSceneRunningAppId)) {
                    if (2 == appInfo.type) {
                        iRKExecutor.pauseSmartScene();
                    } else if (1 == appInfo.type) {
                        iRKExecutor.stopSmartScene();
                    }
                } else {
                    iRKExecutor.resumeSmartScene();
                }
            }
        } catch (Exception e) {
            Logger.m4w("checkForSmartScene exception: " + e.getCause());
        }
    }

    public void stopSmartScene() {
        IRKExecutor iRKExecutor = (IRKExecutor) RemoteServiceHelper.getService("rkruntime");
        if (iRKExecutor == null) {
            return;
        }
        try {
            iRKExecutor.stopSmartScene();
        } catch (Exception e) {
            Logger.m4w("stopSmartScene exception: " + e.getCause());
        }
    }
}
