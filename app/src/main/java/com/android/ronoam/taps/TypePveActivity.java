package com.android.ronoam.taps;

import android.app.Activity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.KeyboardWrapper;
import com.android.ronoam.taps.Utils.MyToast;

import org.w3c.dom.Text;


public class TypePveActivity extends Activity {

    private KeyboardWrapper mKeyboardWrapper;

    private boolean triedExit = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pve);

        mKeyboardWrapper = new KeyboardWrapper(this, R.id.keyboard_view, R.xml.heb_qwerty);

        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        TextView timer = findViewById(R.id.keyboard_game_timer);
        TextView counter = findViewById(R.id.keyboard_game_counter);
        timer.setTypeface(AssistantExtraBoldFont);
        counter.setTypeface(AssistantBoldFont);

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
}