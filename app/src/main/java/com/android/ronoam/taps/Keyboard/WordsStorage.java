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

    private void fetchWords(){

        String str = FinalVariables.STATIC_SENTENCE1;
        String[] arr = str.split("./'?:-");
        nextWordsQueue.addAll(Arrays.asList(arr));
    }

    public String nextWord(){
        return nextWordsQueue.poll();
    }

    public List<String> nextWords(int count){
        List<String> list = new ArrayList<>();
        if(count <= 0)
            return null;
        for(int i = 0; i < count; i++){
            if(nextWordsQueue.isEmpty())
                break;
            list.add(nextWordsQueue.poll());
        }
        return list;
    }

}
