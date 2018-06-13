package com.android.ronoam.taps.Keyboard;

import com.android.ronoam.taps.FinalVariables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WordsStorage {

    private Queue<String> nextWordsQueue;

    public WordsStorage(){
        nextWordsQueue = new LinkedList<>();
        fetchWords();
    }

    public Queue<String> getAllWords(){
        return nextWordsQueue;
    }

    private void fetchWords(){

        String str = FinalVariables.STATIC_SENTENCE1;
        str = str.replaceAll("[\\-\\+\\.\\^:,]"," ");
        str = str.replaceAll("[ ]{2,}"," ");

        String[] arr = str.split(" ");
        nextWordsQueue.addAll(Arrays.asList(arr));
    }

}
