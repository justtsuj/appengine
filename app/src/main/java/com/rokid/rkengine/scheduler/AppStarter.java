package com.rokid.rkengine.scheduler;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import com.rokid.rkengine.service.RkEngineService;
import com.rokid.rkengine.utils.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import rokid.rkengine.scheduler.AppInfo;

/* loaded from: classes.dex */
public class AppStarter {
    private AppManagerImp appManager = AppManagerImp.getInstance();

    public void startCloudApp(String str, String str2, String str3) {
        Logger.m0d(" startCloudApp appId:" + str + " extra: " + str2);
        if (TextUtils.isEmpty(str)) {
            Logger.m0d(" appId is null!");
            return;
        }
        AppInfo appInfoQueryAppInfoByID = this.appManager.queryAppInfoByID(str);
        if (appInfoQueryAppInfoByID == null) {
            Logger.m0d(" appInfo is null!");
            return;
        }
        if (!checkCloudActionDirectives(str2)) {
            Logger.m0d(" cloud action directives is null,do not need send it");
            return;
        }
        this.appManager.checkForSmartScene(appInfoQueryAppInfoByID, str3);
        this.appManager.startApp(appInfoQueryAppInfoByID, str2);
        this.appManager.storeNLP(appInfoQueryAppInfoByID.appId, str2);
        SystemStateMachine.getInstance().setCurrentState(SystemStateMachine.STATE_APP_RUNNING);
        AppStack.getInstance().pushApp(appInfoQueryAppInfoByID);
    }

    public void startCloudService(String str, String str2, String str3) {
        Logger.m0d(" startCloudService appId:" + str + " extra: " + str2);
        if (TextUtils.isEmpty(str)) {
            Logger.m0d(" appId is null!");
            return;
        }
        AppInfo appInfoQueryAppInfoByID = this.appManager.queryAppInfoByID(str);
        if (appInfoQueryAppInfoByID == null) {
            Logger.m0d(" appInfo is null!");
            return;
        }
        if (!checkCloudActionDirectives(str2)) {
            Logger.m0d(" cloud action directives is null,do not need send it");
            return;
        }
        this.appManager.checkForSmartScene(appInfoQueryAppInfoByID, str3);
        Intent intent = new Intent();
        intent.putExtra("nlp", str2);
        intent.putExtra("activation", false);
        intent.putExtra("event", 233);
        intent.putExtra("resume", false);
        intent.putExtra("pickup", false);
        intent.addFlags(805306368);
        intent.setComponent(new ComponentName("com.rokid.cloudappclient", "com.rokid.cloudappclient.activity.CloudCutActivity"));
        RkEngineService.getEngineService().startActivity(intent);
        SystemStateMachine.getInstance().setCurrentState(SystemStateMachine.STATE_APP_RUNNING);
    }

    public void startNativeApp(String str, String str2) {
        Logger.m0d(" startNativeApp appId:" + str + " extra: " + str2);
        if (TextUtils.isEmpty(str)) {
            Logger.m0d("native appId is null!");
            return;
        }
        AppInfo appInfoQueryAppInfoByID = this.appManager.queryAppInfoByID(str);
        if (appInfoQueryAppInfoByID == null) {
            Logger.m0d("native appInfo is null!");
            AppInfo appInfo = new AppInfo();
            appInfo.type = 1;
            this.appManager.checkForSmartScene(appInfo, str);
            return;
        }
        Logger.m0d("app type : " + appInfoQueryAppInfoByID.type);
        this.appManager.checkForSmartScene(appInfoQueryAppInfoByID, str);
        this.appManager.startApp(appInfoQueryAppInfoByID, str2);
        this.appManager.storeNLP(appInfoQueryAppInfoByID.appId, str2);
        SystemStateMachine.getInstance().setCurrentState(SystemStateMachine.STATE_APP_RUNNING);
        if (3 != appInfoQueryAppInfoByID.type) {
            AppStack.getInstance().pushApp(appInfoQueryAppInfoByID);
        }
    }

    public void startNativeAppWithAction(String str, String str2, String str3) {
        Logger.m0d(" startNativeApp appId:" + str + " extra - nlp: " + str2 + ", action: " + str3);
        if (TextUtils.isEmpty(str)) {
            Logger.m0d("native appId is null!");
            return;
        }
        AppInfo appInfoQueryAppInfoByID = this.appManager.queryAppInfoByID(str);
        if (appInfoQueryAppInfoByID == null) {
            Logger.m0d("native appInfo is null!");
            AppInfo appInfo = new AppInfo();
            appInfo.type = 1;
            this.appManager.checkForSmartScene(appInfo, str);
            return;
        }
        Logger.m0d("app type : " + appInfoQueryAppInfoByID.type);
        this.appManager.checkForSmartScene(appInfoQueryAppInfoByID, str);
        this.appManager.startAppWithAction(appInfoQueryAppInfoByID, str2, str3);
        this.appManager.storeNLP(appInfoQueryAppInfoByID.appId, str2);
        SystemStateMachine.getInstance().setCurrentState(SystemStateMachine.STATE_APP_RUNNING);
        if (3 != appInfoQueryAppInfoByID.type) {
            AppStack.getInstance().pushApp(appInfoQueryAppInfoByID);
        }
    }

    public void startLauncher() {
        Logger.m0d("start Launcher");
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(335544320);
        RkEngineService.getEngineService().startActivity(intent);
    }

    private boolean checkCloudActionDirectives(String str) {
        try {
            JSONArray jSONArray = new JSONObject(str).getJSONObject("response").getJSONObject("action").getJSONArray("directives");
            if (jSONArray != null) {
                return jSONArray.length() > 0;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
