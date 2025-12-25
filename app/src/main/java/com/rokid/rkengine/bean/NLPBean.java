package com.rokid.rkengine.bean;

import java.util.HashMap;
import java.util.Map;

/* loaded from: classes.dex */
public class NLPBean extends BaseBean {
    private Map<String, SlotItem> slots = new HashMap();

    public Map<String, SlotItem> getSlots() {
        return this.slots;
    }

    public void setSlots(Map<String, SlotItem> map) {
        this.slots = map;
    }
}
