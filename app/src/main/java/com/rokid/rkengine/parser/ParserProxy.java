package com.rokid.rkengine.parser;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.rokid.rkengine.bean.NLPBean;
import com.rokid.rkengine.bean.SlotItem;
import com.rokid.rkengine.cloud.CustomCloudService;
import com.rokid.rkengine.scheduler.AppManagerImp;
import com.rokid.rkengine.scheduler.AppStarter;
import com.rokid.rkengine.utils.CloudAppCheckConfig;
import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/* loaded from: classes.dex */
public class ParserProxy {
    private static final String FORM_CUT = "cut";
    private static final String FORM_SCENE = "scene";
    public static final String INTENT_CLEAR = "ROKID.SYSTEM.EXIT";
    public static final String INTENT_EXECUTE = "ROKID.INTENT.EXECUTE";
    public static final String INTENT_EXIT = "ROKID.INTENT.EXIT";
    private static final String SERVICE = "service";
    private AppStarter appStarter;

    public static ParserProxy getInstance() {
        return SingleHolder.instance;
    }

    /* JADX WARN: Removed duplicated region for block: B:107:0x0201  */
    /* JADX WARN: Removed duplicated region for block: B:81:0x018a  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public void startParse(String nlpStr, String asrStr, String actionStr) throws JSONException {
        JSONObject nlpJson;
        String appId;
        boolean isCloud;
        String string;
        JSONObject actionJson;
        JSONObject responseJson;
        JSONObject responseActionJson;
        // if (TextUtils.isEmpty(nlpStr) || TextUtils.isEmpty(actionStr)) {
        if (TextUtils.isEmpty(nlpStr)) {
            Logger.m1e("str empty error!! action:" + actionStr + " nlp: " + nlpStr + " asr: " + asrStr);
            return;
        }
        Logger.m0d("action  ---> " + actionStr);
        Logger.m0d("nlp -------> " + nlpStr);
        Logger.m0d("asr -------> " + asrStr);
        String string2 = null;
        try {
            nlpJson = new JSONObject(nlpStr);
        } catch (JSONException e) {
            Logger.m1e(" JSONParseException : parse nlp error !");
            e.printStackTrace();
            nlpJson = null;
        }
        if (nlpJson == null) {
            Logger.m0d("nlp is null!");
            return;
        }
        try {
            appId = (String) nlpJson.get("appId");
        } catch (JSONException e2) {
            e2.printStackTrace();
            appId = null;
        }
        if (TextUtils.isEmpty(appId)) {
            Logger.m0d(" appId is null !");
            return;
        }
        try {
            isCloud = ((Boolean) nlpJson.get("cloud")).booleanValue();
        } catch (JSONException e3) {
            e3.printStackTrace();
            isCloud = false;
        }
        try {
            string = nlpJson.getString("intent");
        } catch (JSONException e4) {
            e4.printStackTrace();
            string = null;
        }
        if (INTENT_CLEAR.equals(string)) {
            Logger.m0d("exit all app !");
            AppManagerImp.getInstance().stopSmartScene();
            AppManagerImp.getInstance().stopAllApp();
            return;
        }
        if (isCloud) {
            try {
                actionJson = new JSONObject(actionStr);
            } catch (JSONException e5) {
                Logger.m1e(" JSONParseException : parse action error !");
                e5.printStackTrace();
                actionJson = null;
            }
            try {
                CustomCloudService.getInstance().HookAction(nlpJson, actionJson);
            } catch (IllegalArgumentException e) {
                Logger.m1e("CustomCloudService初始化失败");
            }
            if (actionJson == null) {
                Logger.m0d("actionObject is null !");
                return;
            }
            try {
                responseJson = (JSONObject) actionJson.get("response");
            } catch (JSONException e6) {
                e6.printStackTrace();
                responseJson = null;
            }
            if (responseJson == null) {
                Logger.m0d(" responseObj is null !");
                return;
            }
            try {
                responseActionJson = (JSONObject) responseJson.get("action");
            } catch (JSONException e7) {
                e7.printStackTrace();
                responseActionJson = null;
            }
            if (responseActionJson == null) {
                Logger.m0d(" actionObj is null !");
                return;
            }
            try {
                string2 = responseActionJson.getString("form");
            } catch (JSONException e8) {
                e8.printStackTrace();
            }
            if (TextUtils.isEmpty(string2)) {
                Logger.m0d("form is null !");
                return;
            }
            actionStr = actionJson.toString();
            char c = 2;
            if (INTENT_EXIT.equals(string)) {
                Logger.m0d(" exit app !");
                int iHashCode = string2.hashCode();
                if (iHashCode != 98882) {
                    if (iHashCode != 109254796) {
                        if (iHashCode != 1984153269 || !string2.equals(SERVICE)) {
                            c = 65535;
                        }
                    } else if (string2.equals(FORM_SCENE)) {
                        c = 0;
                    }
                } else if (string2.equals(FORM_CUT)) {
                    c = 1;
                }
                switch (c) {
                    case 0:
                        appId = CloudAppCheckConfig.CLOUD_SCENE_APP_PACKAGE_NAME;
                        break;
                    case 1:
                        appId = CloudAppCheckConfig.CLOUD_CUT_APP_PACKAGE_NAME;
                        break;
                    case 2:
                        try {
                            nlpJson.get("slots");
                        } catch (JSONException e9) {
                            e9.printStackTrace();
                        }
                        Logger.m1e("slots is null !");
                        appId = CloudAppCheckConfig.CLOUD_CUT_APP_PACKAGE_NAME;
                        break;
                    default:
                        Logger.m0d("unknow form :  " + string2);
                        break;
                }
                AppManagerImp.getInstance().stopApp(AppManagerImp.getInstance().queryAppInfoByID(appId));
                return;
            }
            this.appStarter = new AppStarter();
            int iHashCode2 = string2.hashCode();
            if (iHashCode2 != 98882) {
                if (iHashCode2 != 109254796) {
                    if (iHashCode2 != 1984153269 || !string2.equals(SERVICE)) {
                        c = 65535;
                    }
                } else if (string2.equals(FORM_SCENE)) {
                    c = 0;
                }
            } else if (string2.equals(FORM_CUT)) {
                c = 1;
            }
            switch (c) {
                case 0:
                    CloudAppCheckConfig.storeCloudAppId(CloudAppCheckConfig.CLOUD_SCENE_APP_PACKAGE_NAME, appId);
                    this.appStarter.startCloudApp(CloudAppCheckConfig.CLOUD_SCENE_APP_PACKAGE_NAME, actionStr, appId);
                    break;
                case 1:
                    CloudAppCheckConfig.storeCloudAppId(CloudAppCheckConfig.CLOUD_CUT_APP_PACKAGE_NAME, appId);
                    this.appStarter.startCloudApp(CloudAppCheckConfig.CLOUD_CUT_APP_PACKAGE_NAME, actionStr, appId);
                    break;
                case 2:
                    this.appStarter.startCloudService(CloudAppCheckConfig.CLOUD_CUT_APP_PACKAGE_NAME, actionStr, appId);
                    break;
                default:
                    Logger.m0d("unknow form :  " + string2);
                    break;
            }
            return;
        }
        if (INTENT_EXIT.equals(string)) {
            Logger.m0d(" exit app !");
            AppManagerImp.getInstance().stopApp(AppManagerImp.getInstance().queryAppInfoByID(appId));
        } else {
            this.appStarter = new AppStarter();
            this.appStarter.startNativeAppWithAction(appId, nlpStr, actionStr);
        }
    }

    private static class SingleHolder {
        private static final ParserProxy instance = new ParserProxy();

        private SingleHolder() {
        }
    }

    public boolean isCorrectChat(String str) {
        NLPBean nLPBean = (NLPBean) new Gson().fromJson(str, NLPBean.class);
        Logger.m0d(" nlpBean : " + nLPBean);
        if (nLPBean != null) {
            if (nLPBean.getSlots() == null || nLPBean.getSlots().isEmpty()) {
                Logger.m0d("slots is null !");
            } else if (nLPBean.getSlots().containsKey("from")) {
                SlotItem slotItem = nLPBean.getSlots().get("from");
                Logger.m0d("slotItem : " + slotItem);
                if (slotItem != null && "autoChatEngine".equals(slotItem.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }
}
