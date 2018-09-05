package com.android.ronoam.taps.Fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;

public class CountDownFragment extends Fragment {

    private final String TAG = "CountDown Fragment";

    private CountDownTimer countDown;
    TextView timeToStart;
    private int screenHeight, gameMode;
    private boolean timerFinished;
    private GameActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_count_down, container, false);

        gameMode = activity.gameMode;
        if(gameMode == FinalVariables.TAP_PVP || gameMode == FinalVariables.TAP_PVP_ONLINE){
            if(container != null)
                getScreenSize(container);
            else
                screenHeight = activity.getWindow().getAttributes().height;
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        timeToStart = view.findViewById(R.id.textView_time_to_start);
        setDesign();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                preTimerLogic();
            }
        }, 400);
    }

    private void setDesign() {
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        timeToStart.setTypeface(AssistantExtraBoldFont);
        timeToStart.setTextColor(Color.WHITE);
    }

    private void getScreenSize(final ViewGroup container){
        ViewTreeObserver viewTreeObserver = container.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    screenHeight = container.getHeight();
                }
            });
        }
    }

    private void preTimerLogic() {
        countDown = new CountDownTimer(FinalVariables.PRE_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeToStart.setX(-250f);
                timeToStart.setText(String.valueOf(millisUntilFinished / 1000));
                timeToStart.animate().translationX(new DisplayMetrics().widthPixels/2).setDuration(500);
            }

            @Override
            public void onFinish() {
                timerFinished = true;
                Bundle bundle = null;
                if(gameMode == FinalVariables.TAP_PVP || gameMode == FinalVariables.TAP_PVP_ONLINE) {
                    bundle = new Bundle();
                    bundle.putInt(FinalVariables.SCREEN_SIZE, screenHeight);
                }
                activity.moveToNextFragment(bundle);
            }
        }.start();
    }

    //region Fragment Overrides

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
    }

    @Override
    public void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    public void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        new MyLog(TAG, "Pausing");
    }

    @Override
    public void onStop(){
        super.onStop();
        new MyLog(TAG, "Being stopped");
    }

    @Override
    public  void onDestroy(){
        super.onDestroy();
        if(!timerFinished)
            countDown.cancel();
        new MyLog(TAG, "Being destroyed");
    }

    //endregion

}