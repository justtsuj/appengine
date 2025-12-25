package com.rokid.rkengine.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.rokid.rkengine.scheduler.AppStack;
import com.rokid.rkengine.utils.Logger;

/* loaded from: classes.dex */
public class EventReceiver extends BroadcastReceiver {
    public static final String EXIT_SESSION = "EXIT_SESSION";
    public static final String SIREN = "SIREN";

    public EventReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EXIT_SESSION);
        intentFilter.addAction(SIREN);
        context.registerReceiver(this, intentFilter);
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Logger.m0d(" intent is null !");
            return;
        }
        String action = intent.getAction();
        Logger.m0d("onReceive action : " + action);
        if (EXIT_SESSION.equals(action)) {
            AppStack.getInstance().exitSessionDomain(intent.getStringExtra("appId"));
        } else if (SIREN.equals(action)) {
            RkEngineService.getEngineService().openSiren(intent.getStringExtra("FromType"), intent.getBooleanExtra("isConfirm", false), intent.getIntExtra("durationInMilliseconds", 6000));
        }
    }
}
