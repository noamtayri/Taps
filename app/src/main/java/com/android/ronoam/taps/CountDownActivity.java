package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.TypePveActivity;
import com.android.ronoam.taps.Utils.FinalVariables;

public class CountDownActivity extends AppCompatActivity {

    private TextView timeToStart;
    private CountDownTimer countDown;
    Bundle data;
    int gameMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        timeToStart = findViewById(R.id.textView_time_to_start);

        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        timeToStart.setTypeface(AssistantExtraBoldFont);

        timeToStart.setTextColor(Color.BLACK);

        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE);

        preTimerLogic();
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
                switch (gameMode){
                    case FinalVariables.TAP_PVE:
                        intent = new Intent(CountDownActivity.this, TapPveActivity.class);
                        break;
                    case FinalVariables.TAP_PVP:
                        intent = new Intent(CountDownActivity.this, TapPvpActivity.class);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        //todo: move for tap_pvp_online game mode
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
                }
                finish();
            }
        }.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        countDown.cancel();
        finish();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        countDown.cancel();
        finish();
    }
}
