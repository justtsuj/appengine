package com.rokid.rkengine.cloud;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ChatBot extends CustomServiceBase {

    // Constants for JSON keys and values
    private static final String ROLE = "role";
    private static final String CONTENT = "content";
    private static final String SYSTEM = "system";
    private static final String USER = "user";
    private static final String ASSISTANT = "assistant";
    private static final String MODEL = "model";
    private static final String MESSAGES = "messages";
    private static final String THINKING = "thinking";
    private static final String TYPE = "type";
    private static final String DISABLED = "disabled";
    private static final String TEMPERATURE = "temperature";
    private static final String CHOICES = "choices";
    private static final String MESSAGE = "message";

    // API and Model Configuration
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个高效且知识渊博的生活小助理，能陪伴用户在日常生活中提供帮助和支持。";
    private static final String MODEL_NAME = "glm-4.6";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    // Message History Configuration
    private static final int MAX_MESSAGE_PAIRS = 10;

    private final JSONObject systemMessage; // System message object
    private final String modelName;
    private final List<MessagePair> messageHistory; // Thread-safe message history list
    private long lastExecutionTime; // Response time of the last request

    private static class MessagePair {
        final JSONObject userMessage;
        final JSONObject assistantMessage;

        MessagePair(JSONObject userMessage, JSONObject assistantMessage) {
            this.userMessage = userMessage;
            this.assistantMessage = assistantMessage;
        }
    }

    public ChatBot(String token, String systemPrompt, String modelName) {
        if (token == null || token.isEmpty()) {
            this.avaliable = false;
            this.baseUrl = null;
            this.token = null;
            this.client = null;
            systemMessage = null;
            messageHistory = null;
            this.modelName = null;
            return;
        }
        this.baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        this.token = token;
        this.avaliable = true;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .readTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(6, TimeUnit.SECONDS)
                .build();
        this.messageHistory = Collections.synchronizedList(new ArrayList<>());
        if (modelName == null || modelName.isEmpty()) this.modelName = MODEL_NAME;
        else this.modelName = modelName;
        if (systemPrompt == null || systemPrompt.isEmpty()) systemPrompt = DEFAULT_SYSTEM_PROMPT;
        this.systemMessage = createMessage(SYSTEM, systemPrompt);
    }

    public JSONArray chat(String userMessageText) {
        if (!avaliable) return null;
        String tts;
        long startTime = System.currentTimeMillis();
        RequestBody body;
        try {
            body = buildRequestBody(userMessageText);
        } catch (JSONException e){
            Logger.m4w("智谱服务构造消息失败 " + e.getMessage());
            return null;
        }
        Request request = new Request.Builder()
                .url(this.baseUrl)
                .addHeader("Authorization", "Bearer " + this.token)
                .addHeader("Content-Type", JSON_MEDIA_TYPE.toString())
                .post(body)
                .build();
        try {
            this.lastExecutionTime = System.currentTimeMillis() - startTime;
            String responseBody = executeRequest(request);
            tts = parseChatResponse(responseBody);
        } catch (SocketTimeoutException e) {
            Logger.m4w("智谱服务 timeout for " + baseUrl);
            tts = "智谱服务连接超时，请稍后重试";
        } catch (ConnectException | UnknownHostException e) {
            Logger.m4w("智谱服务 connection failed for " + baseUrl);
            tts = "无法连接到智谱服务，请检查服务地址和网络状态";
        } catch (IOException e) {
            Logger.m4w("智谱服务 IO error for " + baseUrl + ", " + e.getMessage());
            tts = "智谱服务通信异常，请检查服务状态";
        }
        JSONArray directives = new JSONArray();
        directives.put(generateTtsDirective(tts, false));
        return directives;
    }

    private RequestBody buildRequestBody(String userMessageText) throws JSONException {
        // Create current user message
        JSONObject userMessage = createMessage(USER, userMessageText);

        // Create message array
        JSONArray messagesArray = new JSONArray();
        messagesArray.put(this.systemMessage);

        // Add history messages
        synchronized (messageHistory) {
            for (MessagePair pair : messageHistory) {
                messagesArray.put(pair.userMessage);
                messagesArray.put(pair.assistantMessage);
            }
        }
        messagesArray.put(userMessage);

        // Create request body
        JSONObject requestJson = new JSONObject();
        requestJson.put(MODEL, this.modelName);
        requestJson.put(MESSAGES, messagesArray);
        requestJson.put(THINKING, new JSONObject().put(TYPE, DISABLED));
        requestJson.put(TEMPERATURE, 0.6);

        return RequestBody.create(JSON_MEDIA_TYPE, requestJson.toString());
    }

    private String parseChatResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray choices = jsonResponse.optJSONArray(CHOICES);

            if (choices != null && choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject assistantResponseMessage = firstChoice.optJSONObject(MESSAGE);
                if (assistantResponseMessage != null) {
                    return assistantResponseMessage.optString(CONTENT, "");
                }
            }
            return "未获取到AI回复";
        }
        catch (JSONException e){
            Logger.m4w("ChatBot service JSON error for " + baseUrl);
            return "AI回复解析失败";
        }
    }

    public void updateMessageHistory(String userMessage, String assistantMessage) {
        JSONObject userMessageObj = createMessage(USER, userMessage);
        JSONObject assistantMessageObj = createMessage(ASSISTANT, assistantMessage);

        synchronized (messageHistory) {
            messageHistory.add(new MessagePair(userMessageObj, assistantMessageObj));
            while (messageHistory.size() > MAX_MESSAGE_PAIRS) {
                messageHistory.remove(0);
            }
        }
    }

    public void clearMessageHistory() {
        messageHistory.clear();
    }

    public int getMessageHistoryCount() {
        return messageHistory.size();
    }

    public long getLastExecutionTime() {
        return this.lastExecutionTime;
    }

    private JSONObject createMessage(String role, String content) {
        JSONObject message = new JSONObject();
        try {
            message.put(ROLE, role);
            message.put(CONTENT, content);
        } catch (JSONException e) {
            Logger.m4w("Failed to create JSON message object: " + e.getMessage());
        }
        return message;
    }
}
