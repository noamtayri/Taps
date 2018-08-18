package com.android.ronoam.taps.Network.SetupConnectionLogic;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Handler;
import android.os.Message;
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

/**
 * class that handles wifi setup connection
 * observes network incoming messages with {@link #model}
 * and set connection setup messages by order*/
public class StartGameLogic {
    private final String TAG = "startGameLogic";

    private GameActivity activity;
    private MyViewModel model;
    private final Observer<Message> messageInObserver;

    private NetworkConnection mConnection;
    private Handler mHandler;
    private int myChoice, opponentChoice;

    private boolean wordsCreated, choicesMatch;
    private List<String> words;

    public StartGameLogic(GameActivity activity, Handler handler){
        this.activity = activity;
        this.mHandler = handler;

        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        mConnection = activity.getConnection();

        messageInObserver = new Observer<Message>() {
            @Override
            public void onChanged(@Nullable Message message) {
                if(message != null)
                    messageReceiver(message);
            }
        };

        wordsCreated = false;
        myChoice = -1;
        opponentChoice = -1;
    }

    private void messageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if (msg.arg1 == FinalVariables.FROM_MYSELF) {
            if (chatLine == null) {
                mHandler.obtainMessage(FinalVariables.I_EXIT).sendToTarget();
                return;
            }
        }
        if (msg.arg1 == FinalVariables.FROM_OPPONENT) {
            if (chatLine == null) {
                new MyLog(TAG, "Opponent disconnected");
                mHandler.obtainMessage(FinalVariables.OPPONENT_EXIT).sendToTarget();
            }else{
                if(myChoice == opponentChoice && myChoice >= 0)
                    receiveWords(msg);
                else
                    setOpponentChoice(msg);
            }
        }
    }

    public void registerObserver(){
        model.getConnectionInMessages().observe(activity, messageInObserver);
    }

    public void removeObserver(){
        model.getConnectionInMessages().removeObserver(messageInObserver);
    }

    public void resetChoices(){
        myChoice = -1;
        opponentChoice = -1;
    }

    public void setMyChoice(int choice){
        myChoice = choice;
        model.setOutMessage(String.valueOf(choice));
        if(myChoice == opponentChoice){
            if(myChoice == FinalVariables.TYPE_PVP_ONLINE){
                handleTypeLogic();
            }
            else {
                mHandler.obtainMessage(FinalVariables.NO_ERRORS, myChoice, 0).sendToTarget();
                //activity.startOnlineGame(myChoice);
                removeObserver();
            }
        }
    }

    private void setOpponentChoice(Message msg) {
        String choiceStr = msg.getData().getString("msg");
        int choice = Integer.parseInt(choiceStr);
        if(choice >= 0)
            opponentChoice = choice;
        if(myChoice == opponentChoice){
            if(myChoice == FinalVariables.TYPE_PVP_ONLINE){
                handleTypeLogic();
            }
            else {
                mHandler.obtainMessage(FinalVariables.NO_ERRORS, myChoice, 0).sendToTarget();
                //activity.startOnlineGame(myChoice);
                removeObserver();
            }
        }
    }

    private void handleTypeLogic() {
        if(mConnection.compareNames() < 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendWords();
                    mHandler.obtainMessage(FinalVariables.NO_ERRORS, myChoice, 0).sendToTarget();
                    removeObserver();
                }
            }, 200);
        }
    }

    private void receiveWords(Message message) {
        new MyLog(TAG, "receive words");
        words = new ArrayList<>(Arrays.asList(message.getData().getString("msg").split(",")));
        wordsCreated = true;
        model.setWords(words);

        mHandler.obtainMessage(FinalVariables.NO_ERRORS, myChoice, 0).sendToTarget();
        removeObserver();
    }

    /**
     * create {@link #words} and send it to the opponent
     * also save it with {@link #model} for the next fragments to use.
     */
    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(activity, activity.language);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            //new MyLog(TAG, joinedStr);
            model.setWords(words);
            model.setOutMessage(joinedStr);
        }
    }

    /**
     * Collapsing {@link #words} to one long String with commas ',' between every word
     * @param words the list to join
     * @return the merged string
     */
    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }
}