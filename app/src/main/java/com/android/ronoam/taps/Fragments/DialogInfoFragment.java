package com.android.ronoam.taps.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.ronoam.taps.HomeActivity;
import com.android.ronoam.taps.R;

public class DialogInfoFragment extends DialogFragment {

    HomeActivity activity;
    TextView textViewManualErase, textViewManualMix;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_info, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        textViewManualErase = view.findViewById(R.id.dialog_info_textView_manual_erase);
        textViewManualMix = view.findViewById(R.id.dialog_info_textView_manual_mix);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        activity = (HomeActivity)getActivity();

        String erase = getResources().getStringArray(R.array.manual_line1_erase)[activity.language];
        String mix = getResources().getStringArray(R.array.manual_line2_mix)[activity.language];

        textViewManualErase.setText(erase);
        textViewManualMix.setText(mix);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog().getWindow() != null) {
            getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.rounded_background);
            getDialog().getWindow().setLayout(
                    getResources().getDisplayMetrics().widthPixels - 150,
                    getResources().getDisplayMetrics().heightPixels / 2
            );

        }
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.25f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.infoBtn.setImageResource(R.drawable.info_negativ);
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.infoBtn.setImageResource(R.drawable.info_w);
    }
}
