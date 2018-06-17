package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.ronoam.taps.HomeActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.TapPveActivity;
import com.android.ronoam.taps.Utils.FinalVariables;
import com.android.ronoam.taps.Utils.MyToast;


public class TypePveActivity extends Activity {

    private KeyboardWrapper mkeyboardWrapper;

    private boolean tryedExit = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pve);

        mkeyboardWrapper = new KeyboardWrapper(this, R.id.keyboard_view, R.xml.heb_qwerty);

        startGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop timer
        mkeyboardWrapper.cancel();
    }

    private void startGame() {
        mkeyboardWrapper.startGame();
    }


    @Override public void onBackPressed() {
        if(tryedExit) {
            mkeyboardWrapper.cancel();
        }
        else{
            new MyToast(this, R.string.before_exit);
            tryedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryedExit = false;
                }
            }, 1500);
        }
    }
}