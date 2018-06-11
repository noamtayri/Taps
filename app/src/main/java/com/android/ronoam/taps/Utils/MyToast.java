package com.android.ronoam.taps.Utils;

import android.content.Context;
import android.widget.Toast;

public class MyToast {

    private boolean inDev = true; //develope time

    public MyToast(Context context, String text){
        if(inDev)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public MyToast(Context context, int resId){
        if(inDev)
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();

    }
}
