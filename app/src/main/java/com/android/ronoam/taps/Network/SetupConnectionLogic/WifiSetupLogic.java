package com.android.ronoam.taps.Network.SetupConnectionLogic;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiSetupLogic {
    private final String TAG = "wifiSetupLogic";

    private GameActivity activity;
    private Handler mHandler;
    private MyViewModel model;
    private final Observer<Message> messageInObserver;
    private boolean firstMessage, isConnectionEstablished, wordsCreated, meResolvedPeer;

    private List<String> words;

    public WifiSetupLogic(GameActivity activity, Handler handler){
        this.activity = activity;
        this.mHandler = handler;
        int gameMode = activity.gameMode;
        model = ViewModelProviders.of(activity).get(MyViewModel.class);

        firstMessage = true;
        isConnectionEstablished = false;
        wordsCreated = false;
        meResolvedPeer = false;

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

    private void typeMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                mHandler.obtainMessage(FinalVariables.I_EXIT).sendToTarget();
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                new MyLog(TAG, "Opponent disconnected");
                mHandler.obtainMessage(FinalVariables.OPPONENT_EXIT).sendToTarget();
            }
            else if(firstMessage && !isConnectionEstablished) {
                model.setOpponentName(chatLine);
                activity.connectionEstablished = true;
                isConnectionEstablished = true;
                initialSend();
                firstMessage = false;
                Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                message.obj = chatLine;
                message.sendToTarget();
            }
            else if(!firstMessage && isConnectionEstablished && !wordsCreated) {
                new MyLog(TAG, "received words");
                new MyLog(TAG, chatLine);
                //receive words
                words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                wordsCreated = true;
                model.setWords(words);
                mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
            }
            else if(firstMessage && meResolvedPeer) {
                Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                message.obj = chatLine;
                message.sendToTarget();
                model.setOpponentName(chatLine);
                //create and send words
                sendWords();
                mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
            }
        }
    }

    private void tapMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");

        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                mHandler.obtainMessage(FinalVariables.I_EXIT).sendToTarget();
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                new MyLog(TAG, "Opponent disconnected");
                mHandler.obtainMessage(FinalVariables.OPPONENT_EXIT).sendToTarget();
            }
            else{
                //addChatLine(chatLine);
                Message message = mHandler.obtainMessage(FinalVariables.UPDATE_NETWORK_STATUS);
                message.obj = chatLine;
                message.sendToTarget();
                model.setOpponentName(chatLine);
                if(firstMessage && !isConnectionEstablished) {
                    activity.connectionEstablished = true;
                    isConnectionEstablished = true;
                    initialSend();
                    firstMessage = false;
                }
                mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
            }
        }
    }

    public void ResolvedService(NsdServiceInfo service){
        if (service != null) {
            activity.connectToService(service);
            activity.connectionEstablished = true;
            isConnectionEstablished = true;
            meResolvedPeer = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initialSend();
                }
            },200);
        } else
            new MyLog(TAG, "No service to connect to!");
    }

    private void initialSend() {
        if(activity.getLocalPort() > -1) {
            String msg = Settings.Secure.getString(activity.getContentResolver(), "bluetooth_name");
            model.setOutMessage(msg);
        }
    }

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

    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }
}