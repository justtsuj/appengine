package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HomeAssistant extends CustomServiceBase {
    public HomeAssistant(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isEmpty() || token == null || token.isEmpty()) {
            this.avaliable = false;
            this.baseUrl = null;
            this.token = null;
            this.client = null;
            return;
        }
        this.baseUrl = baseUrl;
        this.token = token;
        this.avaliable = true;
        // 局域网服务，超时时间可以短一点
        this.client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    public String getDeviceState(String entityId) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/api/states/" + entityId)
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .build();

        return executeRequest(request);
    }

    public void callService(String domain, String service, JSONObject serviceData) throws IOException {
        JSONObject data = new JSONObject();
        try {
            data.put("entity_id", serviceData.get("entity_id"));
        } catch (JSONException e) {
            Logger.m4w(e.getMessage());
        }

        RequestBody body = RequestBody.create(MediaType.get("application/json"), data.toString());

        Request request = new Request.Builder()
                .url(baseUrl + "/api/services/" + domain + "/" + service)
                .header("Authorization", "Bearer " + token)
                .post(body)
                .build();

        executeRequest(request);
    }

    public void ProcessConversation(JSONObject nlp, JSONObject action) {
        if (!avaliable) return;


        String tts;
        try {
            String asr = nlp.getString("asr");
            JSONObject data = new JSONObject();
            data.put("text", asr);
            RequestBody body = RequestBody.create(MediaType.get("application/json"), data.toString());

            Request request = new Request.Builder()
                    .url(baseUrl + "/api/conversation/process")
                    .header("Authorization", "Bearer " + token)
                    .post(body)
                    .build();


            String responseBody = executeRequest(request);
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject responseObject = jsonResponse.getJSONObject("response");
            JSONObject speechObject = responseObject.getJSONObject("speech");
            JSONObject plainObject = speechObject.getJSONObject("plain");
            tts = plainObject.getString("speech");
        } catch (SocketTimeoutException e) {
            Logger.m4w("Home Assistant timeout for " + baseUrl);
            tts = "家庭助理连接超时，请稍后重试";
        } catch (ConnectException | UnknownHostException e) {
            Logger.m4w("Home Assistant connection failed for " + baseUrl);
            tts = "无法连接到家庭助理，请检查服务地址和网络状态";
        } catch (IOException e) {
            Logger.m4w("Home Assistant IO error for " + baseUrl);
            tts = "家庭助理服务通信异常，请检查服务状态";
        } catch (JSONException e) {
            Logger.m4w("Home Assistant JSON error for " + baseUrl);
            tts = "家庭助理服务处理异常";
        }
        patchTTSDirective(action, tts, true);
    }
}
