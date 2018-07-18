package com.android.ronoam.taps;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.KeyboardWrapper;
import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.List;

public class TypePvpOnlineActivity extends TypesClass{

    private static final String TAG = "TypePvp";
    private KeyboardWrapper mKeyboardWrapper;

    private TextView textViewOpponentCounter;

    private Handler mUpdateHandler;
    ChatConnection mConnection;
    private ChatApplication application;

    Bundle data;

    private boolean triedExit, isGameFinished;
    private int myCount, otherCount;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_pvp_online);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        data = getIntent().getExtras();
        List<String> words = data.getStringArrayList(FinalVariables.WORDS_LIST);

        mKeyboardWrapper = new KeyboardWrapper(this, R.id.keyboard_view, R.xml.heb_qwerty, words);
        application = (ChatApplication)getApplication();

        myCount = 0;
        otherCount = 0;

        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine == null)
                    new MyLog(TAG, "null");
                else
                    new MyLog(TAG, chatLine);
                if(chatLine == null && !isGameFinished){
                    if(msg.arg2 == FinalVariables.NETWORK_CONNECTION_LOST){
                        new MyToast(getApplicationContext(), "Connection Lost");
                        stopGameWithError(FinalVariables.OPPONENT_EXIT, null);
                    }
                }
                else if(!isGameFinished)
                    doOpponentSpace(chatLine);

                return true;
            }
        });

        bindUI();
        getConnection();
        startGame();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        application.setChatConnectionHandler(null);
        mConnection = null;
        application.ChatConnectionTearDown();
        mKeyboardWrapper.cancel();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            stopGameWithError(FinalVariables.I_EXIT, null);
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

    private void startGame() {
        mKeyboardWrapper.startGame();
    }

    private void stopGameWithError(final int exitCode, final String winLoose) {
        new MyLog(TAG, "Stopping Game");
        if(isGameFinished){
            new MyLog(TAG, "isFinished = true");
            return;
        }
        isGameFinished = true;

        Intent resIntent = new Intent(TypePvpOnlineActivity.this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVP_ONLINE);
        //resIntent.putExtra(FinalVariables.SCORE, myCount);
        if(exitCode == FinalVariables.NO_ERRORS){
            resIntent.putExtra(FinalVariables.WINNER, winLoose);
        }
        setResult(RESULT_OK, resIntent);

        finish();
    }

    private void bindUI(){
        textViewOpponentCounter = findViewById(R.id.keyboard_game_opponent_counter);
        textViewOpponentCounter.setText("0");
    }

    @Override
    public void SuccessfulType() {
        if(mConnection.getLocalPort() > -1) {
            String messageString = String.valueOf(++myCount);
            mConnection.sendMessage(messageString);
        }
        else
            new MyToast(this, "Not Connected");
    }

    @Override
    public void finishGame(float[] results) {
        String winner = "";
        if(myCount > otherCount)
            winner = "You won";
        else if(myCount < otherCount)
            winner = "You lost";
        else winner = "It's a tie";
        stopGameWithError(FinalVariables.NO_ERRORS, winner);
        //resIntent.putExtra(FinalVariables.WORDS_PER_MIN, results[2]);
    }


    //region Network Related

    public void getConnection() {
        application.setChatConnectionHandler(mUpdateHandler);
        mConnection = application.getChatConnection();
    }

    private void doOpponentSpace(String chatLine) {
        otherCount++;
        textViewOpponentCounter.setText(chatLine);

    }

    //endregion
}
