package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.ronoam.taps.GameLogic.TapPve;

public class TapPveActivity extends AppCompatActivity {

    private TextView timer;
    private TextView countTextView;
    private TextView go;
    private View layout;

    private CountDownTimer countDown;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    private TapPve gameLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pve);

        timer = findViewById(R.id.textView_timer);
        countTextView = findViewById(R.id.textView_count);
        go = findViewById(R.id.textView_go);
        layout = findViewById(R.id.tap_pve_layout);

        gameLogic = new TapPve();
        //layout.setBackgroundColor(Color.GREEN);

        setTouchListener();
        setDesign();

        countTextView.setText(String.valueOf(gameLogic.getCounter()));

        animation.setDuration(10);

        timerLogic();
    }

    private void setTouchListener() {
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                layout.startAnimation(animation);
                gameLogic.doClick();
                countTextView.setText(String.valueOf(gameLogic.getCounter()));
                return false;
            }
        });
    }

    private void setDesign(){
        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        go.setTypeface(AssistantExtraBoldFont);
        //go.setTextColor(Color.BLUE);
        go.animate().scaleXBy(5.0f).scaleYBy(5.0f).alpha(0.0f).setDuration(1000);
        timer.setTypeface(AssistantExtraBoldFont);
        //timer.setTextColor(Color.RED);
        countTextView.setTypeface(AssistantBoldFont);

    }

    private void timerLogic() {
        countDown = new CountDownTimer(FinalVariables.TAP_GAME_TIME, 24) {
            @Override
            public void onTick(long millisUntilFinished) {
                String timerText = "";
                if(millisUntilFinished % 100 > 9)
                    timerText = "" + millisUntilFinished / 1000 + ":" + millisUntilFinished % 100;
                else
                    timerText = "" + millisUntilFinished / 1000 + ":" + "0" + millisUntilFinished % 100;
                timer.setText(timerText);
            }

            @Override
            public void onFinish() {
                layout.setOnTouchListener(null);
                timer.setText("0:00");

                Intent resIntent = new Intent(TapPveActivity.this, HomeActivity.class);
                resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVE);
                resIntent.putExtra(FinalVariables.SCORE, gameLogic.getCounter());
                setResult(RESULT_OK, resIntent);

                finish();
            }
        }.start();
    }

    @Override
    protected void onResume() {
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        super.onResume();
    }
}
