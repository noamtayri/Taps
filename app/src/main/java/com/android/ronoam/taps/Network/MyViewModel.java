package com.android.ronoam.taps.Network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Bundle;
import android.os.Message;

import java.util.List;
import java.util.Map.Entry;

public class MyViewModel extends ViewModel {
    //private final MutableLiveData<Item> selected = new MutableLiveData<Item>();
    private final MutableLiveData<String> in = new MutableLiveData<>();
    private final MutableLiveData<String> out = new MutableLiveData<>();
    private final MutableLiveData<Entry<Integer, Bundle>> finish = new MutableLiveData<>();
    private final MutableLiveData<List<String>> words = new MutableLiveData<>();
    private final MutableLiveData<Message> connectionInMessages = new MutableLiveData<>();
    private final MutableLiveData<String> opponentName = new MutableLiveData<>();

    public int gameMode;
    public boolean firstMessage = true;

    public void setOutMessage(String item) {
        out.setValue(item);
    }

    public LiveData<String> getOutMessage() {
        return out;
    }

    public void setInMessage(String item) {
        in.setValue(item);
    }

    public LiveData<String> getInMessage() {
        return in;
    }

    public void setFinish(Entry<Integer, Bundle> item) {
        finish.setValue(item);
    }

    public LiveData<Entry<Integer, Bundle>> getFinish() {
        return finish;
    }

    public void setWords(List<String> wordsMsg) {
        words.setValue(wordsMsg);
    }

    public LiveData<List<String>> getWords() {
        return words;
    }

    public void setConnectionInMessages(Message msg){
        connectionInMessages.setValue(msg);
    }

    public LiveData<Message> getConnectionInMessages(){
        return connectionInMessages;
    }

    public void setOpponentName(String msg){
        opponentName.setValue(msg);
    }

    public LiveData<String> getOpponentName(){
        return opponentName;
    }


    /*public void select(Item item) {
        selected.setValue(item);
    }

    public LiveData<Item> getSelected() {
        return selected;
    }*/
}
