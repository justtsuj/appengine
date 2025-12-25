package com.rokid.rkengine.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.rokid.rkengine.utils.Logger;

/* loaded from: classes.dex */
public class SystemStateMachine {
    private static final String ACTION_ROKID_SYSTEM_EXIT = "action.intent.SYSTEM_EXIT";
    public static final String ACTION_SYSTEM_STATE = "action_system_state";
    public static final String STATE_APP_RUNNING = "app_running";
    public static final String STATE_DEFAULT = "default";
    public static final String STATE_DORMANCY = "dormancy";
    public static final String STATE_NET_OFFLINE = "net_offline";
    public static final String STATE_NET_ONLINE = "net_online";
    public static final String STATE_SHUTDOWN = "shutdown";
    public static final String STATE_STAND_BY = "stand_by";
    public static final String TYPE = "type";
    public String currentState;
    private Context mContext;
    BroadcastReceiver systemBroadcastReceiver;

    private SystemStateMachine() {
        this.currentState = "default";
        this.systemBroadcastReceiver = new BroadcastReceiver() { // from class: com.rokid.rkengine.scheduler.SystemStateMachine.1
            /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
            /* JADX WARN: Failed to restore switch over string. Please report as a decompilation issue */
            /* JADX WARN: Removed duplicated region for block: B:22:0x0069  */
            @Override // android.content.BroadcastReceiver
            /*
                Code decompiled incorrectly, please refer to instructions dump.
            */
            public void onReceive(Context context, Intent intent) {
                String stringExtra = intent.getStringExtra(SystemStateMachine.TYPE);
                char c = 1;
                Logger.m0d("rkEvent onReceive type : " + stringExtra);
                if (!TextUtils.isEmpty(stringExtra)) {
                    SystemStateMachine.this.setCurrentState(stringExtra);
                    String str = SystemStateMachine.this.currentState;
                    switch (str.hashCode()) {
                        case -1165856447:
                            if (!str.equals(SystemStateMachine.STATE_NET_OFFLINE)) {
                                c = 65535;
                                break;
                            }
                            break;
                        case -169343402:
                            if (str.equals(SystemStateMachine.STATE_SHUTDOWN)) {
                                c = 4;
                                break;
                            }
                            break;
                        case 297305505:
                            if (str.equals(SystemStateMachine.STATE_APP_RUNNING)) {
                                c = 0;
                                break;
                            }
                            break;
                        case 1283420777:
                            if (str.equals(SystemStateMachine.STATE_DORMANCY)) {
                                c = 2;
                                break;
                            }
                            break;
                        case 1312626016:
                            if (str.equals(SystemStateMachine.STATE_STAND_BY)) {
                                c = 3;
                                break;
                            }
                            break;
                    }
                    switch (c) {
                        case 1:
                            SystemStateMachine.this.startLauncher();
                            break;
                        case 2:
                            AppStack.getInstance().clearAppStack();
                            SystemStateMachine.this.startLauncher();
                            break;
                        case 3:
                            SystemStateMachine.this.startLauncher();
                            break;
                        case 4:
                            SystemStateMachine.this.startLauncher();
                            break;
                    }
                    return;
                }
                Logger.m1e(" error : receive systemState is null !");
            }
        };
    }

    public void initStateListener(Context context) {
        context.registerReceiver(this.systemBroadcastReceiver, new IntentFilter(ACTION_SYSTEM_STATE));
        this.mContext = context;
    }

    public void setCurrentState(String str) {
        this.currentState = str;
        notifySystemExit(str);
    }

    public String getCurrentState() {
        return this.currentState;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startLauncher() {
        new AppStarter().startLauncher();
    }

    public static SystemStateMachine getInstance() {
        return SingleHolder.instance;
    }

    private static class SingleHolder {
        private static final SystemStateMachine instance = new SystemStateMachine();

        private SingleHolder() {
        }
    }

    public void notifySystemExit(String str) {
        if (this.mContext != null) {
            Logger.m2i("notifySystemExit");
            Intent intent = new Intent(ACTION_ROKID_SYSTEM_EXIT);
            intent.putExtra("state", str);
            this.mContext.sendBroadcast(intent);
        }
    }
}
