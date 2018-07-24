package com.android.ronoam.taps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.ronoam.taps.GameLogic.TapPvp;

public class TapPvpActivity extends AppCompatActivity {

    Bundle data;
    private View upLayout;
    private View bottomLayout;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);
    private int deltaY;

    private TapPvp gameLogic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pvp);

        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_bottom);

        //upLayout.setBackgroundColor(Color.RED);
        //bottomLayout.setBackgroundColor(Color.BLUE);

        data = getIntent().getExtras();
        assert data != null;
        int screenHeight = data.getInt(FinalVariables.SCREEN_SIZE);

        gameLogic = new TapPvp(screenHeight);
        deltaY = gameLogic.getDeltaY();

        animation.setDuration(10);

        setTouchListeners();
    }

    private void setTouchListeners() {
        upLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        upLayout.startAnimation(animation);
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
                        gameLogic.upClick();
                        checkWin();
                        return true;
                }
                return true;
            }
        });

        bottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
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

        Intent resIntent = new Intent(TapPvpActivity.this, HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);

        if(result == TapPvp.UP_WIN)
            resIntent.putExtra(FinalVariables.WINNER, "red / up"); //up wins
        else if(result == TapPvp.DOWN_WIN)
            resIntent.putExtra(FinalVariables.WINNER, "blue / bottom"); //bottom wins

        setResult(RESULT_OK, resIntent);
        finish();
    }

    @Override
    protected void onResume() {
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        int diff = Math.abs(gameLogic.getCountUp() - gameLogic.getCountDown());
        if(gameLogic.getCountUp() > gameLogic.getCountDown()) {
            upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY * diff);
            bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY * diff, bottomLayout.getRight(), bottomLayout.getBottom());
        }
        else if(gameLogic.getCountUp() < gameLogic.getCountDown()) {
            bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - deltaY * diff, bottomLayout.getRight(), bottomLayout.getBottom());
            upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - deltaY * diff);
        }
        super.onResume();
    }
}
