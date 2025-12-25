package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class XiaoMusic extends CustomServiceBase {

    public XiaoMusic(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            this.avaliable = false;
            this.baseUrl = null;
            this.token = null;
            this.client = null;
            return;
        }
        this.baseUrl = baseUrl;
        this.token = null;
        this.avaliable = true;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .writeTimeout(2, TimeUnit.SECONDS)
                .build();
    }

    public JSONArray handleMusicReq(JSONObject nlp) {
        if (!avaliable) return null;
        String requestUrl = null;
        // 解析意图构造url
        try {
            String intent = nlp.getString("intent");
            if ("play_song".equals(intent)) {               // 播放指定歌曲
                JSONObject slots = nlp.getJSONObject("slots");
                JSONObject songSlot = slots.getJSONObject("song");
                String songValue = songSlot.optString("value");
                requestUrl = baseUrl + "/searchmusicinfo?name=" + URLEncoder.encode(songValue, "UTF-8");

            } else if ("play_random".equals(intent)) {              // 随机播放一首歌曲
                requestUrl = baseUrl + "/randommusic";
            }
            else {
                return null;
            }
        }catch (JSONException | UnsupportedEncodingException e){
            Logger.m1e("异常请求");
            return null;
        }
        //构造请求
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();
        // 从xiaomusic服务器获取播放url
        String audioUrl = null;
        String tts = null;
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseStr = response.body().string();
                JSONObject jsonObject = new JSONObject(responseStr);
                if ("OK".equals(jsonObject.getString("ret"))) {
                    audioUrl = jsonObject.getString("url");
                    String name = jsonObject.getString("name");
                    tts = "为您播放" + name;
                } else {
                    tts = "未找到音乐";
                }
            } else {
                Logger.m1e("异常响应");
                return null;
            }
        } catch (IOException e) {
            Logger.m1e("响应异常");
            return null;
        } catch (JSONException e) {
            Logger.m1e("异常响应");
            return null;
        }
        //构造directives
        JSONArray directives = new JSONArray();
        directives.put(generateTtsDirective(tts, false));
        if (audioUrl != null)
            // 如果报告事件，由于我没有有效的item id，会导致系统会发送一个“点播失败”的tts
            directives.put(generateMediaDirective(audioUrl, true));
        return directives;
    }
}
