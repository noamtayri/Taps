package com.android.ronoam.taps.Fragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class WifiConnectionSetupFragment extends Fragment {

    public static final String TAG = "Connection Fragment";
    TextView mStatusTextView, textViewManualErase, textViewManualMix;
    int gameMode;
    private GameActivity activity;

    private String serviceName;
    NsdHelper mNsdHelper;
    private BlockingQueue<NsdServiceInfo> peersQueue;
    private Handler mNsdHandler, mConnectionLogicHandler;

    WifiSetupLogic connectionLogic;

    MyViewModel model;
    AsyncResolvingPeer resolvingThread;

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

        peersQueue = new ArrayBlockingQueue<>(10);
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
        //resolvingThread = new ResolvingPeerThread();
    }

    /**
     * initiate {@link #mNsdHandler} to receive NSD updates from {@link #mNsdHelper}
     * initiate {@link #mConnectionLogicHandler} to receive updates from {@link #connectionLogic}
     */
    private void initHandlers(){
        mNsdHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                nsdHandler(msg.what);
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
                        if(activity.connectionEstablished)
                            activity.getConnection().getWifiConnection().teardDownServer();
                        finishFragment(msg.what);
                        break;
                }
                return true;
            }
        });
    }

    private void nsdHandler(int msgWhat){
        if(!activity.connectionEstablished)
            setStatusText(getResources().getStringArray(R.array.network_statuses)[msgWhat]);

        if(msgWhat == FinalVariables.NETWORK_RESOLVED_SERVICE && !activity.connectionEstablished){
            resolvedService(mNsdHelper.getChosenServiceInfo());
            new MyLog(TAG, "after add to queue");
            if(!activity.connectionEstablished){
                new MyLog(TAG, "before init async task");
                if(resolvingThread == null){
                    new MyLog(TAG, "async task is null");
                    resolvingThread = new AsyncResolvingPeer();
                    resolvingThread.execute();
                    new MyLog(TAG, "execute async task");
                }
            }
        }
    }

    private void resolvedService(NsdServiceInfo service){
        if(service != null)
        {
            new MyLog(TAG, "resolved peer " + service.getHost().getHostName());
            peersQueue.offer(service);
        }
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
        if(resolvingThread != null && !resolvingThread.isCancelled())
            resolvingThread.cancel(false);
    }

    //endregion

    @SuppressLint("StaticFieldLeak")
    class AsyncResolvingPeer extends AsyncTask<Void, Void, Void>{

        private final String ASYNC_TAG = "async resolve";
        private NsdServiceInfo mService;
        @Override
        protected Void doInBackground(Void... voids) {
            while(!activity.connectionEstablished && !isCancelled()) {
                try {
                    mService = peersQueue.take();
                    new MyLog(ASYNC_TAG, "do_in_background, service = " + mService.getHost().getHostName());
                    publishProgress();
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    new MyLog(ASYNC_TAG, e.getMessage());
                }
            }
            new MyLog(ASYNC_TAG, "do_in_background finished");
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            new MyLog(ASYNC_TAG, "progress update, service = " +mService.getHost().getHostName());
            if (!activity.connectionEstablished && mService != null) {
                connectionLogic.resolvedService(mService);
                mService = null;
            }
        }
    }
}
