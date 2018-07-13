package com.android.ronoam.taps;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.ronoam.taps.Keyboard.TypePveActivity;
import com.android.ronoam.taps.Utils.FinalVariables;
import com.android.ronoam.taps.Utils.MyToast;

public class TapPvpActivity extends AppCompatActivity {

    private View upLayout;
    private View bottomLayout;
    private View container;
    //private int counter = 11;
    private int screenHeight;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pvp);

        container = findViewById(R.id.tap_pvp_container);

        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_bottom);

        upLayout.setBackgroundColor(Color.RED);
        bottomLayout.setBackgroundColor(Color.BLUE);

        getScreenSize();

        animation.setDuration(10);

        upLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        upLayout.startAnimation(animation);
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + 100);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + 100, bottomLayout.getRight(), bottomLayout.getBottom());
                        //counter += 1;
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
            Intent resIntent = new Intent(TapPvpActivity.this, HomeActivity.class);
            resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
            resIntent.putExtra(FinalVariables.WINNER, "red / up");
            setResult(RESULT_OK, resIntent);
            finish();
        }//else if (counter == 0){ //bottom win
        else if(bottomLayout.getHeight() >= screenHeight){
            Intent resIntent = new Intent(TapPvpActivity.this, HomeActivity.class);
            resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
            resIntent.putExtra(FinalVariables.WINNER, "blue / bottom");
            setResult(RESULT_OK, resIntent);
            finish();
        }
    }

    private void getScreenSize(){
        ViewTreeObserver viewTreeObserver = container.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    //viewWidth = container.getWidth();
                    screenHeight = container.getHeight();
                    new MyToast(TapPvpActivity.this, "screen height = " + screenHeight);
                }
            });
        }
    }
}
