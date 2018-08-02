package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
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
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.GameLogic.TapPvp;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;


public class TapPvpFragment extends Fragment {

    private final String TAG = "TapPvp Fragment";
    private View upLayout;
    private View bottomLayout;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);
    private int deltaY, gameMode;

    private TapPvp gameLogic;
    private GameActivity activity;
    private MyViewModel model;

    View.OnTouchListener upListener;
    View.OnTouchListener bottomListener;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        new MyLog(TAG, "Create View.");
        View view = inflater.inflate(R.layout.fragment_tap_pvp, container, false);

        upLayout = view.findViewById(R.id.frameLayout_up);
        bottomLayout = view.findViewById(R.id.frameLayout_bottom);

        animation.setDuration(10);

        gameMode = activity.gameMode;

        Bundle data = getArguments();

        if(data != null) {
            int screenHeight = data.getInt(FinalVariables.SCREEN_SIZE);

            gameLogic = new TapPvp(screenHeight);
            deltaY = gameLogic.getDeltaY();

            if (gameMode == FinalVariables.TAP_PVP)
                initTouchListenersModePvp();
            else if(gameMode == FinalVariables.TAP_PVP_ONLINE)
                initTouchListenersModeOnline();

            setTouchListeners();
        }

        return view;
    }

    private void initTouchListenersModePvp(){
        upListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()) {
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
        };

        bottomListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()) {
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
        };
    }

    private void initTouchListenersModeOnline(){
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        model.getInMessage().observe(activity, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                new MyLog(TAG, s);
                if(s != null)
                    doOpponentClick(s);
            }
        });

        bottomListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        gameLogic.downClick();
                        model.setOutMessage(String.valueOf(gameLogic.getCountDown()));
                        bottomLayout.startAnimation(animation);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - deltaY);
                        checkWin();
                        return true;
                }
                return true;
            }
        };
    }

    private void setTouchListeners() {
        bottomLayout.setOnTouchListener(bottomListener);
        if(gameMode == FinalVariables.TAP_PVP)
            upLayout.setOnTouchListener(upListener);
    }

    public void doOpponentClick(String line) {
        upLayout.startAnimation(animation);
        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + deltaY);
        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + deltaY, bottomLayout.getRight(), bottomLayout.getBottom());
        gameLogic.upClick();
        checkWin();
    }

    private void checkWin() {
        int result = gameLogic.checkWin();

        if (result == TapPvp.NO_WIN)
            return;

        String winner = "";
        if (result == TapPvp.UP_WIN) {
            if (gameMode == FinalVariables.TAP_PVP)
                winner = getString(R.string.up_layout_win);
            else
                winner = "You Lost";
        } else if (result == TapPvp.DOWN_WIN) {
            if (gameMode == FinalVariables.TAP_PVP)
                winner = getString(R.string.bottom_layout_win);
            else
                winner = "You Won";
        }
        finishGame(winner);
    }

    private void finishGame(String winner){
        upLayout.setOnTouchListener(null);
        bottomLayout.setOnTouchListener(null);
        Bundle resBundle = new Bundle();
        resBundle.putInt(FinalVariables.GAME_MODE, gameMode);

        resBundle.putString(FinalVariables.WINNER, winner); //bottom wins
        new MyLog(TAG, "Finish " + winner);
        model.setFinish(new MyEntry(FinalVariables.NO_ERRORS, resBundle));
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyLog(TAG, "Created.");
        activity = (GameActivity)getActivity();
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fixLayoutsAfterPause();
            }
        },100);
    }
}
