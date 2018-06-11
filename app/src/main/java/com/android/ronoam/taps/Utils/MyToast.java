package com.android.ronoam.taps.Utils;

import android.app.Activity;
import android.widget.Toast;

public class MyToast {

    private boolean inDev = true; //develope time

    public MyToast(Activity context, String text){
        if(inDev)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public MyToast(Activity context, int resId){
        if(inDev)
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();

    }
}
