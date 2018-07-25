package com.android.ronoam.taps;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.GameLogic.TypeLogic;
import com.android.ronoam.taps.Keyboard.KeyboardWrapper;
import com.android.ronoam.taps.Keyboard.MyCustomKeyboard;
import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.List;
import java.util.Random;

public class TypePvpOnlineActivity extends AppCompatActivity{

    private static final String TAG = "TypePvpOnline";
    private KeyboardWrapper mKeyboardWrapper;

    private TypeLogic gameLogic;
    private MyCustomKeyboard mCustomKeyboard;

    EditText editText;
    TextView textViewOpponentCounter, textViewTimer, textViewNextWord, textViewCounter;

    private CountDownTimer countDownTimer;
    private Handler mUpdateHandler, mUpdateNextWordHandler;
    ChatConnection mConnection;
    private ChatApplication application;

    Bundle data;

    private boolean triedExit, gameFinished = false;

    //region Activity Overrides
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pvp_online);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        data = getIntent().getExtras();
        List<String> words = data.getStringArrayList(FinalVariables.WORDS_LIST);

        bindUI();

        mCustomKeyboard = new MyCustomKeyboard(this, R.id.keyboard_view, R.xml.heb_qwerty);
        mCustomKeyboard.registerEditText(editText);

        gameLogic = new TypeLogic(words);
        editText.addTextChangedListener(gameLogic);

        application = (ChatApplication)getApplication();

        setHandlers();
        setDesign();

        getConnection();
        startGame();
    }

    private void setHandlers() {
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine == null) {
                    new MyLog(TAG, "null");
                    if (msg.arg1 == FinalVariables.FROM_OPPONENT && !gameFinished) {
                        new MyToast(getApplicationContext(), "Connection Lost");
                        stopGameWithError(FinalVariables.OPPONENT_EXIT);
                    }
                }
                else if(!gameFinished) {
                    new MyLog(TAG, chatLine);
                    doOpponentSpace(chatLine);
                }

                return true;
            }
        });

        mUpdateNextWordHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(msg.what == FinalVariables.MOVE_TO_NEXT_WORD) {
                    sendSuccessfulType();
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

    private void bindUI(){
        editText = findViewById(R.id.keyboard_game_edit_text);
        textViewTimer = findViewById(R.id.keyboard_game_timer);
        textViewNextWord = findViewById(R.id.keyboard_game_next_word);
        textViewCounter = findViewById(R.id.keyboard_game_counter);
        textViewOpponentCounter = findViewById(R.id.keyboard_game_opponent_counter);
        textViewOpponentCounter.setText("0");
    }

    private void setDesign() {
        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        /*TextView timer = findViewById(R.id.keyboard_game_timer);
        TextView counter = findViewById(R.id.keyboard_game_counter);*/
        textViewTimer.setTypeface(AssistantExtraBoldFont);
        textViewCounter.setTypeface(AssistantBoldFont);
        textViewOpponentCounter.setTypeface(AssistantBoldFont);
    }

    //region Game Methods

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
        /*float wordsPerMin = gameLogic.getMyResults();
        float opponentWordsPerMin = gameLogic.getOpponentResults();
        Intent resIntent = new Intent(this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVP_ONLINE);
        resIntent.putExtra(FinalVariables.WORDS_PER_MIN, wordsPerMin);
        //resIntent.putExtra(FinalVariables.OPP_WORDS_PER_MIN, opponentWordsPerMin);
        setResult(Activity.RESULT_OK, resIntent);*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                stopGameWithError(FinalVariables.NO_ERRORS);
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

    private void stopGameWithError(final int exitCode) {
        new MyLog(TAG, "Stopping Game");
        Intent resIntent = new Intent(TypePvpOnlineActivity.this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVP_ONLINE);
        //resIntent.putExtra(FinalVariables.SCORE, myCount);
        if(exitCode == FinalVariables.NO_ERRORS){
            resIntent.putExtra(FinalVariables.WINNER, getWinner());
        }
        setResult(RESULT_OK, resIntent);
        finish();
    }

    public String getWinner() {
        String winner = "";
        new MyLog(TAG, "my result = " + gameLogic.getMyResults());
        new MyLog(TAG, "opponent results = " + gameLogic.getOpponentResults());
        if(gameLogic.getMyResults() > gameLogic.getOpponentResults())
            winner = "You won";
        else if(gameLogic.getMyResults() < gameLogic.getOpponentResults())
            winner = "You lost";
        else winner = "It's a tie";

        return winner;
    }

    //endregion

    //region Activity Overrides

    @Override
    protected void onResume() {
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        gameLogic.setNextWordHandler(mUpdateNextWordHandler);
        super.onResume();
    }

    @Override
    protected void onPause() {
        gameLogic.setNextWordHandler(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        application.setChatConnectionHandler(null);
        mConnection = null;
        application.ChatConnectionTearDown();
        if(!gameFinished)
            countDownTimer.cancel();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            finishAnimations();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopGameWithError(FinalVariables.I_EXIT);
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

    private void changeKeyboard(boolean resetToDefault){
        int resId;
        Resources resources = getResources();
        String[] keyboards = resources.getStringArray(R.array.heb_keyboards_options);
        if(resetToDefault){
            resId = resources.getIdentifier(keyboards[0], "xml", getPackageName());
        }

        else {
            final int random = new Random().nextInt(keyboards.length) + 1;
            new MyLog(TAG, keyboards[random]);
            resId = resources.getIdentifier(keyboards[random], "xml", getPackageName());
        }
        mCustomKeyboard.changeKeyboard(resId);
    }

    //region Network Related

    public void getConnection() {
        application.setChatConnectionHandler(mUpdateHandler);
        mConnection = application.getChatConnection();
    }

    public void sendSuccessfulType() {
        if(mConnection.getLocalPort() > -1) {
            String messageString = String.valueOf(gameLogic.getSuccessWordsCounter());
            mConnection.sendMessage(messageString);
        }
        else
            new MyToast(this, "Not Connected");
    }

    private void doOpponentSpace(String chatLine) {
        if(Integer.valueOf(chatLine) > 0) {
            gameLogic.doOpponentSpace();
            textViewOpponentCounter.setText(chatLine);
        }
        //changeKeyboard(false);
    }

    //endregion
}
