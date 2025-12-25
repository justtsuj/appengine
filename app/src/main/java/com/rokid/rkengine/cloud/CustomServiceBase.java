package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

public abstract class CustomServiceBase {
    protected boolean avaliable;
    protected OkHttpClient client;
    protected String baseUrl;
    protected String token;

    public boolean isAvaliable() {
        return avaliable;
    }

    protected JSONObject generateTtsDirective(String tts, boolean disableEvent) {
        try {
            JSONObject directive = new JSONObject();
            directive.put("type", "voice");
            directive.put("action", "PLAY");
            directive.put("disableEvent", disableEvent);
            JSONObject item = new JSONObject();
            item.put("itemId", "string of itemid");
            item.put("tts", tts);
            directive.put("item", item);
            return directive;
        } catch (JSONException e) {
            Logger.m1e("json error");
            return null;
        }
    }

    protected JSONObject generateMediaDirective(String url, boolean disableEvent) {
        try {
            JSONObject directive = new JSONObject();
            directive.put("type", "media");
            directive.put("action", "PLAY");
            directive.put("disableEvent", disableEvent);
            JSONObject item = new JSONObject();
            item.put("itemId", "string of itemid");
            item.put("type", "AUDIO");
            item.put("url", url);
            directive.put("item", item);
            return directive;
        } catch (JSONException e) {
            Logger.m1e("json error");
            return null;
        }
    }
}
