package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

public class CountDownActivity extends AppCompatActivity {

    private final String TAG = "Count Down";
    private TextView timeToStart;
    private CountDownTimer countDown;
    Bundle data;
    int gameMode;
    private boolean finishCounting;

    private Handler mUpdateHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        timeToStart = findViewById(R.id.textView_time_to_start);

        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        timeToStart.setTypeface(AssistantExtraBoldFont);
        //timeToStart.setText(" ");
        //timeToStart.setTextColor(Color.BLACK);
        timeToStart.setTextColor(Color.WHITE);

        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE);

        finishCounting = false;

        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine == null)
                    new MyLog(TAG, "null");

                if(chatLine == null && !finishCounting){
                    if(msg.arg2 == FinalVariables.NETWORK_CONNECTION_LOST){
                        new MyToast(getApplicationContext(), "Connection Lost");
                        finish();
                    }
                }

                return true;
            }
        });

        if(gameMode == FinalVariables.TAP_PVP_ONLINE || gameMode == FinalVariables.TYPE_PVP_ONLINE){
            ((ChatApplication)getApplication()).setChatConnectionHandler(mUpdateHandler);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                preTimerLogic();
            }
        }, 50);
    }

    private void preTimerLogic() {
        countDown = new CountDownTimer(FinalVariables.PRE_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeToStart.setX(-250f);
                timeToStart.setText("" + (millisUntilFinished / 1000));
                timeToStart.animate().translationX(new DisplayMetrics().widthPixels/2).setDuration(500);
            }

            @Override
            public void onFinish() {
                Intent intent = null;
                finishCounting = true;
                switch (gameMode){
                    case FinalVariables.TAP_PVE:
                        intent = new Intent(CountDownActivity.this, TapPveActivity.class);
                        break;
                    case FinalVariables.TAP_PVP:
                        intent = new Intent(CountDownActivity.this, TapPvpActivity.class);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        intent = new Intent(CountDownActivity.this, TapPvpOnlineActivity.class);
                        intent.putExtras(getIntent().getExtras());
                        break;
                    case FinalVariables.TYPE_PVE:
                        intent = new Intent(CountDownActivity.this, TypePveActivity.class);
                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        //todo: move for type_pvp_online game mode
                        break;
                }
                if(intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(intent);
                    finish();
                }
            }
        }.start();
    }
    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        new MyLog(TAG, "Pausing");
    }

    @Override
    protected void onStop(){
        super.onStop();
        new MyLog(TAG, "Being stopped");
        //countDown.cancel();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        new MyLog(TAG, "Being destroyed");
        if(!finishCounting) {
            if (gameMode == FinalVariables.TAP_PVP_ONLINE || gameMode == FinalVariables.TYPE_PVP_ONLINE)
                ((ChatApplication) getApplication()).ChatConnectionTearDown();

            countDown.cancel();
        }
    }
}
