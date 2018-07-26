package com.android.ronoam.taps.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.R;


public class ConnectionSetupFragment extends Fragment {

    int gameMode;
    private GameActivity activity;
    private String serviceName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_connection_online, container, false);

        gameMode = activity.gameMode;

        if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            serviceName = FinalVariables.TYPE_PVP_SERVICE;
        return view;
    }
}
