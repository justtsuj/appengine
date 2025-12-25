package com.rokid.rkengine.scheduler;

import android.os.RemoteException;
import android.text.TextUtils;
import com.rokid.rkengine.utils.CloudAppCheckConfig;
import com.rokid.rkengine.utils.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import rokid.rkengine.IRKAppEngineDomainChangeCallback;
import rokid.rkengine.scheduler.AppInfo;

/* loaded from: classes.dex */
public class AppStack {
    private Stack<AppInfo> appStack;
    private IRKAppEngineDomainChangeCallback onDomainChangedListener;

    private AppStack() {
        this.appStack = new Stack<>();
    }

    public static AppStack getInstance() {
        return SingleHolder.instance;
    }

    public void setOnDomainChangedListener(IRKAppEngineDomainChangeCallback iRKAppEngineDomainChangeCallback) {
        if (iRKAppEngineDomainChangeCallback != null) {
            this.onDomainChangedListener = iRKAppEngineDomainChangeCallback;
        }
    }

    public synchronized void pushApp(AppInfo appInfo) {
        if (appInfo == null) {
            return;
        }
        if (appInfo.ignoreFromCDomain) {
            Logger.m0d("ignoreFromCDomain don't push app");
            return;
        }
        Logger.m0d("newAppType is " + appInfo.type + " newApp appId is " + appInfo.appId);
        if (this.appStack.empty()) {
            this.appStack.push(appInfo);
            String finalAppId = CloudAppCheckConfig.getFinalAppId(appInfo.appId);
            if (!TextUtils.isEmpty(finalAppId)) {
                onDomainChanged(finalAppId, null);
            }
        } else {
            AppInfo appInfoPeek = this.appStack.peek();
            Logger.m0d("appStack not empty lastType is " + appInfoPeek.type + " lastApp appId is " + appInfoPeek.appId + " ,  newAppType is " + appInfo.type + " newApp appId is " + appInfo.appId);
            if (TextUtils.isEmpty(appInfoPeek.appId)) {
                this.appStack.pop();
                pushApp(appInfo);
                Logger.m0d("push app error ! lastApp appId is null !!!");
                return;
            }
            if (appInfoPeek.appId.equals(appInfo.appId)) {
                Logger.m0d("lastApp is the same with newApp");
                if (CloudAppCheckConfig.isCloudApp(appInfo.appId) && CloudAppCheckConfig.isCloudApp(appInfoPeek.appId)) {
                    String cloudAppId = CloudAppCheckConfig.getCloudAppId(appInfo.appId);
                    if (!TextUtils.isEmpty(cloudAppId)) {
                        if (this.appStack.size() == 1) {
                            onDomainChanged(cloudAppId, null);
                        } else if (this.appStack.size() == 2) {
                            if (appInfo.type != 2 || appInfoPeek.type != 2) {
                                Logger.m1e("fuck error , please check code !!!");
                            }
                            onDomainChanged(cloudAppId, CloudAppCheckConfig.getFinalAppId(this.appStack.get(0).appId));
                        }
                    }
                }
                Logger.m0d("pushApp appStack size : " + this.appStack.size() + " top app is " + peekApp());
                return;
            }
            String finalAppId2 = CloudAppCheckConfig.getFinalAppId(appInfo.appId);
            if (this.appStack.size() == 1) {
                if (appInfoPeek.type == 1 && appInfo.type == 2) {
                    this.appStack.push(appInfo);
                    onDomainChanged(finalAppId2, CloudAppCheckConfig.getFinalAppId(appInfoPeek.appId));
                } else {
                    this.appStack.pop();
                    this.appStack.push(appInfo);
                    onDomainChanged(finalAppId2, null);
                }
            } else if (this.appStack.size() == 2) {
                AppInfo appInfo2 = this.appStack.get(0);
                if (appInfo.type == 1) {
                    this.appStack.clear();
                    this.appStack.push(appInfo);
                    onDomainChanged(finalAppId2, null);
                } else {
                    this.appStack.pop();
                    this.appStack.push(appInfo);
                    onDomainChanged(finalAppId2, CloudAppCheckConfig.getFinalAppId(appInfo2.appId));
                }
            }
        }
        Logger.m0d("pushApp appStack size : " + this.appStack.size() + " top app is " + peekApp());
    }

    public synchronized boolean popApp(AppInfo appInfo) {
        if (!this.appStack.empty() && appInfo != null && this.appStack.peek() != null) {
            if (TextUtils.isEmpty(appInfo.appId)) {
                Logger.m0d("appInfo appId is null !!!");
                return false;
            }
            if (CloudAppCheckConfig.getFinalAppId(appInfo.appId).equals(CloudAppCheckConfig.getFinalAppId(this.appStack.peek().appId))) {
                Logger.m0d("target app is the same with topApp, so appStack pop app");
                this.appStack.pop();
            }
            Logger.m0d("popApp appStack size : " + this.appStack.size() + " top app is " + peekApp());
            return true;
        }
        Logger.m0d("appStack is empty or appInfo is null");
        return false;
    }

    public synchronized AppInfo peekApp() {
        if (this.appStack.empty()) {
            return null;
        }
        return this.appStack.peek();
    }

    public synchronized boolean exitSessionDomain(String str) {
        if (!this.appStack.empty() && !TextUtils.isEmpty(str)) {
            AppInfo appInfoPeek = this.appStack.peek();
            Logger.m0d("topAppInfo appId : " + appInfoPeek.appId + " endSessionAppId : " + str);
            if (CloudAppCheckConfig.getFinalAppId(str).equals(CloudAppCheckConfig.getFinalAppId(appInfoPeek.appId))) {
                Logger.m0d("endSessionId is the same with topAppId, so appStack pop app");
                this.appStack.pop();
            }
            Logger.m0d("exitSessionDomain appStack size : " + this.appStack.size() + " top app is " + peekApp());
            if (this.appStack.size() == 2) {
                onDomainChanged(CloudAppCheckConfig.getFinalAppId(this.appStack.get(1).appId), CloudAppCheckConfig.getFinalAppId(this.appStack.get(0).appId));
            } else if (this.appStack.size() == 1) {
                onDomainChanged(CloudAppCheckConfig.getFinalAppId(this.appStack.get(0).appId), null);
            } else {
                onDomainChanged(null, null);
            }
            return true;
        }
        Logger.m0d("appStack is empty or endSessionAppId is null");
        return false;
    }

    public synchronized AppInfo getLastApp() {
        if (!this.appStack.isEmpty() && this.appStack.size() != 1) {
            AppInfo appInfoPop = this.appStack.pop();
            AppInfo appInfoPeek = this.appStack.peek();
            onDomainChanged(CloudAppCheckConfig.getFinalAppId(appInfoPop.appId), CloudAppCheckConfig.getFinalAppId(appInfoPeek.appId));
            return appInfoPeek;
        }
        Logger.m0d("getLastApp invalidate");
        return null;
    }

    private void onDomainChanged(String str, String str2) {
        Logger.m0d("onDomainChanged current appId = " + str + " lastDomain = " + str2);
        IRKAppEngineDomainChangeCallback iRKAppEngineDomainChangeCallback = this.onDomainChangedListener;
        if (iRKAppEngineDomainChangeCallback != null) {
            try {
                iRKAppEngineDomainChangeCallback.onDomainChange(str, str2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized int queryAppInfo(AppInfo appInfo) {
        return this.appStack.search(appInfo);
    }

    public synchronized boolean isAppStackEmpty() {
        return this.appStack.isEmpty();
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    public synchronized boolean isAppExitInStack(String str) {
        if (!this.appStack.isEmpty() && !TextUtils.isEmpty(str)) {
            Iterator<AppInfo> it = this.appStack.iterator();
            while (it.hasNext()) {
                if (str.equals(it.next().appId)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public synchronized Stack<AppInfo> getAppStack() {
        return this.appStack;
    }

    public synchronized int getAppNum() {
        return this.appStack.size();
    }

    public synchronized void clearAppStack() {
        Logger.m0d("clearAppStack");
        this.appStack.clear();
        onDomainChanged(null, null);
    }

    public synchronized List<String> queryDomainState() {
        String finalAppId;
        ArrayList arrayList = new ArrayList();
        if (this.appStack.empty()) {
            return arrayList;
        }
        String finalAppId2 = null;
        if (this.appStack.size() == 1) {
            finalAppId2 = CloudAppCheckConfig.getFinalAppId(this.appStack.get(0).appId);
            finalAppId = null;
        } else if (this.appStack.size() == 2) {
            AppInfo appInfo = this.appStack.get(0);
            AppInfo appInfo2 = this.appStack.get(1);
            finalAppId2 = CloudAppCheckConfig.getFinalAppId(appInfo.appId);
            finalAppId = CloudAppCheckConfig.getFinalAppId(appInfo2.appId);
        } else {
            finalAppId = null;
        }
        if (!TextUtils.isEmpty(finalAppId2)) {
            arrayList.add(finalAppId2);
        }
        if (!TextUtils.isEmpty(finalAppId)) {
            arrayList.add(finalAppId);
        }
        return arrayList;
    }

    private static class SingleHolder {
        private static final AppStack instance = new AppStack();

        private SingleHolder() {
        }
    }
}
