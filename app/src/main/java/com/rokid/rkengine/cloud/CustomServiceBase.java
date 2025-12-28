package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

    protected String executeRequest(Request request) throws SocketTimeoutException, ConnectException, UnknownHostException, IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Logger.m4w("HTTP request failed with status code: " + response.code() + ", message: " + response.message());
                if (response.body() != null) {
                    try {
                        Logger.m4w("Response content: " + response.body().string());
                    } catch (IOException e) {
                        Logger.m4w("Failed to read response body");
                    }
                }
                throw new IOException("HTTP request failed: " + response.code() + " " + response.message());
            }
            ResponseBody body = response.body();
            if (body == null) {
                Logger.m4w("Response body is null");
                throw new IOException("Response body is null");
            }
            return body.string();
        }
    }
}
