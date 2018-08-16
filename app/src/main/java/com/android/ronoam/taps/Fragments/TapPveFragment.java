package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.GameLogic.TapPve;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;

public class TapPveFragment extends Fragment {

    GameActivity activity;
    private MyViewModel model;

    private TextView timer;
    private TextView countTextView;
    private TextView go;
    private View layout;

    private CountDownTimer countDown;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    private TapPve gameLogic;
    private boolean gameFinished;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tap_pve, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(activity).get(MyViewModel.class);

        timer = view.findViewById(R.id.textView_timer);
        countTextView = view.findViewById(R.id.textView_count);
        go = view.findViewById(R.id.textView_go);
        layout = view.findViewById(R.id.tap_pve_layout);

        countTextView.setText(String.valueOf(gameLogic.getCounter()));

        setTouchListener();
        setDesign();
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
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        timer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        go.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
                String timerText;
                if(millisUntilFinished % 100 > 9)
                    timerText = "" + millisUntilFinished / 1000 + ":" + millisUntilFinished % 100;
                else
                    timerText = "" + millisUntilFinished / 1000 + ":" + "0" + millisUntilFinished % 100;
                timer.setText(timerText);
            }

            @Override
            public void onFinish() {
                gameFinished = true;
                layout.setOnTouchListener(null);
                String str = "0:00";
                timer.setText(str);

                Bundle resBundle = new Bundle();
                resBundle.putInt(FinalVariables.GAME_MODE, FinalVariables.TAP_PVE);
                resBundle.putInt(FinalVariables.SCORE, gameLogic.getCounter());
                model.setFinish(new MyEntry(FinalVariables.NO_ERRORS, resBundle));
            }
        }.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (GameActivity)getActivity();
        gameLogic = new TapPve();
        animation.setDuration(10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!gameFinished)
            countDown.cancel();
    }
}
