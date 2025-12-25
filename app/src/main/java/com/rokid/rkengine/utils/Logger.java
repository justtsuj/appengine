package com.rokid.rkengine.utils;

import android.util.Log;
import java.util.Locale;

/* loaded from: classes.dex */
public class Logger {
    public static final boolean DEBUG = true;
    private static final String TAG = "RKAppEngine - %1$s.%2$s(L:%3$d)";

    private Logger() {
    }

    /* renamed from: v */
    public static void m3v(String... strArr) {
        Log.v(generateTag(), concatMessage(strArr));
    }

    /* renamed from: d */
    public static void m0d(String... strArr) {
        Log.d(generateTag(), concatMessage(strArr));
    }

    /* renamed from: i */
    public static void m2i(String... strArr) {
        Log.i(generateTag(), concatMessage(strArr));
    }

    /* renamed from: w */
    public static void m4w(String... strArr) {
        Log.w(generateTag(), concatMessage(strArr));
    }

    /* renamed from: e */
    public static void m1e(String... strArr) {
        Log.e(generateTag(), concatMessage(strArr));
    }

    private static String generateTag() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        String className = stackTraceElement.getClassName();
        return String.format(Locale.getDefault(), TAG, className.substring(className.lastIndexOf(".") + 1), stackTraceElement.getMethodName(), Integer.valueOf(stackTraceElement.getLineNumber()));
    }

    private static String concatMessage(String... strArr) {
        if (strArr == null || strArr.length < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : strArr) {
            sb.append(str);
        }
        return sb.toString();
    }
}
