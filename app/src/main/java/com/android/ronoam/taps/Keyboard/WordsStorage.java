package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WordsStorage {

    private Activity mHostActivity;

    private List<String> nextWordsQueue;

    private int arraySize, language;
    private String randomParagraph, topic;

    public WordsStorage(Activity host, int language) {
        mHostActivity = host;
        arraySize = 0;
        nextWordsQueue = new LinkedList<>();
        this.language = language;
        fetchWords();
        removeShortWords(3);
        if(language == FinalUtilsVariables.ENGLISH)
            toLowerCase();
    }

    private void toLowerCase() {
        List<String> temp = new ArrayList<>(nextWordsQueue);
        nextWordsQueue.clear();
        for (String word : temp){
                nextWordsQueue.add(word.toLowerCase());
        }
    }

    private void removeShortWords(int minSize) {
        List<String> temp = new ArrayList<>(nextWordsQueue);
        nextWordsQueue.clear();
        for (String word : temp){
            if(word.length() > minSize)
                nextWordsQueue.add(word);
        }
    }

    public List<String> getAllWords(){
        Collections.shuffle(nextWordsQueue);
        return nextWordsQueue;
    }

    private void splitString(){
        randomParagraph = randomParagraph.replaceAll("[\\-\\+\\.\\^:,]"," ");
        randomParagraph = randomParagraph.replaceAll("[ ]{2,}"," ");

        String[] arr = randomParagraph.split(" ");
        nextWordsQueue.addAll(Arrays.asList(arr));
    }

    public String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = mHostActivity.getAssets().open(mHostActivity.getResources().getStringArray(R.array.words_json_files)[language]);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            //ex.printStackTrace();
            new MyLog("WordsStorage", "error in loadJSONFromAsset");
            return null;
        }
        return json;
    }

    private void fetchWords(){
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            arraySize = Integer.parseInt(obj.getString("size"));
            JSONArray m_jArray = obj.getJSONArray("paragraphs");
            //ArrayList<HashMap<String, String>> paragraphsList = new ArrayList<HashMap<String, String>>();

            int randomIndex = (int) (Math.random() * (arraySize - 1));
            JSONObject jo_inside = m_jArray.getJSONObject(randomIndex);
            topic = jo_inside.getString("topic");
            randomParagraph = jo_inside.getString("paragraph");

            splitString();
        } catch (JSONException e) {
            //e.printStackTrace();
            new MyLog("WordsStorage", "error in fetch words");
        }
    }
}
