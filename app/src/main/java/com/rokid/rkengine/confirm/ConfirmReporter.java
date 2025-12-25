package com.rokid.rkengine.confirm;

import com.rokid.rkengine.service.RkEngineService;
import com.rokid.rkengine.utils.Logger;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;

/* loaded from: classes.dex */
public class ConfirmReporter implements Runnable {
    private static final int SIREN_TIME = 6000;
    private static final String SIREN_TYPE_CONFIRM = "CONFIRM";
    private String appId;
    private String attributes;
    private String confirmIntent;
    private List<String> confirmOptions;
    private String confirmSlot;

    public ConfirmReporter(String str, String str2, List<String> list, String str3, String str4) {
        this.confirmIntent = str;
        this.confirmSlot = str2;
        this.confirmOptions = list;
        this.appId = str3;
        this.attributes = str4;
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // java.lang.Runnable
    public void run() {
        Confirm.SetConfirmRequest setConfirmRequestBuild = Confirm.SetConfirmRequest.newBuilder().setAppId(this.appId).setConfirmIntent(this.confirmIntent).setConfirmSlot(this.confirmSlot).setAttributes(this.attributes).build();
        for (int i = 0; i < this.confirmOptions.size(); i++) {
            setConfirmRequestBuild.toBuilder().setConfirmOptions(i, this.confirmOptions.get(i)).build();
        }
        Logger.m2i("request body is " + setConfirmRequestBuild.toString());
        try {
            try {
                ConfirmRequestConfig.initDeviceInfo();
                Logger.m0d("url is " + ConfirmRequestConfig.getUrl());
                Response responseSendRequest = HttpClientWrapper.getInstance().sendRequest(ConfirmRequestConfig.getUrl(), setConfirmRequestBuild);
                if (responseSendRequest != null && responseSendRequest.body() != null) {
                    String strString = responseSendRequest.body().string();
                    Logger.m2i(" confirm respString is : " + strString);
                    Confirm.SetConfirmResponse from = Confirm.SetConfirmResponse.parseFrom(strString.getBytes());
                    if (from != null && from.getSuccess()) {
                        RkEngineService.getEngineService().openSiren(SIREN_TYPE_CONFIRM, true, SIREN_TIME);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            HttpClientWrapper.getInstance().close();
        }
    }
}
