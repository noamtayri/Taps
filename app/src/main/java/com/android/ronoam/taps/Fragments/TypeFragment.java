package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.GameLogic.TypeLogic;
import com.android.ronoam.taps.Keyboard.MyCustomKeyboard;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.List;
import java.util.Random;

public class TypeFragment extends Fragment {

    private final String TAG = "Type Fragment";
    GameActivity activity;
    private MyViewModel model;

    private TypeLogic gameLogic;
    private MyCustomKeyboard mCustomKeyboard;
    private Handler mTypingHandler;
    private CountDownTimer countDownTimer;

    ConstraintLayout layout;
    EditText editText;
    TextView textViewTimer, textViewNextWord, textViewCounter, textViewOpponentCounter;

    private boolean gameFinished = false;
    private int gameMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        new MyLog(TAG, "Created View.");
        View view = inflater.inflate(R.layout.fragment_type, container, false);

        gameMode = activity.gameMode;

        //Bind UI
        layout = view.findViewById(R.id.type_layout);
        editText = view.findViewById(R.id.keyboard_game_edit_text);
        textViewTimer = view.findViewById(R.id.keyboard_game_timer);
        textViewNextWord = view.findViewById(R.id.keyboard_game_next_word);
        textViewCounter = view.findViewById(R.id.keyboard_game_counter);

        mCustomKeyboard = new MyCustomKeyboard(activity, R.id.keyboard_view, R.xml.heb_qwerty, view);
        mCustomKeyboard.registerEditText(editText);

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            textViewOpponentCounter = view.findViewById(R.id.keyboard_game_opponent_counter);
            setOnlineGame();
        }
        else{
            gameLogic = new TypeLogic(activity);
            editText.addTextChangedListener(gameLogic);
        }

        setHandler();
        setDesign();
        startGame();

        return view;
    }

    private void setOnlineGame() {
        textViewOpponentCounter.setVisibility(View.VISIBLE);

        List<String> words;
        words = model.getWords().getValue();

        gameLogic = new TypeLogic(words);
        editText.addTextChangedListener(gameLogic);

        model.getInMessage().observe(activity, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                doOpponentSpace(s);
            }
        });
    }

    private void setHandler() {
        mTypingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(msg.what == FinalVariables.MOVE_TO_NEXT_WORD) {
                    if(data.getBoolean(FinalVariables.IS_SUCCESSFUL_TYPE))
                        sendSuccessfulType();
                    String nextWord = data.getString(FinalVariables.NEXT_WORD);
                    int successes = data.getInt(FinalVariables.SUCCESS_WORDS);
                    textViewCounter.setText(String.valueOf(successes));
                    textViewNextWord.setText(nextWord);
                    textViewNextWord.setTextColor(Color.BLACK);
                    return true;
                }
                else if(msg.what == FinalVariables.UPDATE_NEXT_WORD){
                    boolean correctSoFar = data.getBoolean(FinalVariables.CORRECT_SO_FAR);
                    String text = data.getString(FinalVariables.NEXT_WORD);
                    String currentWord = data.getString(FinalVariables.CURRENT_WORD);
                    Spannable spannable = new SpannableString(currentWord);
                    if(text.length() == 0){
                        spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else if (correctSoFar) {
                        spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else{
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textViewNextWord.setText(spannable, EditText.BufferType.SPANNABLE);
                }
                return true;
            }
        });
    }

    private void setDesign() {
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");
        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-ExtraBold.ttf");
        textViewTimer.setTypeface(AssistantExtraBoldFont);
        textViewCounter.setTypeface(AssistantBoldFont);

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            textViewOpponentCounter.setTypeface(AssistantBoldFont);
    }

    private void startGame() {
        editText.performClick();
        Animation fadeIn = new AlphaAnimation(0.0f,1.0f);
        fadeIn.setDuration(FinalVariables.KEYBOARD_GAME_SHOW_UI);

        editText.startAnimation(fadeIn);
        textViewNextWord.startAnimation(fadeIn);
        editText.setVisibility(View.VISIBLE);
        textViewNextWord.setVisibility(View.VISIBLE);

        editText.setOnClickListener(null);
        editText.requestFocus();

        textViewNextWord.setText(gameLogic.getNextWord());

        setGameTimer();
    }

    private void setGameTimer(){
        textViewTimer.setText(String.valueOf(FinalVariables.KEYBOARD_GAME_TIME / 1000).concat(":00"));
        countDownTimer = new CountDownTimer(FinalVariables.KEYBOARD_GAME_TIME,FinalVariables.KEYBOARD_GAME_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time;
                time = millisUntilFinished / 1000 < 10 ? " " : "";
                time += String.valueOf(millisUntilFinished / 1000).concat(":");
                time += millisUntilFinished % 100 < 10 ? "0" : "";
                time += String.valueOf(millisUntilFinished % 100);

                textViewTimer.setText(time);
            }

            @Override
            public void onFinish() {
                finishGame();
            }
        }.start();
    }

    private void finishGame(){
        gameFinished = true;
        mCustomKeyboard.unRegisterEditText();
        finishAnimations();

        final Bundle resBundle = new Bundle();
        resBundle.putInt(FinalVariables.GAME_MODE, gameMode);
        resBundle.putFloat(FinalVariables.SCORE, gameLogic.getMyResults());

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            resBundle.putString(FinalVariables.WINNER, getWinner());
            //resBundle.putFloat(FinalVariables.SCORE_OPPONENT, gameLogic.getOpponentResults());
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                model.setFinish(new MyEntry(FinalVariables.NO_ERRORS, resBundle));
            }
        }, FinalVariables.KEYBOARD_GAME_HIDE_UI + 100);
    }

    private void finishAnimations(){
        Animation fadeOut = new AlphaAnimation(1.0f,0.0f);
        fadeOut.setDuration(FinalVariables.KEYBOARD_GAME_HIDE_UI);
        editText.startAnimation(fadeOut);
        textViewNextWord.startAnimation(fadeOut);
        editText.setVisibility(View.INVISIBLE);
        textViewNextWord.setVisibility(View.INVISIBLE);

        String zeros = " 0:00";
        textViewTimer.setText(zeros);
        mCustomKeyboard.hideCustomKeyboard();
        editText.setOnClickListener(null);
    }

    private String getWinner() {
        String winner = "";
        //new MyLog(TAG, "my result = " + gameLogic.getMyResults());
        //new MyLog(TAG, "opponent results = " + gameLogic.getOpponentResults());
        float myResults = gameLogic.getMyResults();
        float oppResults = gameLogic.getOpponentResults();
        if(myResults == oppResults)
            winner = "It's a tie";
        else {
            if (myResults > oppResults)
                winner = "You won";
            else if (myResults < oppResults)
                winner = "You lost";
            winner = winner.concat(" " + (int)myResults + ":" + (int)oppResults);
        }

        return winner;
    }

    private void sendSuccessfulType() {
        if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            String messageString = String.valueOf(gameLogic.getSuccessWordsCounter());
            model.setOutMessage(messageString);
        }
    }

    private void doOpponentSpace(String chatLine) {
        if(Integer.valueOf(chatLine) > 0) {
            gameLogic.doOpponentSpace();
            textViewOpponentCounter.setText(chatLine);
        }
        //changeKeyboard(false);
    }

    private void changeKeyboard(boolean resetToDefault){
        int resId;
        Resources resources = getResources();
        String[] keyboards = resources.getStringArray(R.array.heb_keyboards_options);
        if(resetToDefault){
            resId = resources.getIdentifier(keyboards[0], "xml", activity.getPackageName());
        }

        else {
            final int random = new Random().nextInt(keyboards.length) + 1;
            new MyLog(TAG, keyboards[random]);
            resId = resources.getIdentifier(keyboards[random], "xml", activity.getPackageName());
        }
        mCustomKeyboard.changeKeyboard(resId);
    }


    //region Fragment Overrides

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyLog(TAG, "Created.");

        activity = (GameActivity)getActivity();
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        gameLogic = new TypeLogic(activity);
    }

    @Override
    public void onResume() {
        new MyLog(TAG, "Resuming.");
        gameLogic.setNextWordHandler(mTypingHandler);
        super.onResume();
    }

    @Override
    public void onPause() {
        new MyLog(TAG, "Pausing");
        gameLogic.setNextWordHandler(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!gameFinished){
            countDownTimer.cancel();
        }
        new MyLog(TAG, "Being destroyed");
    }

    //endregion
}