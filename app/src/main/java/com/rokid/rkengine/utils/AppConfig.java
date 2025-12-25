package com.rokid.rkengine.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public final class AppConfig {

    private static volatile Properties props;

    private AppConfig() {
        // 禁止实例化
    }

    /**
     * 在应用启动时调用一次
     */
    public static void load() throws IOException {
        File configFile = new File("/sdcard/Android/data/com.rokid.rkengine/files/config.properties");
        if (!configFile.exists()) {
            throw new FileNotFoundException(
                    "Config file not found: " + configFile.getAbsolutePath()
            );
        }

        Properties p = new Properties();
        try (InputStream is = new FileInputStream(configFile);
             Reader reader = new InputStreamReader(is, "UTF-8")) {
            p.load(reader);
        }

        props = p;
    }

    private static Properties props() {
        if (props == null) {
            throw new IllegalStateException("AppConfig not loaded");
        }
        return props;
    }

    /* ========= 对外读取接口 ========= */

    public static String getString(String key, String def) {
        return props().getProperty(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(
                props().getProperty(key, String.valueOf(def))
        );
    }

    public static int getInt(String key, int def) {
        try {
            return Integer.parseInt(props().getProperty(key));
        } catch (Exception e) {
            return def;
        }
    }

    public static List<String> getList(String key, String sep) {
        String raw = getString(key, "");
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        for (String s : raw.split(sep)) {
            String trimmed = s.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }
}
