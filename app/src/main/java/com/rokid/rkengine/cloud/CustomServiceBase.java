package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
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
    protected boolean available;
    protected OkHttpClient client;
    protected String baseUrl;
    protected String token;

    public boolean isAvailable() {
        return available;
    }

    protected JSONObject generateTtsDirective(String tts, boolean disableEvent) throws JSONException {
        JSONObject directive = new JSONObject();
        directive.put("type", "voice");
        directive.put("action", "PLAY");
        directive.put("disableEvent", disableEvent);
        JSONObject item = new JSONObject();
        item.put("itemId", "string of itemid");
        item.put("tts", tts);
        directive.put("item", item);
        return directive;
    }

    protected JSONObject generateMediaDirective(String url, boolean disableEvent) throws JSONException {
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
    }

    protected void patchTTSDirective(JSONObject action, String tts, boolean disableEvent) {
        try {
            boolean found = false;
            JSONArray directives = action.getJSONObject("response").getJSONObject("action").getJSONArray("directives");
            for (int i = 0; i < directives.length(); i++) {
                JSONObject directive = directives.getJSONObject(i);
                if ("voice".equals(directive.getString("type"))) {
                    JSONObject item = directive.getJSONObject("item");
                    item.put("tts", tts);
                    item.put("disableEvent", disableEvent);
                    found = true;
                    break;
                }
            }
            if (!found) {
                directives.put(generateTtsDirective(tts, disableEvent));
            }
        } catch (JSONException e) {
            Logger.m1e("json error");
        }
    }

    protected void patchMediaDirective(JSONObject action, String url, boolean disableEvent) {
        try {
            boolean found = false;
            JSONArray directives = action.getJSONObject("response").getJSONObject("action").getJSONArray("directives");
            for (int i = 0; i < directives.length(); i++) {
                JSONObject directive = directives.getJSONObject(i);
                if ("media".equals(directive.getString("type"))) {
                    JSONObject item = directive.getJSONObject("item");
                    item.put("url", url);
                    item.put("disableEvent", disableEvent);
                    found = true;
                    break;
                }
            }
            if (!found) {
                directives.put(generateMediaDirective(url, disableEvent));
            }
        } catch (JSONException e) {
            Logger.m1e("json error");
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
