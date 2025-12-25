package com.rokid.rkengine.utils;

import android.text.TextUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* loaded from: classes.dex */
public class CloudAppCheckConfig {
    public static final String CLOUD_CUT_APP_PACKAGE_NAME = "com.rokid.cloudcutclient";
    public static final String CLOUD_SCENE_APP_PACKAGE_NAME = "com.rokid.cloudsceneclient";
    private static Map<String, String> cloudAppIdMaps = new ConcurrentHashMap();

    public static boolean isCloudApp(String str) {
        return CLOUD_CUT_APP_PACKAGE_NAME.equals(str) || CLOUD_SCENE_APP_PACKAGE_NAME.equals(str);
    }

    public static void storeCloudAppId(String str, String str2) {
        Logger.m0d("store appMap cloudAppId : " + str2 + " appId : " + str);
        if (TextUtils.isEmpty(str)) {
            Logger.m0d("key is null !");
        } else if (TextUtils.isEmpty(str2)) {
            Logger.m0d("value is null !");
        } else {
            cloudAppIdMaps.put(str, str2);
        }
    }

    public static void removeCloudAppId(String str) {
        if (TextUtils.isEmpty(str)) {
            Logger.m0d("cloudAppId is null !");
        } else if (str.equals(cloudAppIdMaps.get(CLOUD_SCENE_APP_PACKAGE_NAME))) {
            cloudAppIdMaps.remove(CLOUD_SCENE_APP_PACKAGE_NAME);
        } else if (str.equals(cloudAppIdMaps.get(CLOUD_CUT_APP_PACKAGE_NAME))) {
            cloudAppIdMaps.remove(CLOUD_CUT_APP_PACKAGE_NAME);
        }
    }

    public static String getCloudAppId(String str) {
        return cloudAppIdMaps.get(str);
    }

    public static String getFinalAppId(String str) {
        if (!isCloudApp(str)) {
            return str;
        }
        Logger.m0d(" appId : " + str + " get CloudAppId :" + getCloudAppId(str));
        return getCloudAppId(str);
    }
}
