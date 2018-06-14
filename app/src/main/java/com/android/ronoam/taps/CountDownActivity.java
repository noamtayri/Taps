package com.android.ronoam.taps;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

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
        countDown = new CountDownTimer(FinalVariables.PRE_TIMER, 24) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeToStart.setText("" + (millisUntilFinished / 1000 + 1));
            }

            @Override
            public void onFinish() {
                switch (gameMode){
                    case 1:
                        //todo: move for tap_pve game mode
                        break;
                    case 2:
                        //todo: move for tap_pvp game mode
                        break;
                    case 3:
                        //todo: move for tap_pvp_online game mode
                        break;
                    case 4:
                        //todo: move for type_pve game mode
                        break;
                    case 5:
                        //todo: move for type_pvp_online game mode
                        break;
                }
            }
        }.start();
    }
}
