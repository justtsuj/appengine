package com.rokid.rkengine.confirm;

import android.os.IBinder;
import android.text.TextUtils;
import com.rokid.rkengine.md5.MD5Utils;
import com.rokid.rkengine.utils.Logger;
import java.util.LinkedHashMap;
import java.util.Map;
import rokid.os.IRuntimeService;

/* loaded from: classes.dex */
public class ConfirmRequestConfig {
    private static final String BASE_HTTP = "https://";
    private static final String DEFAULT_HOST = "cloudapigw.open.rokid.com";
    private static final String KEY_HOST = "event_req_host";
    private static final String PARAM_KEY_ACCOUNTID = "accountId";
    private static final String PARAM_KEY_DEVICE_ID = "device_id";
    private static final String PARAM_KEY_DEVICE_TYPE_ID = "device_type_id";
    private static final String PARAM_KEY_KEY = "key";
    private static final String PARAM_KEY_SECRET = "secret";
    private static final String PARAM_KEY_SERVICE = "service";
    private static final String PARAM_KEY_SIGN = "sign";
    private static final String PARAM_KEY_TIME = "time";
    private static final String PARAM_KEY_VERSION = "version";
    private static final String PARAM_VALUE_SERVICE = "rest";
    private static final String SEND_EVENT_PATH = "/v1/skill/dispatch/setConfirm";
    private static String mHost;
    private static Map<String, String> params;

    private static void putUnEmptyParam(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            Logger.m0d("param invalidate ! key " + str + " value : " + str2);
            return;
        }
        params.put(str, str2);
    }

    public static Map<String, String> initDeviceInfo() {
        Map platformAccountInfo;
        IBinder runtimeBinder = getRuntimeBinder();
        if (runtimeBinder == null) {
            Logger.m0d(" runtime binder is null ");
            return null;
        }
        try {
            platformAccountInfo = IRuntimeService.Stub.asInterface(runtimeBinder).getPlatformAccountInfo();
        } catch (Exception e) {
            e.printStackTrace();
            platformAccountInfo = null;
        }
        if (platformAccountInfo == null || platformAccountInfo.isEmpty()) {
            Logger.m0d(" deviceMap is null ");
            return null;
        }
        Logger.m0d(" deviceMap is " + platformAccountInfo.toString());
        mHost = (String) platformAccountInfo.get(KEY_HOST);
        params = new LinkedHashMap();
        putUnEmptyParam(PARAM_KEY_KEY, (String) platformAccountInfo.get(PARAM_KEY_KEY));
        putUnEmptyParam(PARAM_KEY_DEVICE_TYPE_ID, (String) platformAccountInfo.get(PARAM_KEY_DEVICE_TYPE_ID));
        putUnEmptyParam(PARAM_KEY_DEVICE_ID, (String) platformAccountInfo.get(PARAM_KEY_DEVICE_ID));
        putUnEmptyParam(PARAM_KEY_SERVICE, PARAM_VALUE_SERVICE);
        putUnEmptyParam(PARAM_KEY_VERSION, (String) platformAccountInfo.get("api_version"));
        putUnEmptyParam(PARAM_KEY_TIME, String.valueOf(System.currentTimeMillis()));
        putUnEmptyParam(PARAM_KEY_SIGN, MD5Utils.generateMD5(params, (String) platformAccountInfo.get(PARAM_KEY_SECRET)));
        Logger.m0d(" params : " + params.toString());
        return params;
    }

    public static String getUrl() {
        String str = mHost;
        if (str == null || str.isEmpty()) {
            mHost = DEFAULT_HOST;
        }
        return BASE_HTTP + mHost + SEND_EVENT_PATH;
    }

    public static String getAuthorization() {
        String strReplace = params.toString().replace("{", "").replace("}", "").replace(",", ";").replace(" ", "");
        Logger.m2i("authorization is " + strReplace);
        return strReplace;
    }

    private static IBinder getRuntimeBinder() {
        try {
            return (IBinder) Class.forName("android.os.ServiceManager").getMethod("getService", String.class).invoke(null, "runtime_java");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
