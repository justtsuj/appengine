package com.rokid.rkengine.confirm;

import com.rokid.rkengine.utils.Logger;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/* loaded from: classes.dex */
public class HttpClientWrapper {
    private static final String CONTENT_TYPE = "application/octet-stream";
    private static final OkHttpClient okHttpClient;
    private Response response;

    static {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();
    }

    public static HttpClientWrapper getInstance() {
        return SingleHolder.instance;
    }

    public Response sendRequest(String str, Confirm.SetConfirmRequest setConfirmRequest) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        setConfirmRequest.writeTo(byteArrayOutputStream);
        RequestBody body = RequestBody.create(MediaType.parse(CONTENT_TYPE), byteArrayOutputStream.toByteArray());
        Request requestBuild = new Request.Builder()
                .url(str)
                .header("Accept", "text/plain")
                .addHeader("Accept-Charset", "utf-8")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Authorization", ConfirmRequestConfig.getAuthorization())
                .post(body)
                .build();
        Logger.m4w("requestBody is " + byteArrayOutputStream.toString());
        this.response = okHttpClient.newCall(requestBuild).execute();
        return this.response;
    }

    public void close() {
        Response response = this.response;
        if (response == null || response.body() == null) {
            return;
        }
        try {
            this.response.body().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SingleHolder {
        private static final HttpClientWrapper instance = new HttpClientWrapper();
    }

    private HttpClientWrapper() {}
}
