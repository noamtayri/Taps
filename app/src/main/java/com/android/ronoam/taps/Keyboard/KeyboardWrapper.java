package com.android.ronoam.taps.Keyboard;

import android.app.Activity;
import android.inputmethodservice.KeyboardView;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.R;


public class KeyboardWrapper {
    private MyCustomKeyboard mCustomKeyboard;
    private EditText editText;
    private TextView textViewTimer;
    private KeyboardView mKeyboardView;
    private Activity mHostActivity;
    private CountDownTimer countDownTimer;
    private WordsStorage wordsLogic;
    private TextView textViewNextWord;

    public KeyboardWrapper(Activity host, int resKeyboardId, int resQwertyId) {
        wordsLogic = new WordsStorage();
        mHostActivity = host;
        mCustomKeyboard = new MyCustomKeyboard(host, resKeyboardId, resQwertyId);

        mCustomKeyboard.registerEditText(R.id.keyboard_game_edit_text);

        bindUI();

    }

    private void bindUI(){
        editText = mHostActivity.findViewById(R.id.keyboard_game_edit_text);
        textViewTimer = mHostActivity.findViewById(R.id.keyboard_game_timer);
        textViewNextWord = mHostActivity.findViewById(R.id.keyboard_game_next_word);
    }


    public void onBackPressed() {
        if (mCustomKeyboard.isCustomKeyboardVisible()) mCustomKeyboard.hideCustomKeyboard(); else mHostActivity.finish();
    }

    public void startGame(){
        editText.performClick();
        Animation fadeIn = new AlphaAnimation(0.0f,1.0f);
        fadeIn.setDuration(FinalVariables.KEYBORAD_GAME_SHOW_UI);
        editText.startAnimation(fadeIn);
        textViewNextWord.startAnimation(fadeIn);
        editText.setVisibility(View.VISIBLE);
        textViewNextWord.setVisibility(View.VISIBLE);
        setGameTimer();
    }

    private void setGameTimer() {
        textViewTimer.setText(String.valueOf(FinalVariables.KEYBORAD_GAME_TIME / 1000).concat(":00"));
        countDownTimer = new CountDownTimer(FinalVariables.KEYBORAD_GAME_TIME,FinalVariables.KEYBORAD_GAME_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time;
                time = millisUntilFinished / 1000 < 10 ? " " : "";
                time += String.valueOf(millisUntilFinished / 1000).concat(":");
                time += millisUntilFinished % 100 < 10 ? "0" : "";
                time += String.valueOf(millisUntilFinished % 100);

                textViewTimer.setText(time);
            }

            @Override
            public void onFinish() {
                textViewTimer.setText(" 0:00");
                mCustomKeyboard.hideCustomKeyboard();
                editText.setOnClickListener(null);
            }
        }.start();
    }

}
