package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;

import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyToast;


public class KeyboardActivity extends Activity {

    private KeyboardWrapper mkeyboardWrapper;

    private boolean tryedExit = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_game);

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
            super.onBackPressed();
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