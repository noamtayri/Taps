package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;

import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyToast;


public class KeyboardActivity extends Activity {

    //private MyCustomKeyboard mCustomKeyboard;
    private KeyboardWrapper mkeyboardWrapper;
    private CountDownTimer countDownTimer;

    private boolean tryedExit = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyboard_game);

        mkeyboardWrapper = new KeyboardWrapper(this, R.id.keyboard_view, R.xml.heb_qwerty);

        startGame();
        //setTimer();

        
        //mCustomKeyboard = new MyCustomKeyboard(this, R.id.keyboard_view, R.xml.heb_qwerty );

        //mCustomKeyboard.registerEditText(R.id.edit_text);

        //final Animation animation = new TranslateAnimation(0.0f,360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // stop timer...
        //countDownTimer.cancel();
        mkeyboardWrapper.cancel();
    }

    private void setTimer() {

        countDownTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                new MyToast(KeyboardActivity.this, String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                startGame();
            }
        }.start();
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
        //mkeyboardWrapper.onBackPressed();
    	// NOTE Trap the back key: when the CustomKeyboard is still visible hide it, only when it is invisible, finish activity
        //if (mCustomKeyboard.isCustomKeyboardVisible()) mCustomKeyboard.hideCustomKeyboard(); else this.finish();
    }
}

