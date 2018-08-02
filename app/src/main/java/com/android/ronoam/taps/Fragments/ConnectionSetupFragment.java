package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ConnectionSetupFragment extends Fragment {

    public static final String TAG = "Connection Fragment";
    TextView mStatusTextView;
    int gameMode;
    private GameActivity activity;

    private String serviceName;
    NsdHelper mNsdHelper;

    AsyncTaskCheckStatus mAsyncTask;

    MyViewModel model;

    private List<String> words;

    boolean firstMessage, isConnectionEstablished, beingStopped, wordsCreated, meResolvedPeer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connection_online, container, false);

        firstMessage = true;
        isConnectionEstablished = false;
        beingStopped = false;

        mStatusTextView = view.findViewById(R.id.textView_status_connection_online);
        gameMode = activity.gameMode;

        if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            serviceName = FinalVariables.TYPE_PVP_SERVICE;
            serviceName = serviceName.concat("_" + activity.getResources().getStringArray(R.array.default_keyboards)[activity.language]);
        }
        
        model.getConnectionInMessages().observe(getActivity(), new Observer<Message>() {
            @Override
            public void onChanged(@Nullable Message message) {
                if(gameMode == FinalVariables.TAP_PVP_ONLINE)
                    tapMessageReceiver(message);
                else
                    typeMessageReceiver(message);
            }
        });

        return view;
    }

    private void typeMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                finishFragment(FinalVariables.I_EXIT);
                new MyToast(getActivity(), "Error resolving connection");
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                new MyToast(getActivity(), "Opponent disconnected");
                new MyLog(TAG, "Opponent disconnected");
                mAsyncTask.cancel(true);
                finishFragment(FinalVariables.OPPONENT_EXIT);
            }
            else if(firstMessage && !isConnectionEstablished) {
                addChatLine(chatLine);
                model.setOpponentName(chatLine);
                activity.connectionEstablished = true;
                isConnectionEstablished = true;
                initialSend();
                firstMessage = false;
            }
            else if(!firstMessage && isConnectionEstablished && !wordsCreated) {
                new MyLog(TAG, "received words");
                new MyLog(TAG, chatLine);
                //receive words
                words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                wordsCreated = true;
                model.setWords(words);
                finishFragment(FinalVariables.NO_ERRORS);
            }
            else if(firstMessage && isConnectionEstablished) {
                if (meResolvedPeer) {
                    addChatLine(chatLine);
                    model.setOpponentName(chatLine);
                    //create and send words
                    sendWords();
                    finishFragment(FinalVariables.NO_ERRORS);
                }
            }
        }
    }

    private void tapMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");

        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                finishFragment(FinalVariables.I_EXIT);
                new MyToast(getActivity(), "Error resolving connection");
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                new MyToast(getActivity(), "Opponent disconnected");
                new MyLog(TAG, "Opponent disconnected");
                mAsyncTask.cancel(true);
                finishFragment(FinalVariables.OPPONENT_EXIT);
            }
            else{
                addChatLine(chatLine);
                model.setOpponentName(chatLine);
                if(firstMessage && !isConnectionEstablished) {
                    activity.connectionEstablished = true;
                    isConnectionEstablished = true;
                    initialSend();
                    firstMessage = false;
                }
                finishFragment(FinalVariables.NO_ERRORS);
            }
        }
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

    public void initialSend() {
        if(activity.getLocalPort() > -1) {
            String msg = Settings.Secure.getString(activity.getContentResolver(), "bluetooth_name");
            model.setOutMessage(msg);
        }
    }

    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(getActivity(), activity.language);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            new MyLog(TAG, joinedStr);
            model.setWords(words);
            model.setOutMessage(joinedStr);
        }
    }

    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }

    private void addChatLine(String line) {
        mStatusTextView.setText(line);
    }

    //region Activity Overrides
    @Override
    public void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();

        mNsdHelper = new NsdHelper(getActivity(), serviceName);
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
        new MyLog(TAG, "Resuming.");
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
        mAsyncTask = new AsyncTaskCheckStatus();
        mAsyncTask.execute();
    }

    @Override
    public void onPause() {
        new MyLog(TAG, "Pausing.");
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        if(mAsyncTask != null && !mAsyncTask.isCancelled())
            mAsyncTask.cancel(false);
        super.onPause();
    }

    @Override
    public void onStop() {
        new MyLog(TAG, "Being stopped.");
        beingStopped = true;
        if(mNsdHelper != null) {
            mNsdHelper.tearDown();
            mNsdHelper = null;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
    }

    //endregion

    private class AsyncTaskCheckStatus extends AsyncTask<Void, Integer, String> {

        private final String TAG = "async check status";
        private int sleepTime = 100;
        private int status;


        @Override
        protected void onPreExecute() {
            mStatusTextView.setText("Uninitialized");
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                while(true) {
                    if(isCancelled())
                        return null;
                    Thread.sleep(sleepTime);
                    if(isCancelled())
                        return null;
                    if (mNsdHelper != null) {
                        status = mNsdHelper.connection_status;
                        publishProgress(status); // Calls onProgressUpdate()
                        if (status == FinalVariables.NETWORK_RESOLVED_SERVICE) {
                            return "finish";
                        }
                    } else
                        return null;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                new MyLog(TAG, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                new MyLog(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... numbers) {
            if(!isConnectionEstablished)
                mStatusTextView.setText(getResources().getStringArray(R.array.network_statuses)[numbers[0]]);
        }

        @Override
        protected void onPostExecute(String result) {
            if(isCancelled())
                return;
            NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
            if (service != null) {
                new MyLog(TAG, "Connecting.");
                activity.connectToService(service);
                activity.connectionEstablished = true;
                isConnectionEstablished = true;
                meResolvedPeer = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initialSend();
                    }
                },300);
            } else
                new MyLog(TAG, "No service to connect to!");
        }
    }
}
