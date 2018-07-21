package com.android.ronoam.taps.GameLogic;

public class TapPve {
    private int counter;

    public TapPve(){
        counter = 0;
    }

    public void doClick(){
        counter++;
    }

    public int getCounter(){
        return  counter;
    }
}
