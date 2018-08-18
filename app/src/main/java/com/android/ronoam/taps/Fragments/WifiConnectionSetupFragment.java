package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Typeface;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Fragments.Adapter.DeviceAdapter;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.HomeActivity;
import com.android.ronoam.taps.MyApplication;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Network.SetupConnectionLogic.WifiSetupLogic;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;


public class WifiConnectionSetupFragment extends Fragment {

    public static final String TAG = "Connection Fragment";

    private HomeActivity activity;

    private String serviceName, deviceName;
    NsdHelper mNsdHelper;
    private Handler mNsdHandler, mConnectionLogicHandler;

    WifiSetupLogic connectionLogic;

    MyViewModel model;

    boolean beingStopped;


    private NetworkConnection mConnection;
    private RecyclerView recyclerViewDevices;
    private TextView textViewInfo, textViewStatus;
    private DeviceAdapter mNsdServicesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wifi_connection_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        textViewStatus = view.findViewById(R.id.textView_status_wifi_connection);
        textViewInfo = view.findViewById(R.id.textView_info_wifi_connection);
        recyclerViewDevices = view.findViewById(R.id.recycler_devices);

        //int gameMode = activity.gameMode;
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        mConnection = activity.getConnection();

        setDesign();
        initHandlers();
        connectionLogic = new WifiSetupLogic(activity, mConnectionLogicHandler);

        serviceName = FinalVariables.TAP_PVP_SERVICE;
        /*if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            serviceName = FinalVariables.TYPE_PVP_SERVICE;
            serviceName = serviceName.concat("_" + activity.getResources().getStringArray(R.array.default_keyboards)[activity.language]);
        }*/
        deviceName =  Settings.Secure.getString(activity.getContentResolver(), "bluetooth_name");
        initRecycler();
    }

    /**
     * initiate {@link #mNsdHandler} to receive NSD updates from {@link #mNsdHelper}
     * initiate {@link #mConnectionLogicHandler} to receive updates from {@link #connectionLogic}
     */
    private void initHandlers(){
        mNsdHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(!activity.connectionEstablished) {
                    textViewStatus.setText(getResources().getStringArray(R.array.network_statuses)[msg.what]);

                    if(msg.what == FinalVariables.NETWORK_REGISTERED_SUCCEEDED){
                        ((MyApplication)activity.getApplication()).getNetworkConnection().setMyService((NsdServiceInfo)msg.obj);
                        connectionLogic.setMyService((NsdServiceInfo)msg.obj);
                    }
                    if(msg.what == FinalVariables.NETWORK_DISCOVERY_SERVICE_LOST){
                        NsdServiceInfo service = (NsdServiceInfo) msg.obj;
                        mNsdServicesAdapter.remove(service);
                    }

                    if (msg.what == FinalVariables.NETWORK_RESOLVED_SERVICE) {
                        mNsdServicesAdapter.add(msg.obj);
                    }
                }
                return true;
            }
        });

        mConnectionLogicHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case FinalVariables.UPDATE_NETWORK_STATUS:
                        textViewInfo.setText((String)msg.obj);
                        break;
                    case FinalVariables.I_EXIT:
                        new MyToast(activity, "Error resolving connection");
                    case FinalVariables.OPPONENT_EXIT:
                        if(msg.what == FinalVariables.OPPONENT_EXIT)
                            new MyToast(activity, "Opponent disconnected");
                    case FinalVariables.NO_ERRORS:
                        if(activity.connectionEstablished)
                            activity.getConnection().getWifiConnection().tearDownServer();
                        finishFragment(msg.what);
                        break;
                }
                return true;
            }
        });
    }

    private void initRecycler(){
        RecyclerView.LayoutManager mLayoutManagerNew = new LinearLayoutManager(activity);
        recyclerViewDevices.setLayoutManager(mLayoutManagerNew);
        recyclerViewDevices.setHasFixedSize(true);

        mNsdServicesAdapter = new DeviceAdapter(connectionLogic.getAdapterHandler(), FinalVariables.WIFI_MODE);
        mNsdServicesAdapter.setServiceName(serviceName);
        recyclerViewDevices.setAdapter(mNsdServicesAdapter);
    }

    private void setDesign() {
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        textViewInfo.setTypeface(AssistantExtraBoldFont);
        textViewStatus.setTypeface(AssistantBoldFont);
    }

    private void finishFragment(final int code){
        if(code == FinalVariables.NO_ERRORS){
            Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(40);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //activity.moveToNextFragment(null);
                    activity.startGame(true);
                    activity.currentFragment--;
                    activity.getSupportFragmentManager().popBackStack();
                }
            }, 1000);
        }
        else
            setFinishEntry(code);
    }

    private void setFinishEntry(final int code){
        model.setFinish(new MyEntry(code, null));
    }


    //region Activity Overrides
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (HomeActivity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        new MyLog(TAG, "Starting.");
        connectionLogic.registerObserver();

        mNsdHelper = new NsdHelper(activity, serviceName, deviceName, mNsdHandler);
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
        mConnection.stopListening();
        mConnection.stopAsyncConnect();
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
        mNsdServicesAdapter.removeAll();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new MyLog(TAG, "Being destroyed.");
    }

    //endregion

}
