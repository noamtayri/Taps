package com.android.ronoam.taps.Utils;

import android.util.Log;

public class MyLog {

    public MyLog(String tag, String msg){
        if(FinalUtilsVariables.IN_DEV)
            Log.e(tag, msg);
    }
}
