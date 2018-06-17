package com.android.ronoam.taps.Keyboard;

import android.app.Activity;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Utils.MyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class WordsStorage {

    private Activity mHostActivity;

    private List<String> nextWordsQueue;

    private int arraySize;
    private String randomParagraph, topic;

    public WordsStorage(Activity host) throws JSONException {
        mHostActivity = host;
        arraySize = 0;
        nextWordsQueue = new LinkedList<>();
        fetchWords();
    }

    public List<String> getAllWords(){
        Collections.shuffle(nextWordsQueue);
        return nextWordsQueue;
    }

    private void splitString(){
        //String str = FinalVariables.STATIC_SENTENCE1;
        randomParagraph = randomParagraph.replaceAll("[\\-\\+\\.\\^:,]"," ");
        randomParagraph = randomParagraph.replaceAll("[ ]{2,}"," ");

        String[] arr = randomParagraph.split(" ");
        nextWordsQueue.addAll(Arrays.asList(arr));
        //return nextWordsQueue;
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = mHostActivity.getAssets().open("words.json");
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

            HashMap<String, String> m_li;

            int randomIndex = (int) (Math.random() * (arraySize - 1));
            //new MyLog("WordsStorage", "random index = " + randomIndex);
            JSONObject jo_inside = m_jArray.getJSONObject(randomIndex);
            topic = jo_inside.getString("topic");
            randomParagraph = jo_inside.getString("paragraph");
            //new MyLog("WordsStorage", randomParagraph);

            splitString();

            //for getting the whole array
            /*for (int i = 0; i < m_jArray.length(); i++) {
                JSONObject jo_inside = m_jArray.getJSONObject(i);
                //Log.d("Details-->", jo_inside.getString("formule"));
                String topic = jo_inside.getString("topic");
                String paragraph = jo_inside.getString("paragraph");

                //Add your values in your `ArrayList` as below:
                m_li = new HashMap<String, String>();
                m_li.put("topic", topic);
                m_li.put("paragraph", paragraph);

                paragraphsList.add(m_li);
            }*/
        } catch (JSONException e) {
            //e.printStackTrace();
            new MyLog("WordsStorage", "error in fetch words");
        }
    }
}
