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
import android.widget.ImageView;
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

public class TypeFragment extends Fragment {

    private final String TAG = "Type Fragment";
    GameActivity activity;
    private MyViewModel model;
    private Observer<String> stringObserver;

    private TypeLogic gameLogic;
    private MyCustomKeyboard mCustomKeyboard;
    private Handler mTypingHandler;
    private CountDownTimer countDownTimer, countDownExtraTimer;

    EditText editText;
    TextView textViewTimer, textViewNextWord, textViewCounter, textViewOpponentCounter ,textViewExtraTimer;
    ImageView imageViewExtraTimer;

    private boolean gameFinished = false, keyboardChanged, changeOpponentKeyboard,
            isMixLocked, isEraseLocked;
    private int gameMode;

    final Animation fadeIn = new AlphaAnimation(0.0f, 0.9f);
    final Animation fadeOut = new AlphaAnimation(0.9f, 0.0f);

    ImageView erase, mix;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        gameMode = activity.gameMode;

        //Bind UI
        editText = view.findViewById(R.id.type_edit_text);
        textViewTimer = view.findViewById(R.id.type_game_timer);
        textViewNextWord = view.findViewById(R.id.type_next_word);
        textViewCounter = view.findViewById(R.id.type_counter);

        //Set the keyboard
        String keyboardResName = activity.getResources().getStringArray(R.array.default_keyboards)[activity.language];
        int keyboardResId = getResources().getIdentifier(keyboardResName, "xml", activity.getPackageName());
        mCustomKeyboard = new MyCustomKeyboard(activity, R.id.keyboard_view, keyboardResId, view);
        mCustomKeyboard.registerEditText(editText);

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE) {
            textViewOpponentCounter = view.findViewById(R.id.type_opponent_counter);
            textViewExtraTimer = view.findViewById(R.id.type_extra_timer);
            imageViewExtraTimer = view.findViewById(R.id.type_imageView_extra_timer);
            erase = view.findViewById(R.id.type_imageView_erase);
            mix = view.findViewById(R.id.type_imageView_mix);
            setOnlineGame();
        }
        else{
            gameLogic = new TypeLogic(activity, activity.language);
            editText.addTextChangedListener(gameLogic);
        }

        setHandler();
        setDesign();
        startGame();
    }

    private void setOnlineGame() {
        textViewOpponentCounter.setVisibility(View.VISIBLE);

        enableDisturbButtons();
        lockDisturbButtons();
        setEraseAndMixListeners();

        List<String> words;
        words = model.getWords().getValue();

        gameLogic = new TypeLogic(words);
        editText.addTextChangedListener(gameLogic);

        stringObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                doOpponentSpace(s);
            }
        };
    }

    private void setHandler() {
        mTypingHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
                    checkForDisturbing();
                if(msg.what == FinalVariables.MOVE_TO_NEXT_WORD) {
                    if(data.getBoolean(FinalVariables.IS_SUCCESSFUL_TYPE)) {
                        sendSuccessfulType();
                        int successes = data.getInt(FinalVariables.SUCCESS_WORDS);
                        textViewCounter.setText(String.valueOf(successes));
                        textViewCounter.setScaleX(textViewCounter.getScaleX() * 7);
                        textViewCounter.setScaleY(textViewCounter.getScaleY() * 7);
                        textViewCounter.animate().scaleXBy(-6.0f).scaleYBy(-6.0f).setDuration(400);
                    }
                    final String nextWord = data.getString(FinalVariables.NEXT_WORD);

                    textViewNextWord.setAlpha(0);
                    textViewNextWord.setText(nextWord);
                    textViewNextWord.setTextColor(Color.BLACK);
                    textViewNextWord.animate().alpha(1f).setDuration(80);

                    /*
                    Runnable changeWord = new Runnable() {
                        @Override
                        public void run() {
                            textViewNextWord.setText(nextWord);
                            textViewNextWord.setTextColor(Color.BLACK);
                            textViewNextWord.animate().alpha(1f).setDuration(80);
                        }
                    };
                    textViewNextWord.animate().alpha(0f).setDuration(80).withEndAction(changeWord);
                    */
                    return true;
                }
                else if(msg.what == FinalVariables.UPDATE_NEXT_WORD){
                    boolean correctSoFar = data.getBoolean(FinalVariables.CORRECT_SO_FAR);
                    String text = data.getString(FinalVariables.NEXT_WORD);
                    String currentWord = data.getString(FinalVariables.CURRENT_WORD);
                    Spannable spannable = new SpannableString(currentWord);
                    if(text == null || currentWord == null)
                        return true;
                    if(text.length() == 0){
                        spannable.setSpan(new ForegroundColorSpan(Color.BLACK), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else if (correctSoFar) {
                        //spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#30EB3D")), 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    else{
                        //spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#C00505")), 0, currentWord.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        textViewTimer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            textViewOpponentCounter.setTypeface(AssistantBoldFont);
            textViewExtraTimer.setTypeface(AssistantBoldFont);
        }

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

        textViewNextWord.setText(gameLogic.wordsLogic.getNextWord());

        setGameTimer();
    }

    private void setGameTimer(){
        textViewTimer.setText(String.valueOf(FinalVariables.TYPE_GAME_TIME / 1000).concat(":00"));
        countDownTimer = new CountDownTimer(FinalVariables.TYPE_GAME_TIME,FinalVariables.KEYBOARD_GAME_INTERVAL) {
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
        if(keyboardChanged){
            countDownExtraTimer.cancel();
            keyboardChanged = false;
        }

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
        }, FinalVariables.KEYBOARD_GAME_HIDE_UI);
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

    private void doOpponentSpace(final String chatLine) {
        int num = Integer.valueOf(chatLine);
        if(num  > 0) {
            gameLogic.doOpponentSpace();
            textViewOpponentCounter.setText(chatLine);

            textViewOpponentCounter.setScaleX(textViewOpponentCounter.getScaleX() * 7);
            textViewOpponentCounter.setScaleY(textViewOpponentCounter.getScaleY() * 7);
            textViewOpponentCounter.animate().scaleXBy(-6.0f).scaleYBy(-6.0f).setDuration(500);
        }
        else if(!keyboardChanged){
            keyboardChanged = true;
            if(num == FinalVariables.ERASE_KEYBOARD) {
                changeKeyboard(FinalVariables.ERASE_KEYBOARD);
            }
            else if(num == FinalVariables.MIX_KEYBOARD)
                changeKeyboard(FinalVariables.MIX_KEYBOARD);

            setExtraTimer(); //reset keyboard after 5 seconds
        }
    }

    private void setExtraTimer(){
        imageViewExtraTimer.startAnimation(fadeIn);
        textViewExtraTimer.startAnimation(fadeIn);
        imageViewExtraTimer.setVisibility(View.VISIBLE);
        textViewExtraTimer.setVisibility(View.VISIBLE);
        textViewExtraTimer.setText(String.valueOf(FinalVariables.TYPE_ONLINE_EXTRA_TIMER / 1000));
        countDownExtraTimer = new CountDownTimer(FinalVariables.TYPE_ONLINE_EXTRA_TIMER, 500) {
            @Override
            public void onTick(long millisUntilFinished) {
                String time;
                time = String.valueOf(millisUntilFinished / 1000);

                textViewExtraTimer.setText(time);
            }

            @Override
            public void onFinish() {
                String str = "0";
                textViewExtraTimer.setText(str);
                keyboardChanged = false;
                imageViewExtraTimer.startAnimation(fadeOut);
                textViewExtraTimer.startAnimation(fadeOut);
                imageViewExtraTimer.setVisibility(View.INVISIBLE);
                textViewExtraTimer.setVisibility(View.INVISIBLE);
                changeKeyboard(FinalVariables.DEFAULT_KEYBOARD);
            }
        }.start();
    }

    private void changeKeyboard(int changeMode){
        String keyboard;
        int lang = activity.language;
        Resources resources = getResources();
        switch (changeMode){
            case FinalVariables.DEFAULT_KEYBOARD:
                keyboard = resources.getStringArray(R.array.default_keyboards)[lang];
                break;
            case FinalVariables.ERASE_KEYBOARD:
                keyboard = resources.getStringArray(R.array.erased_keyboards)[lang];
                break;
            case FinalVariables.MIX_KEYBOARD:
                keyboard = resources.getStringArray(R.array.mixed_keyboards)[lang];
                break;
            default:
                keyboard = resources.getStringArray(R.array.default_keyboards)[lang];
        }

        int resId;
        resId = resources.getIdentifier(keyboard, "xml", activity.getPackageName());
        //String[] keyboards = resources.getStringArray(R.array.heb_keyboards_options);
        /*if(resetToDefault){
            resId = resources.getIdentifier(keyboard, "xml", activity.getPackageName());
        }

        else {
            final int random = new Random().nextInt(keyboards.length) + 1;
            new MyLog(TAG, keyboards[random]);
            resId = resources.getIdentifier(keyboards[random], "xml", activity.getPackageName());
        }*/
        mCustomKeyboard.changeKeyboard(resId);
    }

    //region Fragment Overrides

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyLog(TAG, "Created.");

        fadeIn.setDuration(400);
        fadeOut.setDuration(700);

        activity = (GameActivity)getActivity();
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
    public void onStart() {
        super.onStart();
        if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            model.getInMessage().observe(activity, stringObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            model.getInMessage().removeObserver(stringObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!gameFinished){
            countDownTimer.cancel();
        }
        if(keyboardChanged)
            countDownExtraTimer.cancel();
        new MyLog(TAG, "Being destroyed");
    }

    //endregion

    //region disturb methods

    private void checkForDisturbing(){
        if(gameLogic.wordsLogic.correctWordCounterInARow >= FinalVariables.WORDS_IN_A_ROW){
            if(!changeOpponentKeyboard && isEraseLocked) {
                unlockErase();
                isEraseLocked = false;
            }
        }else if(!isEraseLocked){
            lockErase();
            isEraseLocked = true;
        }

        if(gameLogic.wordsLogic.correctCharStrokesInARow >= FinalVariables.CHAR_STROKES_IN_A_ROW) {
            if(!changeOpponentKeyboard && isMixLocked) {
                unlockMix();
                isMixLocked = false;
            }
        }else if(!isMixLocked) {
            lockMix();
            isMixLocked = true;
        }
    }
    private void setEraseAndMixListeners(){
        erase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameLogic.wordsLogic.correctWordCounterInARow -= FinalVariables.WORDS_IN_A_ROW;
                disturbOpponent(FinalVariables.ERASE_KEYBOARD);
                lockErase();
            }
        });

        mix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameLogic.wordsLogic.correctCharStrokesInARow -= FinalVariables.CHAR_STROKES_IN_A_ROW;
                disturbOpponent(FinalVariables.MIX_KEYBOARD);
                blockDisturbButtons();
            }
        });
    }

    public void disturbOpponent(int disturbCode){
        model.setOutMessage(String.valueOf(disturbCode));
        lockDisturbButtons();
        changeOpponentKeyboard = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                changeOpponentKeyboard = false;
                unblockAfterDelay();
            }
        }, FinalVariables.TYPE_ONLINE_EXTRA_TIMER);
    }

    private void enableDisturbButtons(){
        erase.setVisibility(View.VISIBLE);
        mix.setVisibility(View.VISIBLE);
    }

    private void lockDisturbButtons(){
        lockMix();
        lockErase();
        isMixLocked = true;
        isEraseLocked = true;
    }

    private void unlockErase(){
        erase.setImageResource(R.drawable.erase_c);
        erase.setAlpha(0.2f);
        erase.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        erase.setEnabled(true);
    }

    private void lockErase(){
        erase.setImageResource(R.drawable.erase_w);
        erase.setAlpha(0.2f);
        erase.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        erase.setEnabled(false);
    }

    private void unlockMix(){
        mix.setImageResource(R.drawable.mix_c);
        mix.setAlpha(0.2f);
        mix.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        mix.setEnabled(true);
    }

    private void lockMix(){
        mix.setImageResource(R.drawable.mix_w);
        mix.setAlpha(0.2f);
        mix.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        mix.setEnabled(false);
    }

    private void blockDisturbButtons(){
        mix.setImageResource(R.drawable.mix_y);
        erase.setImageResource(R.drawable.erase_y);
        mix.setAlpha(0.2f);
        erase.setAlpha(0.2f);
        mix.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        erase.animate().alpha(0.6f).setDuration(FinalVariables.HOME_SHOW_UI);
        mix.setEnabled(false);
        erase.setEnabled(false);
    }

    private void unblockAfterDelay(){
        if(gameLogic.wordsLogic.correctWordCounterInARow >= FinalVariables.WORDS_IN_A_ROW){
            unlockErase();
            isEraseLocked = false;
        }else{
            lockErase();
            isEraseLocked = true;
        }
        if(gameLogic.wordsLogic.correctCharStrokesInARow >= FinalVariables.CHAR_STROKES_IN_A_ROW) {
            unlockMix();
            isMixLocked = false;
        }else{
            lockMix();
            isMixLocked = true;
        }
    }
    //endregion disturb methods
}
