package com.rokid.rkengine.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.rokid.rkengine.confirm.ConfirmReporter;
import com.rokid.rkengine.confirm.ConfirmRequestConfig;
import com.rokid.rkengine.confirm.ReporterManager;
import com.rokid.rkengine.parser.ParserProxy;
import com.rokid.rkengine.scheduler.AppManagerImp;
import com.rokid.rkengine.scheduler.AppStack;
import com.rokid.rkengine.scheduler.AppStarter;
import com.rokid.rkengine.scheduler.SystemStateMachine;
import com.rokid.rkengine.utils.Logger;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import rokid.rkengine.IRKAppEngine;
import rokid.rkengine.IRKAppEngineAppContextChangeCallback;
import rokid.rkengine.IRKAppEngineDomainChangeCallback;
import rokid.rkengine.scheduler.AppInfo;

/* loaded from: classes.dex */
public class RkEngineService extends Service {
    private static RkEngineService engineService;
    private AppManagerImp appManager;
    public IBinder binder = new IRKAppEngine.Stub() { // from class: com.rokid.rkengine.service.RkEngineService.1
        public void setDeviceInfo(String str) throws RemoteException {
        }

        public void clearTaskStack() throws RemoteException {
            RkEngineService.this.appManager.clearAppStack();
        }

        public AppInfo getLastAppInfo() throws RemoteException {
            return RkEngineService.this.appManager.getTopApp();
        }

        public void launch(String str, String str2, String str3) throws RemoteException {
            try {
                Logger.m0d("launch RKEngineService startParse nlp : " + str);
                Logger.m0d(" asr : " + str2);
                Logger.m0d("action : " + str3);
                ParserProxy.getInstance().startParse(str, str2, str3);
            } catch (JSONException e) {
                Logger.m1e("Launch failed with JSONException");
                e.printStackTrace();
            }
        }

        public void launchApp(String str, String str2) {
            Logger.m0d("launchApp RKEngineService start app " + str + " nlpStr: " + str2);
            new AppStarter().startNativeApp(str, str2);
        }

        public void launchLast() throws RemoteException {
            AppInfo lastApp = RkEngineService.this.appManager.getLastApp();
            if (lastApp != null) {
                RkEngineService.this.appManager.startApp(lastApp, RkEngineService.this.appManager.getNLP(lastApp.appId));
            } else {
                Logger.m0d("lastApp no exits");
            }
        }

        public void registerDomainChangeCallback(IRKAppEngineDomainChangeCallback iRKAppEngineDomainChangeCallback) throws RemoteException {
            if (iRKAppEngineDomainChangeCallback != null) {
                Logger.m0d("setOnDomainChangedListener");
                RkEngineService.this.appManager.setOnDomainChangedListener(iRKAppEngineDomainChangeCallback);
            }
        }

        public void registerAppContextChangeCallback(IRKAppEngineAppContextChangeCallback iRKAppEngineAppContextChangeCallback) throws RemoteException {
            if (iRKAppEngineAppContextChangeCallback != null) {
                Logger.m0d("setOnAppContextChangeListener");
                RkEngineService.this.appManager.setOnAppContextChangeListener(iRKAppEngineAppContextChangeCallback);
            }
        }

        public List<String> queryDomainState() throws RemoteException {
            return RkEngineService.this.appManager.queryDomainState();
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x0082  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
        */
        public boolean startConfirm(String str, String str2, List<String> list) {
            String str3 = "";
            String str4 = "";
            Logger.m0d("startConfirm confirmIntent : " + str + " confirmSlot : " + str2 + " confirmOptions : " + list);
            AppInfo appInfoPeekApp = AppStack.getInstance().peekApp();
            if (appInfoPeekApp != null) {
                String str5 = appInfoPeekApp.appId;
                Logger.m0d("appId is " + str5);
                str3 = str5;
            } else {
                str3 = "";
            }
            try {
                JSONObject jSONObject = new JSONObject(str);
                String string = jSONObject.getString("intent");
                if (jSONObject.has("slots") && jSONObject.getJSONObject("slots") != null) {
                    String string2 = jSONObject.getJSONObject("slots").toString();
                    if (!TextUtils.isEmpty(string2)) {
                        str4 = string2;
                    }
                    ReporterManager.getInstance().executeReporter(new ConfirmReporter(string, str2, list, str3, str4));
                } else {
                    str4 = "";
                    ReporterManager.getInstance().executeReporter(new ConfirmReporter(string, str2, list, str3, str4));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    };
    private EventReceiver eventReceiver;

    public static RkEngineService getEngineService() {
        return engineService;
    }

    @Override // android.app.Service
    public void onCreate() {
        engineService = this;
        this.appManager = AppManagerImp.getInstance();
        this.appManager.bindService(this);
        this.eventReceiver = new EventReceiver(this);
        ConfirmRequestConfig.initDeviceInfo();
        SystemStateMachine.getInstance().initStateListener(this);
        super.onCreate();
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.eventReceiver);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int i, int i2) {
        if (intent == null) {
            Logger.m2i("onStartCommand with invalid intent");
            return super.onStartCommand(intent, i, i2);
        }
        return super.onStartCommand(intent, i, i2);
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public void openSiren(String str, boolean z, int i) {
        Logger.m0d(" process openSiren ");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.rokid.activation", "com.rokid.activation.service.CoreService"));
        intent.putExtra("FromType", str);
        intent.putExtra("InputAction", "confirmEvent");
        Bundle bundle = new Bundle();
        bundle.putBoolean("isConfirm", z);
        bundle.putInt("durationInMilliseconds", i);
        intent.putExtra("intent", bundle);
        startService(intent);
    }
}
