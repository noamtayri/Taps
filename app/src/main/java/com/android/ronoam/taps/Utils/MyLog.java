package com.android.ronoam.taps.Utils;

import android.util.Log;

public class MyLog {

    private boolean inDev = true; //develope time

    public MyLog(String tag, String msg){
        if(inDev)
            Log.e(tag, msg);
    }
}
