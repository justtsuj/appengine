package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;


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

    public void handleMusicReq(JSONObject nlp, JSONObject action) {
        if (!avaliable) return;
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
                return;
            }
        }catch (JSONException | UnsupportedEncodingException e){
            Logger.m4w("xiaomusic服务 JSON error for " + baseUrl);
            return;
        }
        //构造请求
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();
        // 从xiaomusic服务器获取播放url
        String audioUrl = null;
        String tts = null;
        try {
            String responseStr = super.executeRequest(request);
            JSONObject jsonObject = new JSONObject(responseStr);
            if ("OK".equals(jsonObject.getString("ret"))) {
                audioUrl = jsonObject.getString("url");
                String name = jsonObject.getString("name");
                tts = "为您播放" + name;
            } else {
                tts = "未找到音乐";
            }
        } catch (SocketTimeoutException e) {
            Logger.m4w("xiaomusic服务 timeout for " + baseUrl);
            tts = "xiaomusic服务连接超时，请稍后重试";
        } catch (ConnectException | UnknownHostException e) {
            Logger.m4w("xiaomusic服务 connection failed for " + baseUrl);
            tts = "无法连接到xiaomusic服务，请检查服务地址和网络状态";
        } catch (IOException e) {
            Logger.m4w("xiaomusic服务 IO error for " + baseUrl);
            tts = "xiaomusic服务通信异常，请检查服务状态";
        } catch (JSONException e) {
            Logger.m4w("xiaomusic服务 JSON error for " + baseUrl);
            return;
        }
        //构造directives
        patchTTSDirective(action, tts, false);
        if (audioUrl != null)
            // 如果报告事件，由于我没有有效的item id，会导致系统会发送一个“点播失败”的tts
            patchMediaDirective(action, audioUrl, false);
    }
}
