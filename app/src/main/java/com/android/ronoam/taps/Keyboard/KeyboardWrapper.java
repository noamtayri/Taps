package com.android.ronoam.taps.Keyboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
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
    private TextView textViewTimer, textViewCounter;
    private Activity mHostActivity;
    private CountDownTimer countDownTimer;
    private WordsLogic wordsLogic;
    private TextView textViewNextWord;

    public KeyboardWrapper(Activity host, int resKeyboardId, int resQwertyId) {
        mHostActivity = host;

        wordsLogic = new WordsLogic(mHostActivity, (int)FinalVariables.KEYBORAD_GAME_TIME/1000);

        mCustomKeyboard = new MyCustomKeyboard(host, resKeyboardId, resQwertyId);

        mCustomKeyboard.registerEditText(R.id.keyboard_game_edit_text);

        bindUI();
        setListeners();
    }

    //region Listeners

    private void setListeners() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() <= 0)
                    return;
                String str = s.toString();
                int length = str.length();
                char lastChar = str.charAt(length - 1);
                boolean correctSoFar = false;

                switch ((int)lastChar){
                    case KeyCodes.SPC:
                        if(s.length() > 1){
                            int successes = wordsLogic.typedSpace(str);
                            //animate +1
                            textViewCounter.setText(String.valueOf(successes));
                            s.clear();
                            textViewNextWord.setText(wordsLogic.getNextWord());
                        }
                        else
                            s.clear();
                        textViewNextWord.setTextColor(Color.BLACK);
                        break;
                    default:
                        correctSoFar = wordsLogic.typedChar(str);
                        if(correctSoFar)
                            textViewNextWord.setTextColor(Color.GREEN);
                        else
                            textViewNextWord.setTextColor(Color.RED);
                }
            }
        });
    }

    //endregion

    private void bindUI(){
        editText = mHostActivity.findViewById(R.id.keyboard_game_edit_text);
        textViewTimer = mHostActivity.findViewById(R.id.keyboard_game_timer);
        textViewNextWord = mHostActivity.findViewById(R.id.keyboard_game_next_word);
        textViewCounter = mHostActivity.findViewById(R.id.keyboard_game_counter);

    }

    //region Timers

    public void startGame(){
        editText.performClick();
        Animation fadeIn = new AlphaAnimation(0.0f,1.0f);
        fadeIn.setDuration(FinalVariables.KEYBORAD_GAME_SHOW_UI);

        editText.startAnimation(fadeIn);
        textViewNextWord.startAnimation(fadeIn);
        editText.setVisibility(View.VISIBLE);
        textViewNextWord.setVisibility(View.VISIBLE);

        editText.setOnClickListener(null);
        editText.requestFocus();

        textViewNextWord.setText(wordsLogic.getNextWord());

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
                finishGame();
            }
        }.start();
    }

    private void finishGame(){
        Animation fadeOut = new AlphaAnimation(1.0f,0.0f);
        fadeOut.setDuration(FinalVariables.KEYBORAD_GAME_HIDE_UI);
        editText.startAnimation(fadeOut);
        textViewNextWord.startAnimation(fadeOut);
        editText.setVisibility(View.INVISIBLE);
        textViewNextWord.setVisibility(View.INVISIBLE);

        textViewTimer.setText(" 0:00");
        mCustomKeyboard.hideCustomKeyboard();
        editText.setOnClickListener(null);

        float[] results = wordsLogic.calculateStatistics();
    }

    public void cancel(){
        countDownTimer.cancel();
    }

    //endregion

}
