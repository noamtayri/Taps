package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import com.android.ronoam.taps.Utils.MyLog;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//manage the game comparing words logic;
public class WordsLogic {

    private WordsStorage wordsStorage;

    private String currentWord, nextWord;
    private Queue<String> nextWords;
    private Activity mHostActivity;
    private int timeSeconds;//successes, fails, timeSeconds;
    private int correctCharStrokes, wrongCharStrokes, correctWordCounter, wrongWordCounter;

    public WordsLogic(int timeSeconds, List<String> wordsFromOpponent){
        Queue<String> words = new LinkedList<String>(wordsFromOpponent);
        nextWords = new LinkedList<>(words);

        this.timeSeconds = timeSeconds;

        if(!nextWords.isEmpty())
            nextWord = nextWords.poll();

        correctCharStrokes = 0;
        wrongCharStrokes = 0;
        correctWordCounter = 0;
        wrongWordCounter = 0;
    }

    public WordsLogic(Activity host, int timeSeconds, int language){
        wordsStorage = new WordsStorage(host, language);
        Queue<String> words = new LinkedList<String>(wordsStorage.getAllWords());
        new MyLog("words_logic", words.toString());
        nextWords = new LinkedList<>(words);

        mHostActivity = host;
        this.timeSeconds = timeSeconds;

        if(!nextWords.isEmpty())
            nextWord = nextWords.poll();

        correctCharStrokes = 0;
        wrongCharStrokes = 0;
        correctWordCounter = 0;
        wrongWordCounter = 0;
        //successes = 0;
        //fails = 0;
    }

    public String getNextWord(){
        currentWord = nextWord;
        if(!nextWords.isEmpty())
            nextWord = nextWords.poll();
        return currentWord;
    }

    public String getCurrentWord(){
        return currentWord;
    }

    /**
     *
     * @param count
     * @return the next 'count' words of the words list. if there isn't enough, return the rest of words till the end
     * unused method
     */
    public List<String> nextWords(int count){
        List<String> list = new LinkedList<>();
        if(count <= 0)
            return null;
        for(int i = 0; i < count && !nextWords.isEmpty(); i++)
            list.add(nextWords.poll());

        return list;
    }


    /**
     *
     * @param typedString the current typed string
     * @return if the inserted text is a starting substring of currentWord return true, else false
     */
    public boolean typedChar(String typedString) {
        boolean match = currentWord.startsWith(typedString);
        if (match)
            correctCharStrokes++;
        else
            wrongCharStrokes++;
        return match;
    }

    public int typedSpace(String typedString) {
        if(currentWord.equals(typedString.substring(0, typedString.length() - 1))) {
            //successes++;
            correctWordCounter++;
        }
        else {
            //fails++;
            wrongWordCounter++;
        }
        return correctWordCounter;
    }

    public float[] calculateStatistics(){
        float[] results = new float[3];
        /*
        words/min
        chars/min
        accuracy
         */
        float accuracy = (float)correctWordCounter / (float)(correctWordCounter + wrongWordCounter) * 100f;
        float wordPerMin = timeSeconds < 60 ? (60/timeSeconds) * correctWordCounter : (timeSeconds/60) * correctWordCounter;
        float charsPerMin = timeSeconds < 60 ? (60/timeSeconds) * correctWordCounter : (timeSeconds/60) * correctWordCounter;;

        results[0] = accuracy;
        results[1] = charsPerMin;
        results[2] = wordPerMin;
        return results;
    }


}
