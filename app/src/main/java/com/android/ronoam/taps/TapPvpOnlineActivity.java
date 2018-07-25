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

import com.android.ronoam.taps.GameLogic.TapPvp;
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
    private int deltaY;

    final Animation animation = new AlphaAnimation(0.1f, 1.0f);
    private TapPvp gameLogic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ChatApplication) getApplication();

        setContentView(R.layout.activity_tap_pvp);
        new MyLog(TAG, "Creating tap_pvp_online activity");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        animation.setDuration(10);

        setHandler();
        bindUI();
        setListeners();

        data = getIntent().getExtras();
        int screenHeight = data.getInt(FinalVariables.SCREEN_SIZE);

        gameLogic = new TapPvp(screenHeight);
        deltaY = gameLogic.getDeltaY();

        getConnection();
    }

    private void setHandler() {
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine == null && !isGameFinished){
                    new MyLog(TAG, "null");
                    if(msg.arg1 == FinalVariables.FROM_OPPONENT){
                        new MyToast(getApplicationContext(), "Connection Lost");
                        stopGameWithError(FinalVariables.OPPONENT_EXIT, null);
                    }
                }
                else if(chatLine != null && !isGameFinished) {
                    new MyLog(TAG, chatLine);
                    doOpponentClick(chatLine);
                }

                return true;
            }
        });
    }

    private void bindUI() {
        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_bottom);

        //upLayout.setBackgroundColor(Color.RED);
        //bottomLayout.setBackgroundColor(Color.BLUE);
    }

    private void setListeners(){
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
                        gameLogic.downClick();
                        checkWin();
                        return true;
                }
                return true;
            }
        });

    }

    private void checkWin(){
        int result = gameLogic.checkWin();

        if(result == TapPvp.NO_WIN)
            return;

        if(result == TapPvp.UP_WIN) {
            new MyLog(TAG, "I loose");
            stopGameWithError(FinalVariables.NO_ERRORS, "You Lost");
        }else if(result == TapPvp.DOWN_WIN){
            //I win
            new MyLog(TAG, "I win");
            stopGameWithError(FinalVariables.NO_ERRORS, "You Won");
        }
    }

    private void fixLayoutsAfterPause(){
        int diff = Math.abs(gameLogic.getCountUp() - gameLogic.getCountDown());
        if(gameLogic.getCountUp() > gameLogic.getCountDown()) {
            upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY * diff);
            bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY * diff, bottomLayout.getRight(), bottomLayout.getBottom());
        }
        else if(gameLogic.getCountUp() < gameLogic.getCountDown()) {
            bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - deltaY * diff, bottomLayout.getRight(), bottomLayout.getBottom());
            upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - deltaY * diff);
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
        //resIntent.putExtra(FinalVariables.SCORE, gameLogic.getCountDown());
        if(exitCode == FinalVariables.NO_ERRORS){
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
            String messageString = String.valueOf(gameLogic.getCountDown());
            mConnection.sendMessage(messageString);
        }
        else
            new MyToast(this, "Not Connected");
    }

    public void doOpponentClick(String line) {
        upLayout.startAnimation(animation);
        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY);
        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
        gameLogic.upClick();
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
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fixLayoutsAfterPause();
            }
        },100);
        super.onResume();
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
