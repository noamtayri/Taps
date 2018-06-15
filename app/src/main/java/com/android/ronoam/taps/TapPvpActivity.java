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
    private int counter = 21; // max 21
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
                upLayout.startAnimation(animation);
                upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + 50);
                bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + 50, bottomLayout.getRight(), bottomLayout.getBottom());
                counter += 1;
                checkWin();
                return false;
            }
        });

        bottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                bottomLayout.startAnimation(animation);
                bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - 50, bottomLayout.getRight(), bottomLayout.getBottom());
                upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - 50);
                counter -= 1;
                checkWin();
                return false;
            }
        });


    }

    private void checkWin(){
        if(counter == 42){ //up win
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
