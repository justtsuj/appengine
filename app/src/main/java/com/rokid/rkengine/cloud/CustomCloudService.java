package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.AppConfig;
import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomCloudService {

    private static volatile CustomCloudService instance;
    private boolean avaliable;
    // 需要捕获的appid
    // 默认配置，后续会尝试从配置文件加载覆盖
    private List<String> chatAppIds = new ArrayList<>(Arrays.asList(
            "E33FCE60E7294A61B84C43C1A171DFD8",
            "RE1E0645BD994EBFB452AB58FF75A106",
            "RE0933FA9A2E4BA895D887D4ABD85C9D"
    ));
    private List<String> weatherAppIds = new ArrayList<>(Arrays.asList("R35B2BADA5164AC69D4B3BC75F815DF9"));
    private List<String> smartHomeAppIds = new ArrayList<>(Arrays.asList("R24242AB0F85483986A02CF678C02232"));
    private List<String> musicAppIds = new ArrayList<>(Arrays.asList("R4E44BEF982F43B28F4F3DFB3B9A8001"));

    private final ChatBot chatBot;
    private final QWeather qWeather;
    private final HomeAssistant homeAssistant;
    private final XiaoMusic xiaoMusic;

    private CustomCloudService() {
        try {
            AppConfig.load();
        } catch (Exception e) {
            avaliable = false;
            chatBot = null;
            qWeather = null;
            homeAssistant = null;
            xiaoMusic = null;
            Logger.m1e("Failed to load config: " + e.getMessage());
            return;
        }

        updateListFromConfig(chatAppIds, AppConfig.getList("chat-app.ids", ","));
        updateListFromConfig(weatherAppIds, AppConfig.getList("weather-app.ids", ","));
        updateListFromConfig(smartHomeAppIds, AppConfig.getList("smarthome-app.ids", ","));
        updateListFromConfig(musicAppIds, AppConfig.getList("music-app.ids", ","));

        String chatBotApiKey = AppConfig.getString("zhipu.token", "");
        String chatBotSystemPrompt = AppConfig.getString("zhipu.system.prompt", "");
        String chatBotModel = AppConfig.getString("zhipu.model", "");


        String qWeatherUrl = AppConfig.getString("qweather.url", "");
        String qWeatherApiKey = AppConfig.getString("qweather.token", "");

        String homeAssistantUrl = AppConfig.getString("ha.url", "");
        String homeAssistantToken = AppConfig.getString("ha.token", "");

        String xiaoMusicUrl = AppConfig.getString("xiaomusic.url", "");

        avaliable = true;
        chatBot = new ChatBot(chatBotApiKey, chatBotSystemPrompt, chatBotModel);
        qWeather = new QWeather(qWeatherUrl, qWeatherApiKey);
        homeAssistant = new HomeAssistant(homeAssistantUrl, homeAssistantToken);
        xiaoMusic = new XiaoMusic(xiaoMusicUrl);
    }

    public boolean isAvaliable() {
        return avaliable;
    }

    // 这里想要的逻辑是在原有的基础上添加，且保证添加的不重复
    private void updateListFromConfig(List<String> list, List<String> newList) {
        if (newList == null) {
            return;
        }
        for (String item : newList) {
            if (!list.contains(item)) {
                list.add(item);
            }
        }
    }

    public static CustomCloudService getInstance() {
        if (instance == null) {
            synchronized (CustomCloudService.class) {
                if (instance == null) {
                    instance = new CustomCloudService();
                }
            }
        }
        return instance;
    }

    /*
     * hook 若琪原本的应用进行自定义响应
     */
    public void HookAction(JSONObject nlp, JSONObject action) {
        if (!avaliable) return;
        if (action == null) return;
        try {
            String appId = nlp.getString("appId");

            if (chatBot.avaliable && chatAppIds.contains(appId)) {
                chatBot.chat(nlp, action);
            } else if (qWeather.avaliable && weatherAppIds.contains(appId)) {
                qWeather.getWeatherForecast(nlp, action);
            } else if (homeAssistant.avaliable && smartHomeAppIds.contains(appId)) {
                homeAssistant.ProcessConversation(nlp, action);
            } else if (xiaoMusic.avaliable && musicAppIds.contains(appId)) {
                xiaoMusic.handleMusicReq(nlp, action);
            }
        } catch (JSONException e) {
            Logger.m1e("json error");
        }
    }
}
