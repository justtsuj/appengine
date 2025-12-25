package com.rokid.rkengine.md5;

import com.rokid.rkengine.utils.Logger;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Map;

/* loaded from: classes.dex */
public class MD5Utils {
    private static final String CHARSET_UTF8 = "UTF-8";
    private static final String SECRET_KEY = "secret";

    public static String generateMD5(Map<String, String> map, String str) {
        String[] strArr = (String[]) map.keySet().toArray(new String[0]);
        StringBuilder sb = new StringBuilder();
        for (String str2 : strArr) {
            String str3 = map.get(str2);
            sb.append(str2);
            sb.append("=");
            sb.append(str3);
            sb.append("&");
        }
        sb.append(SECRET_KEY);
        sb.append("=");
        sb.append(str);
        Logger.m0d("query str " + sb.toString());
        return byte2hex(encryptMD5(sb.toString()));
    }

    public static byte[] encryptMD5(String str) {
        try {
            return MessageDigest.getInstance("MD5").digest(str.getBytes(CHARSET_UTF8));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (GeneralSecurityException e2) {
            e2.printStackTrace();
            return null;
        }
    }

    public static String byte2hex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append("0");
            }
            sb.append(hexString.toUpperCase());
        }
        Logger.m0d("generate sign is " + sb.toString());
        return sb.toString();
    }
}
