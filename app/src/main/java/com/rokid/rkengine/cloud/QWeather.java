package com.rokid.rkengine.cloud;

import android.text.TextUtils;

import com.rokid.rkengine.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 和风天气API客户端
 * 用于与和风天气API进行交互，获取天气数据
 */
public class QWeather extends CustomServiceBase {


    // JSON Keys
    private static final String KEY_CODE = "code";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_ID = "id";
    private static final String KEY_DAILY = "daily";
    private static final String KEY_FX_DATE = "fxDate";
    private static final String KEY_TEXT_DAY = "textDay";
    private static final String KEY_TEMP_MIN = "tempMin";
    private static final String KEY_TEMP_MAX = "tempMax";
    private static final String KEY_WIND_DIR_DAY = "windDirDay";
    private static final String KEY_WIND_SCALE_DAY = "windScaleDay";

    private static final String API_CODE_SUCCESS = "200";

    public QWeather(String baseUrl, String token) {
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
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

//    public String getCurrentWeather(String location) throws IOException, WeatherApiException {
//        validateLocation(location);
//        HttpUrl url = new HttpUrl.Builder()
//                .scheme(API_SCHEME)
//                .host(baseUrl)
//                .addPathSegment(PATH_WEATHER)
//                .addPathSegment("weather")
//                .addPathSegment("now")
//                .addQueryParameter("key", token)
//                .addQueryParameter(KEY_LOCATION, location)
//                .build();
//        return executeRequest(url);
//    }

    public JSONArray getWeatherForecast(JSONObject nlp) throws WeatherApiException {
        if (!avaliable) return null;

        String location = "101010300"; // Default to Beijing
        int day = 0; // Default to today

        JSONObject slots = nlp.optJSONObject("slots");
        if (slots != null) {
            day = parseDateFromSlots(slots);
            location = parseLocationFromSlots(slots, location);
        }
        validateLocation(location);
        if (day < 0 || day > 2) {
            throw new IllegalArgumentException("参数day必须是0（当天）、1（明天）或2（后天）");
        }
        String tts = null;
        try {
            String locationId = resolveLocationToId(location);
            String forecastUrl = baseUrl + "/v7/weather/3d?key=" + token + "&location=" + locationId;
            Request forecastRequest = new Request.Builder()
                    .url(forecastUrl)
                    .get()
                    .build();
            String responseJson = super.executeRequest(forecastRequest);
            validateApiResponse(responseJson);
            tts = parseForecastResponse(responseJson, day);
        } catch (SocketTimeoutException e) {
            Logger.m4w("和风天气服务 timeout for " + baseUrl);
            tts = "和风天气服务连接超时，请稍后重试";
        } catch (ConnectException | UnknownHostException e) {
            Logger.m4w("和风天气服务 connection failed for " + baseUrl);
            tts = "无法连接到和风天气服务，请检查服务地址和网络状态";
        } catch (IOException e) {
            Logger.m4w("和风天气服务 IO error for " + baseUrl);
            tts = "和风天气服务通信异常，请检查服务状态";
        }
        JSONArray directives = new JSONArray();
        directives.put(generateTtsDirective(tts, false));
        return directives;
    }

    private int parseDateFromSlots(JSONObject slots) {
        JSONObject dateSlot = slots.optJSONObject("date");
        if (dateSlot != null) {
            String dateValue = dateSlot.optString("value");
            switch (dateValue) {
                case "明天":
                    return 1;
                case "后天":
                    return 2;
                default:
                    return 0;
            }
        }
        return 0;
    }

    private String parseLocationFromSlots(JSONObject slots, String defaultLocation) {
        JSONObject locationSlot = slots.optJSONObject("location");
        if (locationSlot != null) {
            String locationValueStr = locationSlot.optString("value");
            if (!TextUtils.isEmpty(locationValueStr)) {
                try {
                    JSONObject locationValueJson = new JSONObject(locationValueStr);
                    String cityName = locationValueJson.optString("city");
                    if (!TextUtils.isEmpty(cityName)) {
                        return cityName;
                    }
                } catch (JSONException e) {
                    Logger.m4w("Error parsing location value JSON: " + e.getMessage());
                }
            }
        }
        return defaultLocation;
    }

//    public String getAirQuality(String location) throws IOException, WeatherApiException {
//        validateLocation(location);
//        HttpUrl url = new HttpUrl.Builder()
//                .scheme(API_SCHEME)
//                .host(baseUrl)
//                .addPathSegment(PATH_AIR)
//                .addPathSegment("air")
//                .addPathSegment("now")
//                .addQueryParameter("key", token)
//                .addQueryParameter(KEY_LOCATION, location)
//                .build();
//        return executeRequest(url);
//    }

    private String resolveLocationToId(String location) throws IOException, WeatherApiException {
        // If location is already an ID (numeric) or lat,lon, no need to look up.
        if (location.matches("^[0-9,.]+$")) {
            return location;
        }
        return fetchLocationIdForCity(location);
    }

    private String fetchLocationIdForCity(String cityName) throws IOException, WeatherApiException {
        Request locationRequest = new Request.Builder()
                .url(baseUrl + "/geo/v2/city/lookup?key=" + token + "&location=" + cityName)
                .get()
                .build();
        String responseJson = super.executeRequest(locationRequest);
        validateApiResponse(responseJson);

        try {
            JSONObject root = new JSONObject(responseJson);
            JSONArray locations = root.optJSONArray(KEY_LOCATION);
            if (locations != null && locations.length() > 0) {
                return locations.getJSONObject(0).getString(KEY_ID);
            } else {
                throw new WeatherApiException("City not found: " + cityName, "404", "No location found for the given city name.");
            }
        } catch (JSONException e) {
            throw new WeatherApiException("Error parsing city data", "PARSE_ERROR", e.getMessage());
        }
    }

    private String parseForecastResponse(String responseJson, int day) {
        try {
            JSONObject rootObject = new JSONObject(responseJson);
            JSONArray dailyArray = rootObject.optJSONArray(KEY_DAILY);

            if (dailyArray == null) {
                return "无法获取天气预报数据。";
            }

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, day);
            String targetDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            for (int i = 0; i < dailyArray.length(); i++) {
                JSONObject dayData = dailyArray.optJSONObject(i);
                if (dayData != null && targetDateStr.equals(dayData.optString(KEY_FX_DATE))) {
                    String textDay = dayData.optString(KEY_TEXT_DAY, "未知");
                    String tempMin = dayData.optString(KEY_TEMP_MIN, "N/A");
                    String tempMax = dayData.optString(KEY_TEMP_MAX, "N/A");
                    String windDirDay = dayData.optString(KEY_WIND_DIR_DAY, "");
                    String windScaleDay = dayData.optString(KEY_WIND_SCALE_DAY, "");

                    String[] dayPrefixes = {"今天", "明天", "后天"};

                    return dayPrefixes[day] + "天气" + textDay + "，"
                            + "温度" + tempMin + "到" + tempMax + "摄氏度，"
                            + windDirDay + windScaleDay + "级。";
                }
            }
            return "暂无 " + targetDateStr + " 的天气数据";
        } catch (JSONException e) {
            Logger.m4w("Error parsing forecast data JSON: " + e.getMessage());
            return "解析天气数据失败";
        }
    }

    private void validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("位置参数不能为空");
        }
    }



    private void validateApiResponse(String responseJson) throws WeatherApiException {
        try {
            JSONObject jsonObject = new JSONObject(responseJson);
            String code = jsonObject.optString(KEY_CODE);
            if (!API_CODE_SUCCESS.equals(code)) {
                Logger.m4w("QWeather API Error: " + responseJson);  
                throw new WeatherApiException("API请求失败", code, "API返回非成功状态码");
            }
        } catch (JSONException e) {
            // If parsing fails, it might not be a standard QWeather JSON error, but still an issue.
            throw new WeatherApiException("解析API响应失败", "PARSE_ERROR", e.getMessage());
        }
    }
}
