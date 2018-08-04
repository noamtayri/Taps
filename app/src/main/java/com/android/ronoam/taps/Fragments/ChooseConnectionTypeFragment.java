package com.android.ronoam.taps.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.MyApplication;
import com.android.ronoam.taps.R;

public class ChooseConnectionTypeFragment extends Fragment {

    GameActivity activity;
    ImageButton bluetooth, wifi;
    TextView textViewWifi, textViewBluetooth, textViewSelectInfo;

    boolean isWifiPressed, isBluetoothPressed;
    final long duration = 800;
    Runnable runnableFadeInBluetooth, runnableFadeOutBluetooth;
    Runnable runnableFadeInWifi, runnableFadeOutWifi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_choose_connection_type, container,false);

        wifi = view.findViewById(R.id.choose_connection_imageButton_wifi);
        bluetooth = view.findViewById(R.id.choose_connection_imageButton_bluetooth);
        textViewWifi = view.findViewById(R.id.choose_connection_textView_wifi);
        textViewBluetooth = view.findViewById(R.id.choose_connection_textView_bluetooth);
        textViewSelectInfo = view.findViewById(R.id.choose_connection_textView_select);

        setDesign();
        setRunnables();
        setListeners();

        return view;
    }

    private void setRunnables() {
        runnableFadeOutBluetooth = new Runnable() {
            @Override
            public void run() {
                bluetooth.setAlpha(0.1f);
                finishFragment(FinalVariables.BLUETOOTH_MODE);
            }
        };

        runnableFadeInBluetooth = new Runnable() {
            @Override
            public void run() {
                bluetooth.setAlpha(1f);
            }
        };

        runnableFadeOutWifi = new Runnable() {
            @Override
            public void run() {
                wifi.setAlpha(0.1f);
                finishFragment(FinalVariables.WIFI_MODE);
            }
        };

        runnableFadeInWifi = new Runnable() {
            @Override
            public void run() {
                wifi.setAlpha(1f);
            }
        };
    }

    private synchronized void finishFragment(int selectedMode){
        ((MyApplication)activity.getApplication()).setConnectionMethod(selectedMode);
        activity.moveToNextFragment(null);
    }

    private void setDesign(){
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        textViewWifi.setTypeface(AssistantBoldFont);
        textViewBluetooth.setTypeface(AssistantBoldFont);
        textViewSelectInfo.setTypeface(AssistantExtraBoldFont);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListeners() {

        bluetooth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!isBluetoothPressed) {
                            isBluetoothPressed  = true;
                            bluetooth.setAlpha(bluetooth.getAlpha());
                            bluetooth.clearAnimation();
                            setFadeOutAnimationBluetooth();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(isBluetoothPressed){
                            isBluetoothPressed = false;
                            bluetooth.setAlpha(bluetooth.getAlpha());
                            bluetooth.clearAnimation();
                            setFadeInAnimationBluetooth();
                        }
                        return true;
                }
                return false;
            }
        });

        wifi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if(!isWifiPressed) {
                            isWifiPressed  = true;
                            wifi.setAlpha(wifi.getAlpha());
                            wifi.clearAnimation();
                            setFadeOutAnimationWifi();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(isWifiPressed){
                            isWifiPressed = false;
                            wifi.setAlpha(wifi.getAlpha());
                            wifi.clearAnimation();
                            setFadeInAnimationWifi();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void setFadeOutAnimationBluetooth(){
        bluetooth.animate().alpha(0.1f).setDuration(duration).withEndAction(runnableFadeOutBluetooth);
    }

    private void setFadeInAnimationBluetooth(){
        bluetooth.animate().alpha(1f).setDuration(duration).withEndAction(runnableFadeInBluetooth);
    }

    private void setFadeOutAnimationWifi(){
        wifi.animate().alpha(0.1f).setDuration(duration).withEndAction(runnableFadeOutWifi);
    }

    private void setFadeInAnimationWifi(){
        wifi.animate().alpha(1f).setDuration(duration).withEndAction(runnableFadeInWifi);
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
    }
}
