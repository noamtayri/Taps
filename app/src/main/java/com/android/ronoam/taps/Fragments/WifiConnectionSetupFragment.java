package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Network.SetupConnectionLogic.WifiSetupLogic;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;



public class WifiConnectionSetupFragment extends Fragment {

    public static final String TAG = "Connection Fragment";
    TextView mStatusTextView, textViewManualErase, textViewManualMix;
    int gameMode;
    private GameActivity activity;

    private String serviceName;
    NsdHelper mNsdHelper;
    private Handler mNsdHandler, mConnectionLogicHandler;

    WifiSetupLogic connectionLogic;

    MyViewModel model;

    boolean beingStopped, printedResolvedPeer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection_online, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        beingStopped = false;
        printedResolvedPeer = false;

        mStatusTextView = view.findViewById(R.id.textView_status_connection_online);
        textViewManualErase = view.findViewById(R.id.connection_online_text_manual1);
        textViewManualMix = view.findViewById(R.id.connection_online_text_manual2);

        gameMode = activity.gameMode;
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        setDesign();
        initHandlers();

        if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            serviceName = FinalVariables.TYPE_PVP_SERVICE;
            serviceName = serviceName.concat("_" + activity.getResources().getStringArray(R.array.default_keyboards)[activity.language]);
            view.findViewById(R.id.connection_online_imageView_erase).setVisibility(View.VISIBLE);
            view.findViewById(R.id.connection_online_imageView_mix).setVisibility(View.VISIBLE);
            setManuals();
        }

        connectionLogic = new WifiSetupLogic(activity, mConnectionLogicHandler);
    }

    /**
     * initiate {@link #mNsdHandler} to receive NSD updates from {@link #mNsdHelper}
     * initiate {@link #mConnectionLogicHandler} to receive updates from {@link #connectionLogic}
     */
    private void initHandlers(){
        mNsdHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(!printedResolvedPeer)
                    setStatusText(getResources().getStringArray(R.array.network_statuses)[msg.what]);
                if(msg.what == FinalVariables.NETWORK_RESOLVED_SERVICE && !activity.connectionEstablished){
                    new MyLog(TAG, "resolved peer");
                    printedResolvedPeer = true;
                    NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                    connectionLogic.ResolvedService(service);
                }
                return true;
            }
        });

        mConnectionLogicHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case FinalVariables.UPDATE_NETWORK_STATUS:
                        setStatusText((String)msg.obj);
                        break;
                    case FinalVariables.I_EXIT:
                        new MyToast(activity, "Error resolving connection");
                    case FinalVariables.OPPONENT_EXIT:
                        if(msg.what == FinalVariables.OPPONENT_EXIT)
                            new MyToast(activity, "Opponent disconnected");
                    case FinalVariables.NO_ERRORS:
                        finishFragment(msg.what);
                        break;
                }
                return true;
            }
        });
    }

    private void setManuals(){
        int lang = activity.language;
        Resources resources = activity.getResources();
        textViewManualErase.setVisibility(View.VISIBLE);
        textViewManualMix.setVisibility(View.VISIBLE);
        String erase = resources.getStringArray(R.array.manual_line1_erase)[lang];
        String mix = resources.getStringArray(R.array.manual_line2_mix)[lang];
        textViewManualErase.setText(erase);
        textViewManualMix.setText(mix);
    }

    private void setDesign() {
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        mStatusTextView.setTypeface(AssistantExtraBoldFont);
        textViewManualErase.setTypeface(AssistantBoldFont);
        textViewManualMix.setTypeface(AssistantBoldFont);
    }

    private void finishFragment(final int code){
        if(code == FinalVariables.NO_ERRORS){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.moveToNextFragment(null);
                }
            }, 1500);
        }
        else
            setFinishEntry(code);
    }

    private void setFinishEntry(final int code){
        model.setFinish(new MyEntry(code, null));
    }

    private void setStatusText(String line) {
        mStatusTextView.setText(line);
    }

    //region Activity Overrides
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        new MyLog(TAG, "Starting.");
        connectionLogic.registerObserver();

        mNsdHelper = new NsdHelper(activity, serviceName, mNsdHandler);
        mNsdHelper.initializeNsd();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNsdHelper.registerService(activity.getLocalPort());
            }
        },100);
    }

    @Override
    public void onResume() {
        super.onResume();
        new MyLog(TAG, "Resuming.");
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        new MyLog(TAG, "Pausing.");
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        new MyLog(TAG, "Being stopped.");
        connectionLogic.removeObserver();
        beingStopped = true;
        if(mNsdHelper != null) {
            mNsdHelper.tearDown();
            mNsdHelper = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new MyLog(TAG, "Being destroyed.");
    }

    //endregion
}
