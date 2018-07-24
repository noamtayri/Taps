package com.android.ronoam.taps.GameLogic;

import com.android.ronoam.taps.FinalVariables;

public class TapPvp {

    public static final int NO_WIN = 0;
    public static final int UP_WIN = 1;
    public static final int DOWN_WIN = 2;
    private int countUp, countDown, deltaY, tapsDiff;

    public TapPvp(int screenHeight){
        countUp = 0;
        countDown = 0;
        tapsDiff = FinalVariables.TAPS_DIFFERENCE;
        deltaY = screenHeight / (FinalVariables.TAPS_DIFFERENCE * 2);
    }
    public int getDeltaY(){
        return deltaY;
    }

    public void upClick(){
        countUp++;
    }

    public void downClick(){
        countDown++;
    }

    public int getCountUp(){
        return countUp;
    }

    public int getCountDown(){
        return countDown;
    }

    public int checkWin(){
        int diff = Math.abs(countUp - countDown);
        if(diff >= tapsDiff){
            if(countUp > countDown)
                return UP_WIN;
            else
                return DOWN_WIN;
        }
        return NO_WIN;
    }
}
