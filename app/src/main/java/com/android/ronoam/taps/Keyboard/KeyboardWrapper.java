package com.android.ronoam.taps.Keyboard;

import android.content.Intent;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.HomeActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.TypesClass;

import java.util.List;


public class KeyboardWrapper {
    private MyCustomKeyboard mCustomKeyboard;
    private EditText editText;
    private TextView textViewTimer, textViewCounter;
    private TypesClass mHostActivity;
    private CountDownTimer countDownTimer;
    private WordsLogic wordsLogic;
    private TextView textViewNextWord;

    public KeyboardWrapper(TypesClass host, int resKeyboardId, int resQwertyId) {
        mHostActivity = host;
        wordsLogic = new WordsLogic(mHostActivity, (int)FinalVariables.KEYBORAD_GAME_TIME/1000);

        mCustomKeyboard = new MyCustomKeyboard(host, resKeyboardId, resQwertyId);
        mCustomKeyboard.registerEditText(R.id.keyboard_game_edit_text);

        bindUI();
        setListeners();
    }

    public KeyboardWrapper(TypesClass host, int resKeyboardId, int resQwertyId, List<String> words) {
        mHostActivity = host;
        wordsLogic = new WordsLogic(mHostActivity, (int)FinalVariables.KEYBORAD_GAME_TIME/1000, words);

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
            public void afterTextChanged(Editable editable) {
                if (editable.length() <= 0)
                    return;
                String str = editable.toString();
                int length = str.length();
                char lastChar = str.charAt(length - 1);
                boolean correctSoFar = false;

                switch ((int)lastChar){
                    case KeyCodes.SPC:
                        if(editable.length() > 1){
                            int successes = wordsLogic.typedSpace(str);
                            //animate +1
                            if(successes > Integer.parseInt(textViewCounter.getText().toString()))
                                mHostActivity.SuccessfulType();
                            textViewCounter.setText(String.valueOf(successes));
                            //editable.clear();
                            //textViewNextWord.setText(wordsLogic.getNextWord());
                        }
                        editable.clear();
                        textViewNextWord.setText(wordsLogic.getNextWord());
                        textViewNextWord.setTextColor(Color.BLACK);
                        break;
                    default:
                        String currentWord = wordsLogic.getCurrentWord();
                        correctSoFar = wordsLogic.typedChar(str);
                        if(correctSoFar){
                            String text = editText.getText().toString();

                            Spannable spannable = new SpannableString(currentWord);
                            spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textViewNextWord.setText(spannable, EditText.BufferType.SPANNABLE);
                            //textViewNextWord.setTextColor(Color.GREEN);
                        }
                        else {
                            Spannable spannable = new SpannableString(currentWord);
                            spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                            textViewNextWord.setText(spannable, EditText.BufferType.SPANNABLE);
                            //textViewNextWord.setTextColor(Color.RED);
                        }
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

    private void finishGame(){
        finishAnimations();
        float[] results = wordsLogic.calculateStatistics();
        mHostActivity.finishGame(results);
        /*Intent resIntent = new Intent(mHostActivity, HomeActivity.class);
        //new MyToast(mHostActivity, "words = " + results[2]);
        resIntent.putExtra(com.android.ronoam.taps.FinalVariables.GAME_MODE, com.android.ronoam.taps.FinalVariables.TYPE_PVE);
        resIntent.putExtra(com.android.ronoam.taps.FinalVariables.WORDS_PER_MIN, results[2]);
        mHostActivity.setResult(Activity.RESULT_OK, resIntent);*/

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHostActivity.finish();
            }
        }, FinalVariables.KEYBORAD_GAME_HIDE_UI + 100);
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

    private void finishAnimations(){
        Animation fadeOut = new AlphaAnimation(1.0f,0.0f);
        fadeOut.setDuration(FinalVariables.KEYBORAD_GAME_HIDE_UI);
        editText.startAnimation(fadeOut);
        textViewNextWord.startAnimation(fadeOut);
        editText.setVisibility(View.INVISIBLE);
        textViewNextWord.setVisibility(View.INVISIBLE);

        textViewTimer.setText(" 0:00");
        mCustomKeyboard.hideCustomKeyboard();
        editText.setOnClickListener(null);
    }

    public void cancel(){
        countDownTimer.cancel();
        Intent resIntent = new Intent(mHostActivity, HomeActivity.class);
        mHostActivity.setResult(mHostActivity.RESULT_CANCELED, resIntent);

        finishAnimations();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mHostActivity.finish();
            }
        }, FinalVariables.KEYBORAD_GAME_HIDE_UI + 100);
    }

    //endregion

}
