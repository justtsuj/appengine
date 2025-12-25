package com.rokid.rkengine.bean;

import com.google.gson.Gson;

/* loaded from: classes.dex */
public class BaseBean {
    public String toString() {
        return new Gson().toJson(this);
    }
}
