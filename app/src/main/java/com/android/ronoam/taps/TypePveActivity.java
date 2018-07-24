package com.android.ronoam.taps;

import android.app.Activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.GameLogic.TypePve;
import com.android.ronoam.taps.Keyboard.MyCustomKeyboard;
import com.android.ronoam.taps.Utils.MyToast;

import org.w3c.dom.Text;


public class TypePveActivity extends AppCompatActivity {

    private TypePve gameLogic;
    private MyCustomKeyboard mCustomKeyboard;
    private Handler mUpdateHandler;
    private CountDownTimer countDownTimer;

    EditText editText;
    TextView textViewTimer, textViewNextWord, textViewCounter;

    private boolean triedExit = false, gameFinished = false;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pve);

        bindUI();

        mCustomKeyboard = new MyCustomKeyboard(this, R.id.keyboard_view, R.xml.heb_qwerty);
        mCustomKeyboard.registerEditText(editText);

        gameLogic = new TypePve(this);
        editText.addTextChangedListener(gameLogic);

        setHandler();
        setDesign();
        startGame();
    }

    private void setHandler() {
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(msg.what == FinalVariables.MOVE_TO_NEXT_WORD) {
                    String nextWord = data.getString(FinalVariables.NEXT_WORD);
                    int successes = data.getInt(FinalVariables.SUCCESS_WORDS);
                    textViewCounter.setText(String.valueOf(successes));
                    textViewNextWord.setText(nextWord);
                    textViewNextWord.setTextColor(Color.BLACK);
                    return true;
                }
                else if(msg.what == FinalVariables.UPDATE_NEXT_WORD){

                    boolean correctSoFar = data.getBoolean(FinalVariables.CORRECT_SO_FAR);
                    String text = data.getString(FinalVariables.NEXT_WORD);
                    String currentWord = data.getString(FinalVariables.CURRENT_WORD);
                    Spannable spannable = new SpannableString(currentWord);
                    if(text.length() == 0){
                        spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else if (correctSoFar) {
                        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else{
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textViewNextWord.setText(spannable, EditText.BufferType.SPANNABLE);
                }
                return true;
            }
        });
    }

    //region UI and game methods
    private void bindUI(){
        editText = findViewById(R.id.keyboard_game_edit_text);
        textViewTimer = findViewById(R.id.keyboard_game_timer);
        textViewNextWord = findViewById(R.id.keyboard_game_next_word);
        textViewCounter = findViewById(R.id.keyboard_game_counter);
    }

    private void setDesign() {
        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        TextView timer = findViewById(R.id.keyboard_game_timer);
        TextView counter = findViewById(R.id.keyboard_game_counter);
        timer.setTypeface(AssistantExtraBoldFont);
        counter.setTypeface(AssistantBoldFont);
    }

    private void startGame() {
        editText.performClick();
        Animation fadeIn = new AlphaAnimation(0.0f,1.0f);
        fadeIn.setDuration(FinalVariables.KEYBOARD_GAME_SHOW_UI);

        editText.startAnimation(fadeIn);
        textViewNextWord.startAnimation(fadeIn);
        editText.setVisibility(View.VISIBLE);
        textViewNextWord.setVisibility(View.VISIBLE);

        editText.setOnClickListener(null);
        editText.requestFocus();

        textViewNextWord.setText(gameLogic.getNextWord());

        setGameTimer();
    }

    private void setGameTimer(){
        textViewTimer.setText(String.valueOf(FinalVariables.KEYBOARD_GAME_TIME / 1000).concat(":00"));
        countDownTimer = new CountDownTimer(FinalVariables.KEYBOARD_GAME_TIME,FinalVariables.KEYBOARD_GAME_INTERVAL) {
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
        gameFinished = true;
        finishAnimations();
        float wordsPerMin = gameLogic.getResults()[2];
        Intent resIntent = new Intent(this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVE);
        resIntent.putExtra(FinalVariables.WORDS_PER_MIN, wordsPerMin);
        setResult(Activity.RESULT_OK, resIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, FinalVariables.KEYBOARD_GAME_HIDE_UI + 100);
    }

    private void finishAnimations(){
        Animation fadeOut = new AlphaAnimation(1.0f,0.0f);
        fadeOut.setDuration(FinalVariables.KEYBOARD_GAME_HIDE_UI);
        editText.startAnimation(fadeOut);
        textViewNextWord.startAnimation(fadeOut);
        editText.setVisibility(View.INVISIBLE);
        textViewNextWord.setVisibility(View.INVISIBLE);

        textViewTimer.setText(" 0:00");
        mCustomKeyboard.hideCustomKeyboard();
        editText.setOnClickListener(null);
    }

    //endregion

    //region Activity Overrides

    @Override
    protected void onResume() {
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        gameLogic.setNextWordHandler(mUpdateHandler);
        super.onResume();
    }

    @Override
    protected void onPause() {
        gameLogic.setNextWordHandler(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!gameFinished){
            countDownTimer.cancel();
        }
    }


    @Override public void onBackPressed() {
        if(triedExit) {
            Intent resIntent = new Intent(this, HomeActivity.class);
            setResult(RESULT_CANCELED, resIntent);

            finishAnimations();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, FinalVariables.KEYBOARD_GAME_HIDE_UI + 100);
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

    //endregion
}