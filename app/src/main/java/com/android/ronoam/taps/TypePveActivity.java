package com.android.ronoam.taps;

import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;

import com.android.ronoam.taps.Keyboard.KeyboardWrapper;
import com.android.ronoam.taps.Utils.MyToast;


public class TypePveActivity extends Activity {

    private KeyboardWrapper mkeyboardWrapper;

    private boolean triedExit = false;
    
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
        if(triedExit) {
            mkeyboardWrapper.cancel();
        }
        else{
            new MyToast(this, R.string.before_exit);
            triedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    triedExit = false;
                }
            }, 1500);
        }
    }
}