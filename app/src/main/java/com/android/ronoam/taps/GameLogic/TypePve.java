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

public class TypePve implements TextWatcher {

    private final int MAX_FAIL_SPC = 3;
    private int successWords = 0, failSpaceCounter = 0;
    private Handler mNextWordHandler;
    private WordsLogic wordsLogic;

    public TypePve(Activity host){
        wordsLogic = new WordsLogic(host,(int) FinalVariables.KEYBOARD_GAME_TIME /1000);
    }

    public TypePve(List<String> words){
        wordsLogic = new WordsLogic((int) FinalVariables.KEYBOARD_GAME_TIME /1000, words);
    }

    public void setNextWordHandler(Handler handler){
        mNextWordHandler = handler;
    }

    public float[] getResults(){
        return wordsLogic.calculateStatistics();
    }

    public String getNextWord(){
        return wordsLogic.getNextWord();
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
        if(editable.length() > 1){
            String currentString = editable.toString();
            int successes = wordsLogic.typedSpace(currentString);
            //animate +1
            if(successes > successWords) {
                failSpaceCounter = 0;
                successWords++;
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
                messageBundle.putInt(FinalVariables.SUCCESS_WORDS, successWords);
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
