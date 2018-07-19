package com.android.ronoam.taps;

import android.app.Activity;

public abstract class TypesClass extends Activity implements TypeInterface {
}

interface TypeInterface {

    void SuccessfulType();
    void finishGame(float[] results);
    //void bindUI();
}
