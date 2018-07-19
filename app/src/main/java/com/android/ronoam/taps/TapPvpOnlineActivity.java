package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;


public class TapPvpOnlineActivity extends AppCompatActivity {

    private final String TAG = "Tap Online";
    private Handler mUpdateHandler;
    ChatConnection mConnection;

    ChatApplication application;
    Bundle data;

    private boolean triedExit, isGameFinished;

    private View upLayout;
    private View bottomLayout;
    private int screenHeight, deltaY, count = 0;

    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ChatApplication) getApplication();

        setContentView(R.layout.activity_tap_pvp);
        new MyLog(TAG, "Creating tap_pvp_online activity");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                    doOpponentClick(chatLine);

                return true;
            }
        });

        animation.setDuration(10);

        bindUI();
        bindListeners();

        data = getIntent().getExtras();
        screenHeight = data.getInt(FinalVariables.SCREEN_SIZE);

        calculateDeltaY();
        getConnection();
    }

    private void calculateDeltaY() {
        deltaY = screenHeight / (FinalVariables.TAPS_DIFFERENCE * 2);
    }

    private void bindUI() {
        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_bottom);

        upLayout.setBackgroundColor(Color.RED);
        bottomLayout.setBackgroundColor(Color.BLUE);
    }

    private void bindListeners(){
        bottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        clickSend(null);
                        bottomLayout.startAnimation(animation);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - deltaY);
                        //counter -= 1;
                        checkWin();
                        return true;
                }
                return true;
            }
        });

    }

    private void checkWin(){
        //if(counter == 22){ //up win
        if(upLayout.getHeight() >=  screenHeight){
            //I loose
            new MyLog(TAG, "I loose");
            stopGameWithError(FinalVariables.NO_ERRORS, "You Lost");
        }
        else if(bottomLayout.getHeight() >= screenHeight){
            //I win
            new MyLog(TAG, "I win");
            stopGameWithError(FinalVariables.NO_ERRORS, "You Won");
        }
    }

    private void stopGameWithError(final int exitCode, final String winLoose) {
        new MyLog(TAG, "Stopping Game");
        if(isGameFinished){
            new MyLog(TAG, "isFinished = true");
            return;
        }
        isGameFinished = true;

        Intent resIntent = new Intent(TapPvpOnlineActivity.this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP_ONLINE);
        resIntent.putExtra(FinalVariables.SCORE, count);
        if(exitCode == FinalVariables.NO_ERRORS){
            //checkWinner();
            resIntent.putExtra(FinalVariables.WINNER, winLoose);
        }
        setResult(RESULT_OK, resIntent);

        finish();
    }

    //region Network Related
    public void getConnection() {
        application.setChatConnectionHandler(mUpdateHandler);
        mConnection = application.getChatConnection();
    }

    public void clickSend(View v) {
        if(mConnection.getLocalPort() > -1) {
            String messageString = String.valueOf(count++);
            mConnection.sendMessage(messageString);
        }
        else
            new MyToast(this, "Not Connected");
    }

    public void doOpponentClick(String line) {
        upLayout.startAnimation(animation);
        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY);
        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
        //counter += 1;
        checkWin();
    }
    //endregion

    //region Activity Overrides

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    protected void onPause() {
        new MyLog(TAG, "Pausing.");
        super.onPause();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();

        triedExit = false;
    }

    @Override
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        application.setChatConnectionHandler(null);
        mConnection = null;
        application.ChatConnectionTearDown();
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            stopGameWithError(FinalVariables.I_EXIT, null);
            //super.onBackPressed();
        }
        else{
            new MyToast(this, R.string.before_exit);
            triedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    triedExit = false;
                }
            }, 1200);
        }
    }
    //endregion
}
