package com.android.ronoam.taps.GameLogic;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Keyboard.KeyCodes;
import com.android.ronoam.taps.Keyboard.WordsLogic;

import java.util.List;

public class TypeLogic implements TextWatcher {

    private static final int MAX_FAIL_SPC = 2;
    private int successWordsCounter = 0, failSpaceCounter = 0;
    private int opponentCounter;
    private Handler mNextWordHandler;
    public WordsLogic wordsLogic;

    public TypeLogic(Activity host, int language){
        wordsLogic = new WordsLogic(host,(int) FinalVariables.TYPE_GAME_TIME /1000, language);
    }

    public TypeLogic(List<String> words){
        wordsLogic = new WordsLogic((int) FinalVariables.TYPE_GAME_TIME /1000, words);
        opponentCounter = 0;
    }

    public int getSuccessWordsCounter(){
        return successWordsCounter;
    }

    public void setNextWordHandler(Handler handler){
        mNextWordHandler = handler;
    }

    public float getMyResults(){
        return wordsLogic.calculateStatistics()[2];
    }

    public float getOpponentResults(){
        int timeSeconds = (int)FinalVariables.TYPE_GAME_TIME / 1000;
        return timeSeconds < 60 ? (60/timeSeconds) * opponentCounter : (timeSeconds/60) * opponentCounter;
    }

    /*public String getNextWord(){
        return wordsLogic.getNextWord();
    }*/

    public void doOpponentSpace(){
        opponentCounter++;
    }

    //region TextWatcher Overrides
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() == 0) {
            letterPressed("");
            return;
        }
        String str = editable.toString();
        int length = str.length();
        char lastChar = str.charAt(length - 1);

        switch ((int)lastChar) {
            case KeyCodes.SPC:
                spacePressed(editable);
                break;
            default:
                letterPressed(str);
        }

    }

    //endregion

    private void spacePressed(Editable editable){
        boolean successfulType = false;
        if(editable.length() > 1){
            String currentString = editable.toString();
            int successes = wordsLogic.typedSpace(currentString);
            //animate +1
            if(successes > successWordsCounter) {
                failSpaceCounter = 0;
                successWordsCounter++;
                successfulType = true;
            }
            else
                failSpaceCounter++;
        }
        else if(editable.toString().length() == 1)
            failSpaceCounter++;

        editable.clear();
        if(failSpaceCounter <= MAX_FAIL_SPC) {
            if (mNextWordHandler != null) {
                Message message = new Message();
                Bundle messageBundle = new Bundle();
                messageBundle.putString(FinalVariables.NEXT_WORD, wordsLogic.getNextWord());
                messageBundle.putInt(FinalVariables.SUCCESS_WORDS, successWordsCounter);
                messageBundle.putBoolean(FinalVariables.IS_SUCCESSFUL_TYPE, successfulType);
                message.what = FinalVariables.MOVE_TO_NEXT_WORD;

                message.setData(messageBundle);
                mNextWordHandler.sendMessage(message);
            }
        }
    }

    private void letterPressed(String typedString){
        String currentWord = wordsLogic.getCurrentWord();
        boolean correctSoFar = wordsLogic.typedChar(typedString);

        if(mNextWordHandler != null) {
            Message message = new Message();
            Bundle messageBundle = new Bundle();

            messageBundle.putString(FinalVariables.NEXT_WORD, typedString);
            messageBundle.putString(FinalVariables.CURRENT_WORD, currentWord);
            messageBundle.putBoolean(FinalVariables.CORRECT_SO_FAR, correctSoFar);
            message.what = FinalVariables.UPDATE_NEXT_WORD;

            message.setData(messageBundle);
            mNextWordHandler.sendMessage(message);
        }
    }
}
