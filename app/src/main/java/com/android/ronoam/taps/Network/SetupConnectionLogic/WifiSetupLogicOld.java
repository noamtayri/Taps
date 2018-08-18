/*
package com.android.ronoam.taps.Network.SetupConnectionLogic;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

*/
/**
 * class that handles wifi setup connection
 * observes network incoming messages with {@link #model}
 * and set connection setup messages by order*//*

public class WifiSetupLogicOld {
    private final String TAG = "wifiSetupLogic";

    private GameActivity activity;
    private Handler mHandler, mAdapterHandler;
    private MyViewModel model;
    private final Observer<Message> messageInObserver;
    private boolean firstMessage, isConnectionEstablished, wordsCreated, meResolvedPeer;

    private NetworkConnection mConnection;
    private NsdServiceInfo myService, pressedService;
    private int msgCounter;
    private AsyncTask myAsyncConnect;

    private List<String> words;
    private boolean finishAsync = false;

    public WifiSetupLogicOld(GameActivity activity, Handler handler){
        this.activity = activity;
        this.mHandler = handler;
        int gameMode = activity.gameMode;
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        mConnection = activity.getConnection();

        setAdaptersHandler();
        firstMessage = true;
        isConnectionEstablished = false;
        wordsCreated = false;
        meResolvedPeer = false;
        msgCounter = 0;

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            messageInObserver = new Observer<Message>() {
                @Override
                public void onChanged(@Nullable Message message) {
                    if(message != null)
                        typeMessageReceiver(message);
                }
            };
        } else{
            messageInObserver = new Observer<Message>() {
                @Override
                public void onChanged(@Nullable Message message) {
                    if(message != null)
                        tapMessageReceiver(message);
                }
            };
        }
    }

    public void registerObserver(){
        model.getConnectionInMessages().observe(activity, messageInObserver);
    }

    public void removeObserver(){
        model.getConnectionInMessages().removeObserver(messageInObserver);
    }

    public void setMyService(NsdServiceInfo service) {
        this.myService = service;
    }

    */
/**
     * Type Wifi initiating protocol. share my name, send or receive {@link #words} and finish
     * @param msg the message received from {@link #model}
     *//*

    private void typeMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null && isConnectionEstablished) {
                mHandler.obtainMessage(FinalVariables.I_EXIT).sendToTarget();
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null && isConnectionEstablished) {
                new MyLog(TAG, "Opponent disconnected");
                mHandler.obtainMessage(FinalVariables.OPPONENT_EXIT).sendToTarget();
            } else {
                new MyLog(TAG, chatLine);
                if (firstMessage && !isConnectionEstablished && !meResolvedPeer) {
                    model.setOpponentName(chatLine);
                    activity.connectionEstablished = true;
                    isConnectionEstablished = true;
                    firstMessage = false;
                    initialSend();
                    Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                    message.obj = chatLine;
                    message.sendToTarget();
                } else if (!firstMessage && !wordsCreated && !meResolvedPeer) {
                    new MyLog(TAG, "received words");
                    new MyLog(TAG, chatLine);
                    //receive words
                    words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                    wordsCreated = true;
                    model.setWords(words);
                    mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
                } else if (meResolvedPeer) {
                    activity.connectionEstablished = true;
                    isConnectionEstablished = true;
                    Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                    message.obj = chatLine;
                    message.sendToTarget();
                    model.setOpponentName(chatLine);
                    //create and send words
                    new MyLog(TAG, "send words");
                    sendWords();
                    mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
                }
            }
        }
    }

    */
/**
     * Tap Wifi initiating protocol. share my name and finish
     * @param msg the message received from {@link #model}
     *//*

    private void tapMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if(chatLine != null)
            new MyLog(TAG, chatLine);

        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                if(isConnectionEstablished)
                    mHandler.obtainMessage(FinalVariables.I_EXIT).sendToTarget();
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                //new MyLog(TAG, "Opponent disconnected");
                if(isConnectionEstablished)
                    mHandler.obtainMessage(FinalVariables.OPPONENT_EXIT).sendToTarget();
            }
            else{
                msgCounter++;
                Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                message.obj = chatLine;
                message.sendToTarget();

                if(firstMessage && !isConnectionEstablished) {
                    model.setOpponentName(chatLine);
                    activity.connectionEstablished = true;
                    isConnectionEstablished = true;
                    firstMessage = false;
                    initialSend();
                }
                if(msgCounter == 1)
                    mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
            }
        }
    }

    */
/**
     * get the handler for handling {@link android.view.MotionEvent} events
     * @see #setAdaptersHandler()
     * @return the handler
     *//*

    public Handler getAdapterHandler(){
        return mAdapterHandler;
    }

    */
/**
     * set the handler for handling {@link android.view.MotionEvent} events
     * from {@link com.android.ronoam.taps.Fragments.WifiConnectionSetupFragment#mNsdServicesAdapter}
     *//*

    private void setAdaptersHandler(){
        mAdapterHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(isConnectionEstablished)
                    return true;
                switch (msg.what){
                    case FinalVariables.OPPONENT_PRESSED:
                        NsdServiceInfo device = (NsdServiceInfo)msg.obj;
                        opponentPressed(device);
                        break;
                    case FinalVariables.OPPONENT_RELEASED:
                    case FinalVariables.OPPONENT_CANCELED:
                        opponentReleased();
                        break;
                }
                return true;
            }
        });
    }

    private void opponentPressed(NsdServiceInfo device){
        pressedService = device;
        if(compareServices(device) < 0){
            meResolvedPeer = true;
            startAsyncConnect(device);
        } else
            startListeningForDevice(device);
    }

    private void opponentReleased() {
        if(compareServices(pressedService) < 0){
            finishAsync = true;
            if(myAsyncConnect != null)
                myAsyncConnect.cancel(false);
            myAsyncConnect = null;
            mConnection.getWifiConnection().tearDownClient();
        } else{
            mConnection.stopListening();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void startAsyncConnect(final NsdServiceInfo device) {
        final long duration = 1000;
        if(myAsyncConnect != null)
            myAsyncConnect.cancel(false);
        finishAsync = false;
        myAsyncConnect = new AsyncTask<Void, Boolean, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(duration);
                    if (!finishAsync && !isCancelled()) {
                        publishProgress(true);
                        Thread.sleep(200);
                        publishProgress(false);

                    }
                    else return null;
                    while(!isCancelled() && !activity.connectionEstablished){
                        Thread.sleep(duration * 3);
                        if (!finishAsync && !isCancelled() && !activity.connectionEstablished) {
                            publishProgress(true);
                            Thread.sleep(200);
                            publishProgress(false);
                        }
                        else break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Boolean... values) {
                //super.onProgressUpdate(values);
                if(!activity.connectionEstablished) {
                    if (values[0])
                        activity.connectToService(device);
                    if (!values[0])
                        initialSend();
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    */
/**
     * @param service the potentially last connected device
     *//*

    private void startListeningForDevice(NsdServiceInfo service){
        new MyLog(TAG, "start listening");
        mConnection.startListening(service);
    }

    */
/**
     * compare my service name name vs opponent's.
     * @return {@link String#compareTo(String)} value
     *//*

    private int compareServices(NsdServiceInfo device){
        return device.getServiceName().compareTo(myService.getServiceName());
    }

    */
/*
     * called after wifi opponent has resolved
     * call {@link GameActivity#connectToService(NsdServiceInfo)}
     * and after initiating the connection, send the first message {@link #initialSend} with small delay
     * @param service the service to connect to
     *//*

   */
/* public void resolvedService(NsdServiceInfo service){
        if (service != null) {
            activity.connectToService(service);
            meResolvedPeer = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initialSend();
                }
            },200);
        } else
            new MyLog(TAG, "No service to connect to!");
    }*//*


    */
/**
     * send my bluetooth name with {@link #model}
     *//*

    private void initialSend() {
        if(activity.getLocalPort() > -1) {
            String msg = Settings.Secure.getString(activity.getContentResolver(), "bluetooth_name");
            model.setOutMessage(msg);
        }
    }

    */
/**
     * create {@link #words} and send it to the opponent
     * also save it with {@link #model} for the next fragments to use.
     *//*

    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(activity, activity.language);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            new MyLog(TAG, joinedStr);
            model.setWords(words);
            model.setOutMessage(joinedStr);
        }
    }

    */
/**
     * Collapsing {@link #words} to one long String with commas ',' between every word
     * @param words the list to join
     * @return the merged string
     *//*

    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }
}*/
