package com.android.ronoam.taps.Utils;

import android.content.Context;
import android.widget.Toast;

public class MyToast {

    public MyToast(Context context, String text){
        if(FinalUtilsVariables.IN_DEV)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public MyToast(Context context, int resId){
        if(FinalUtilsVariables.IN_DEV)
            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();

    }
}
