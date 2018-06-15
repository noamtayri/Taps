package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.ronoam.taps.Utils.FinalVariables;

public class TapPvpActivity extends AppCompatActivity {

    private View upLayout;
    private View bottomLayout;
    private int counter = 11;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pvp);

        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_buttom);

        upLayout.setBackgroundColor(Color.RED);
        bottomLayout.setBackgroundColor(Color.BLUE);

        animation.setDuration(10);

        upLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        upLayout.startAnimation(animation);
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + 100);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + 100, bottomLayout.getRight(), bottomLayout.getBottom());
                        counter += 1;
                        checkWin();
                        return true;
                }
                return true;
            }
        });

        bottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        bottomLayout.startAnimation(animation);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - 100, bottomLayout.getRight(), bottomLayout.getBottom());
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - 100);
                        counter -= 1;
                        checkWin();
                        return true;
                }
                return true;
            }
        });

    }

    private void checkWin(){
        if(counter == 22){ //up win
            Intent i = new Intent(TapPvpActivity.this, HomeActivity.class);
            i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
            i.putExtra(FinalVariables.WINNER, "red / up");
            startActivity(i);
        }else if (counter == 0){ //bottom win
            Intent i = new Intent(TapPvpActivity.this, HomeActivity.class);
            i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
            i.putExtra(FinalVariables.WINNER, "blue / buttom");
            startActivity(i);
        }
    }
}
