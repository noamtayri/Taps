package com.android.ronoam.taps;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.ronoam.taps.Keyboard.KeyboardWrapper;
import com.android.ronoam.taps.Utils.MyToast;


public class TypePveActivity extends TypesClass {

    private KeyboardWrapper mKeyboardWrapper;

    private boolean triedExit = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pve);

        mKeyboardWrapper = new KeyboardWrapper(this, R.id.keyboard_view, R.xml.heb_qwerty);

        startGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop timer
        mKeyboardWrapper.cancel();
    }

    private void startGame() {
        mKeyboardWrapper.startGame();
    }


    @Override public void onBackPressed() {
        if(triedExit) {
            mKeyboardWrapper.cancel();
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

    @Override
    public void SuccessfulType() {

    }

    @Override
    public void finishGame(float[] results) {
        Intent resIntent = new Intent(this, HomeActivity.class);
        resIntent.putExtra(com.android.ronoam.taps.FinalVariables.GAME_MODE, com.android.ronoam.taps.FinalVariables.TYPE_PVE);
        resIntent.putExtra(com.android.ronoam.taps.FinalVariables.WORDS_PER_MIN, results[2]);
        setResult(Activity.RESULT_OK, resIntent);
    }
}