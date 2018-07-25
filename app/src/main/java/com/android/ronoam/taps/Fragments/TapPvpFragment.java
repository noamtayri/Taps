package com.android.ronoam.taps.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameLogic.TapPvp;
import com.android.ronoam.taps.HomeActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;

public class TapPvpFragment extends Fragment {

    private final String TAG = "TapPvp Fragment";
    Bundle data;
    private View upLayout;
    private View bottomLayout;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);
    private int deltaY;

    private TapPvp gameLogic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        new MyLog(TAG, "Create View.");
        View view = inflater.inflate(R.layout.activity_tap_pvp, container, false);

        upLayout = view.findViewById(R.id.frameLayout_up);
        bottomLayout = view.findViewById(R.id.frameLayout_bottom);

        animation.setDuration(10);
        data = getArguments();

        if(data != null) {
            int screenHeight = data.getInt(FinalVariables.SCREEN_SIZE);

            gameLogic = new TapPvp(screenHeight);
            deltaY = gameLogic.getDeltaY();

            //bindUI();
            setTouchListeners();
        }

        return view;
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

        Intent resIntent = new Intent(getActivity(), HomeActivity.class);
        resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);

        if(result == TapPvp.UP_WIN)
            resIntent.putExtra(FinalVariables.WINNER, "red / up"); //up wins
        else if(result == TapPvp.DOWN_WIN)
            resIntent.putExtra(FinalVariables.WINNER, "blue / bottom"); //bottom wins

        getActivity().setResult(getActivity().RESULT_OK, resIntent);
        getActivity().finish();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyLog(TAG, "Created.");
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
