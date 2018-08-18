package com.android.ronoam.taps.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.MyApplication;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;
import com.android.ronoam.taps.Utils.SharedPreferencesHandler;

public class HomeFragment extends Fragment implements View.OnClickListener{

    private final String TAG = "HomeFragment";
    private ImageButton tap, type, tapPve, tapPvp, tapPvpOnline, typePve, typePvpOnline;
    private ImageView heb, eng;
    public ImageView infoBtn;

    private TextView highScoreTitle, highScoreTapTitle, highScoreTypeTitle;
    private TextView highScoreTap, highScoreType;
    private TextView winScore;

    private int highTaps, highTypes;
    private int score;
    public int language;
    private String winner;
    private int screenWidth;
    private boolean isRtl, isTapClicked, isTypeClicked;

    final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);

    GameActivity activity;
    MyViewModel model;
    private boolean triedExit, isFirstTime = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        new MyLog(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new MyLog(TAG, "onViewCreated");

        tap = view.findViewById(R.id.imageButton_tap);
        type = view.findViewById(R.id.imageButton_type);

        heb = view.findViewById(R.id.imageView_heb);
        eng = view.findViewById(R.id.imageView_eng);
        infoBtn = view.findViewById(R.id.button_information);

        tapPve = view.findViewById(R.id.button_tap_pve);
        tapPvp = view.findViewById(R.id.button_tap_pvp);
        tapPvpOnline = view.findViewById(R.id.button_tap_pvp_online);
        typePve = view.findViewById(R.id.button_type_pve);
        typePvpOnline = view.findViewById(R.id.button_type_pvp_online);

        winScore = view.findViewById(R.id.textView_winner_score);

        highScoreTap = view.findViewById(R.id.textView_high_tap_score);
        highScoreType = view.findViewById(R.id.textView_high_type_score);
        highScoreTitle = view.findViewById(R.id.textView_high_score_title);
        highScoreTapTitle = view.findViewById(R.id.textView_high_tap_title);
        highScoreTypeTitle = view.findViewById(R.id.textView_high_type_title);

        bindClicks();
        setDesign();
        if(isFirstTime) {
            isFirstTime = false;
            determineLayoutDirection(tap);

            loadFromSharedPreferences();

            if (language == FinalUtilsVariables.HEBREW) {
                heb.setImageResource(R.drawable.heb_c_c);
            } else {
                eng.setImageResource(R.drawable.eng_c_c);
            }

            showHighScores();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;

            model = ViewModelProviders.of(activity).get(MyViewModel.class);
        }
    }

    private void bindClicks(){
        tap.setOnClickListener(this);
        type.setOnClickListener(this);

        tapPve.setOnClickListener(this);
        tapPvp.setOnClickListener(this);
        tapPvpOnline.setOnClickListener(this);
        typePve.setOnClickListener(this);
        typePvpOnline.setOnClickListener(this);

        eng.setOnClickListener(this);
        heb.setOnClickListener(this);

        infoBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageButton_tap:
                tapClick(v);
                break;
            case R.id.imageButton_type:
                typeClick(v);
                break;
            case R.id.button_tap_pve:
                tapPveClick(v);
                break;
            case R.id.button_tap_pvp:
                tapPvpClick(v);
                break;
            case R.id.button_tap_pvp_online:
                tapPvpOnlineClick(v);
                break;
            case R.id.button_type_pve:
                typePveClick(v);
                break;
            case R.id.button_type_pvp_online:
                typePvpOnlineClick(v);
                break;
            case R.id.imageView_eng:
                engClick(v);
                break;
            case R.id.imageView_heb:
                hebClick(v);
                break;
            case R.id.button_information:
                infoClick(v);
                break;
        }
    }

    public void infoClick(View v){
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new DialogInfoFragment();
        newFragment.show(ft, "dialog");
    }

    public void hebClick(View v){
        if(language != FinalUtilsVariables.HEBREW){
            language = FinalUtilsVariables.HEBREW;
            eng.setImageResource(R.drawable.eng_c_w);
            //eng.setImageResource(R.drawable.eng_r_w);
            eng.setAlpha(0.2f);
            eng.animate().alpha(0.5f).setDuration(FinalVariables.HOME_SHOW_UI);
            heb.setImageResource(R.drawable.heb_c_c);
            //heb.setImageResource(R.drawable.israel_flag_icon);
            heb.setAlpha(0.2f);
            heb.animate().alpha(0.5f).setDuration(FinalVariables.HOME_SHOW_UI);
            saveToSharedPreferences(FinalVariables.LANGUAGE, FinalUtilsVariables.HEBREW);
        }
    }

    public void engClick(View v){
        if(language != FinalUtilsVariables.ENGLISH){
            language = FinalUtilsVariables.ENGLISH;
            heb.setImageResource(R.drawable.heb_c_w);
            //heb.setImageResource(R.drawable.heb_r_w);
            heb.setAlpha(0.2f);
            heb.animate().alpha(0.5f).setDuration(FinalVariables.HOME_SHOW_UI);
            eng.setImageResource(R.drawable.eng_c_c);
            //eng.setImageResource(R.drawable.united_states_flag_icon);
            eng.setAlpha(0.2f);
            eng.animate().alpha(0.5f).setDuration(FinalVariables.HOME_SHOW_UI);
            saveToSharedPreferences(FinalVariables.LANGUAGE, FinalUtilsVariables.ENGLISH);
        }
    }

    private void setDesign() {
        //using imported font
        Typeface AssistantBoldFont = Typeface.createFromAsset(activity.getAssets(),  "fonts/Assistant-Bold.ttf");

        highScoreTitle.setTypeface(AssistantBoldFont);
        highScoreTap.setTypeface(AssistantBoldFont);
        highScoreType.setTypeface(AssistantBoldFont);
        highScoreTapTitle.setTypeface(AssistantBoldFont);
        highScoreTypeTitle.setTypeface(AssistantBoldFont);
        winScore.setTypeface(AssistantBoldFont);
    }


    //region onClicks

    public void tapClick(View v){
        if (tapPve.getVisibility() == View.VISIBLE){
            isTapClicked = false;
            tap.setEnabled(false);

            if(!isRtl)
                tap.animate().xBy((screenWidth/2 - tap.getWidth() - tap.getWidth()/10) * -1).setDuration(FinalVariables.HOME_HIDE_UI);
            else
                tap.animate().xBy(screenWidth/2 - tap.getWidth() - tap.getWidth()/10).setDuration(FinalVariables.HOME_HIDE_UI);

            type.startAnimation(fadeIn);
            type.setVisibility(View.VISIBLE);

            tapPve.startAnimation(fadeOut);
            tapPvp.startAnimation(fadeOut);
            tapPvpOnline.startAnimation(fadeOut);

            tapPve.setVisibility(View.INVISIBLE);
            tapPvp.setVisibility(View.INVISIBLE);
            tapPvpOnline.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tap.setEnabled(true);
                    type.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }else {
            isTapClicked = true;
            tap.setEnabled(false);
            type.setEnabled(false);

            if(!isRtl)
                tap.animate().xBy(screenWidth / 2 - tap.getWidth() - tap.getWidth() / 10).setDuration(FinalVariables.HOME_SHOW_UI);
            else
                tap.animate().xBy((screenWidth / 2 - tap.getWidth() - tap.getWidth() / 10) * -1).setDuration(FinalVariables.HOME_SHOW_UI);
            type.startAnimation(fadeOut);
            type.setVisibility(View.INVISIBLE);

            tapPve.startAnimation(fadeIn);
            tapPvp.startAnimation(fadeIn);
            tapPvpOnline.startAnimation(fadeIn);

            tapPve.setVisibility(View.VISIBLE);
            tapPvp.setVisibility(View.VISIBLE);
            tapPvpOnline.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tap.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }
    }

    public void typeClick(View v){
        if (typePve.getVisibility() == View.VISIBLE){
            isTypeClicked = false;
            type.setEnabled(false);

            if(!isRtl)
                type.animate().xBy(screenWidth/2 - type.getWidth() - type.getWidth()/10).setDuration(FinalVariables.HOME_SHOW_UI);
            else
                type.animate().xBy((screenWidth/2 - type.getWidth() - type.getWidth()/10) * -1).setDuration(FinalVariables.HOME_SHOW_UI);

            tap.startAnimation(fadeIn);
            tap.setVisibility(View.VISIBLE);

            typePve.startAnimation(fadeOut);
            typePvpOnline.startAnimation(fadeOut);

            heb.startAnimation(fadeOut);
            eng.startAnimation(fadeOut);
            infoBtn.startAnimation(fadeOut);
            heb.setVisibility(View.INVISIBLE);
            eng.setVisibility(View.INVISIBLE);
            infoBtn.setVisibility(View.INVISIBLE);

            typePve.setVisibility(View.INVISIBLE);
            typePvpOnline.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    type.setEnabled(true);
                    tap.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }else {
            isTypeClicked = true;
            type.setEnabled(false);
            tap.setEnabled(false);

            if(!isRtl)
                type.animate().xBy((screenWidth / 2 - tap.getWidth() - type.getWidth() / 10) * -1).setDuration(FinalVariables.HOME_HIDE_UI);
            else
                type.animate().xBy(screenWidth / 2 - tap.getWidth() - type.getWidth() / 10).setDuration(FinalVariables.HOME_HIDE_UI);

            tap.startAnimation(fadeOut);
            tap.setVisibility(View.INVISIBLE);

            typePve.startAnimation(fadeIn);
            typePvpOnline.startAnimation(fadeIn);

            heb.startAnimation(fadeIn);
            eng.startAnimation(fadeIn);
            infoBtn.startAnimation(fadeIn);
            heb.setVisibility(View.VISIBLE);
            eng.setVisibility(View.VISIBLE);
            infoBtn.setVisibility(View.VISIBLE);

            typePve.setVisibility(View.VISIBLE);
            typePvpOnline.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    type.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }
    }

    public void tapPveClick(View v){
        activity.setupGameFragment(FinalVariables.TAP_PVE);
        activity.moveToNextFragment(null);
    }

    public void tapPvpClick(View v){
        activity.setupGameFragment(FinalVariables.TAP_PVP);
        activity.moveToNextFragment(null);
    }

    public void tapPvpOnlineClick(View v){
        activity.startTapOnline();
        /*activity.setupGameFragment(FinalVariables.TAP_PVP_ONLINE);
        activity.moveToNextFragment(null);*/
    }

    public void typePveClick(View v){
        activity.language = language;
        ((MyApplication)activity.getApplication()).setGameLanguage(language);
        activity.setupGameFragment(FinalVariables.TYPE_PVE);
        activity.moveToNextFragment(null);
    }

    public void typePvpOnlineClick(View v){
        activity.language = language;
        ((MyApplication)activity.getApplication()).setGameLanguage(language);

        activity.startTypeOnline();
        /*activity.setupGameFragment(FinalVariables.TYPE_PVP_ONLINE);
        activity.moveToNextFragment(null);*/
    }

    //endregion

    private void loadFromSharedPreferences() {
        highTaps = SharedPreferencesHandler.getInt(activity, FinalVariables.HIGH_SCORE_TAP_KEY);
        highTypes = SharedPreferencesHandler.getInt(activity, FinalVariables.HIGH_SCORE_TYPE_KEY);
        language = SharedPreferencesHandler.getInt(activity, FinalVariables.LANGUAGE_NAME);
    }

    private void showHighScores(){
        highScoreTap.setText(String.valueOf(highTaps));
        highScoreType.setText(String.valueOf(highTypes));
    }

    private void saveToSharedPreferences(int key, int value){
        switch(key){
            case FinalVariables.TAP_PVE:
                highTaps = value;
                SharedPreferencesHandler.writeInt(activity, FinalVariables.HIGH_SCORE_TAP_KEY, value);
                break;
            case FinalVariables.TYPE_PVE:
                highTypes = value;
                SharedPreferencesHandler.writeInt(activity, FinalVariables.HIGH_SCORE_TYPE_KEY, value);
                break;
            case FinalVariables.LANGUAGE:
                SharedPreferencesHandler.writeInt(activity, FinalVariables.LANGUAGE_NAME, value);
                break;
        }
    }

    private void determineLayoutDirection(final View view){
        ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
                        return true;
                    }
                });
    }

    public void onGameFinished(Bundle data) {
        new MyLog(TAG, "on game finished");
        if(data != null) {
            int gameMode = data.getInt(FinalVariables.GAME_MODE, 0);
            switch (gameMode) {
                case FinalVariables.TAP_PVE:
                    score = data.getInt(FinalVariables.SCORE, 0);
                    winScore.setText(getString(R.string.score).concat(" " + score));
                    //winScore.setText("Score: " + score);
                    if (score > highTaps) {
                        highScoreTitle.setText(getString(R.string.new_high_score));
                        saveToSharedPreferences(FinalVariables.TAP_PVE, score);
                    }
                    break;
                case FinalVariables.TAP_PVP:
                    winner = data.getString(FinalVariables.WINNER);
                    winScore.setText(winner.concat(" " + getString(R.string.tap_pvp_winner_msg)));
                    //winScore.setText("Winner: " + winner);
                    break;
                case FinalVariables.TAP_PVP_ONLINE:
                    winner = data.getString(FinalVariables.WINNER);
                    winScore.setText(winner);
                    break;
                case FinalVariables.TYPE_PVE:
                    score = (int) data.getFloat(FinalVariables.SCORE, 0f);
                    winScore.setText(getString(R.string.score).concat(" " + score));
                    //winScore.setText("words per minute: " + score);
                    if (score > highTypes) {
                        highScoreTitle.setText(getString(R.string.new_high_score));
                        saveToSharedPreferences(FinalVariables.TYPE_PVE, score);
                    }
                    break;
                case FinalVariables.TYPE_PVP_ONLINE:
                    score = (int) data.getFloat(FinalVariables.SCORE, 0f);
                    winner = data.getString(FinalVariables.WINNER);
                    winScore.setText(winner);
                    if (score > highTypes) {
                        highScoreTitle.setText(getString(R.string.new_high_score));
                        saveToSharedPreferences(FinalVariables.TYPE_PVE, score);
                    }
                    break;
            }
            showHighScores();
        }
    }

    //region Fragment Overrides
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MyLog(TAG, "on create");
        activity = (GameActivity)getActivity();

        fadeIn.setDuration(FinalVariables.HOME_SHOW_UI);
        fadeOut.setDuration(FinalVariables.HOME_HIDE_UI);
        isTapClicked = false;
        isTypeClicked = false;
    }

    @Override
    public void onStart() {
        new MyLog(TAG, "on start");
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isTapClicked){
                    tap.performClick();
                    isTapClicked = true;
                }
                if(isTypeClicked){
                    type.performClick();
                    isTypeClicked = true;
                }
            }
        }, 200);
    }

    @Override
    public void onResume() {
        new MyLog(TAG, "on resume");
        super.onResume();
        triedExit = false;
        activity.isGameFinished = false;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed(){
        new MyLog(TAG, "on back pressed");
        if(isTapClicked)
            tap.performClick();
        else if(isTypeClicked)
            type.performClick();

        else if (triedExit) {
            model.setFinish(new MyEntry(FinalVariables.I_EXIT, null));
        } else {
            new MyToast(activity, R.string.before_exit);
            triedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    triedExit = false;
                }
            }, 1200);
        }
    }

    //endregion
}
