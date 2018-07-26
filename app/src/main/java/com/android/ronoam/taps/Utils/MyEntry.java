package com.android.ronoam.taps.Utils;

import android.os.Bundle;

import java.util.Map.Entry;

public class MyEntry implements Entry<Integer, Bundle> {
    private final int key;
    private Bundle value;

    public MyEntry(int key, Bundle value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Integer getKey() {
        return key;
    }

    @Override
    public Bundle getValue() {
        return value;
    }

    @Override
    public Bundle setValue(Bundle value) {
        Bundle old = this.value;
        this.value = value;
        return old;
    }
}
