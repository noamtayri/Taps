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

import com.android.ronoam.taps.ChatApplication;
import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;

public class CountDownFragment extends Fragment {

    private final String TAG = "Count Down Fragment";

    private CountDownTimer countDown;
    TextView timeToStart;
    private int screenHeight, gameMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_count_down, container, false);

        gameMode = ((GameActivity)getActivity()).gameMode;
        timeToStart = view.findViewById(R.id.textView_time_to_start);
        setDesign();
        if(gameMode == FinalVariables.TAP_PVP || gameMode == FinalVariables.TAP_PVP_ONLINE)
            getScreenSize(container);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                preTimerLogic();
            }
        }, 50);

        return view;
    }

    private void setDesign() {
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getActivity().getAssets(),  "fonts/Assistant-ExtraBold.ttf");
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
                timeToStart.setText("" + (millisUntilFinished / 1000));
                timeToStart.animate().translationX(new DisplayMetrics().widthPixels/2).setDuration(500);
            }

            @Override
            public void onFinish() {
                Bundle bundle = null;
                if(gameMode == FinalVariables.TAP_PVP || gameMode == FinalVariables.TAP_PVP_ONLINE) {
                    bundle = new Bundle();
                    bundle.putInt(FinalVariables.SCREEN_SIZE, screenHeight);
                }
                ((GameActivity)getActivity()).moveToNextFragment(bundle);
            }
        }.start();
    }

    //region Fragment Overrides
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
        new MyLog(TAG, "Being destroyed");
    }

    //endregion

}
