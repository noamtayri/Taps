package com.android.ronoam.taps.Network;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.os.Message;

import com.android.ronoam.taps.Utils.MyEntry;

import java.util.List;

public class MyViewModel extends ViewModel {
    private final MutableLiveData<String> in = new MutableLiveData<>();
    private final MutableLiveData<String> out = new MutableLiveData<>();
    private final MutableLiveData<MyEntry> finish = new MutableLiveData<>();
    private final MutableLiveData<List<String>> words = new MutableLiveData<>();
    private final MutableLiveData<Message> connectionInMessages = new MutableLiveData<>();
    private final MutableLiveData<String> opponentName = new MutableLiveData<>();

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

    public void setFinish(MyEntry item) {
        finish.setValue(item);
    }

    public LiveData<MyEntry> getFinish() {
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
}
