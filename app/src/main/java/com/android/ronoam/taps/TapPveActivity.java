package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.ronoam.taps.Utils.FinalVariables;

public class TapPveActivity extends AppCompatActivity {

    private TextView timer;
    private TextView countTextView;
    private View layout;
    private int count;
    private CountDownTimer countDown;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pve);

        timer = findViewById(R.id.textView_timer);
        countTextView = findViewById(R.id.textView_count);
        layout = findViewById(R.id.tap_pve_layout);

        count = 0;
        layout.setBackgroundColor(Color.GREEN);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                layout.startAnimation(animation);
                count++;
                countTextView.setText(""+count);
                return false;
            }
        });

        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        timer.setTypeface(AssistantExtraBoldFont);
        timer.setTextColor(Color.RED);
        countTextView.setTypeface(AssistantBoldFont);
        countTextView.setText("" + count);

        animation.setDuration(10);

        timerLogic();
    }

    private void timerLogic() {
        countDown = new CountDownTimer(FinalVariables.TIMER_LIMIT, 24) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished % 100 > 9)
                    timer.setText("" + millisUntilFinished / 1000 + ":" + millisUntilFinished % 100);
                else
                    timer.setText("" + millisUntilFinished / 1000 + ":" + "0" + millisUntilFinished % 100);
            }

            @Override
            public void onFinish() {
                layout.setOnTouchListener(null);
                timer.setText("0:00");

                Intent i = new Intent(TapPveActivity.this, HomeActivity.class);
                i.putExtra(FinalVariables.SCORE, count);
                startActivity(i);

                finish();
            }
        }.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        countDown.cancel();

        Intent i = new Intent(TapPveActivity.this, HomeActivity.class);
        i.putExtra(FinalVariables.SCORE, count);
        startActivity(i);

        finish();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        countDown.cancel();

        Intent i = new Intent(TapPveActivity.this, HomeActivity.class);
        i.putExtra(FinalVariables.SCORE, count);
        startActivity(i);

        finish();
    }
}
