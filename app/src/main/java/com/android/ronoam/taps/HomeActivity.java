package com.android.ronoam.taps;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.ronoam.taps.Fragments.DialogInfoFragment;
import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.SharedPreferencesHandler;

public class HomeActivity extends AppCompatActivity {

    private final String TAG = "HomeActivity";
    private LinearLayout tapDrawer, typeDrawer;
    private Button tap, type;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fadeIn.setDuration(FinalVariables.HOME_SHOW_UI);
        fadeOut.setDuration(FinalVariables.HOME_HIDE_UI);
        isTapClicked = false;
        isTypeClicked = false;

        bindUI();
        setDesign();
        determineLayoutDirection(tap);

        loadFromSharedPreferences();

        if(language == FinalUtilsVariables.HEBREW){
            heb.setImageResource(R.drawable.heb_c_c);
        }
        else{
            eng.setImageResource(R.drawable.eng_c_c);
        }


        showHighScores();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
    }

    public void infoClick(View v){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
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
        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");

        tap.setTypeface(AssistantBoldFont);
        type.setTypeface(AssistantBoldFont);
        highScoreTitle.setTypeface(AssistantBoldFont);
        highScoreTap.setTypeface(AssistantBoldFont);
        highScoreType.setTypeface(AssistantBoldFont);
        highScoreTapTitle.setTypeface(AssistantBoldFont);
        highScoreTypeTitle.setTypeface(AssistantBoldFont);
        winScore.setTypeface(AssistantBoldFont);
    }

    //region Activity Overrides

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    protected void onPause() {
        new MyLog(TAG, "Pausing.");
        super.onPause();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
    }

    @Override
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
    }

    //endregion

    //region onClicks

    private int widthReturn = 8;

    public void tapClick(View v){
        if(tapDrawer.getVisibility() == View.VISIBLE){
            isTapClicked = false;
            tap.setEnabled(false);

            if(!isRtl)
                tap.animate().xBy((screenWidth/2 - tap.getWidth() - tap.getWidth() / widthReturn) * -1).setDuration(FinalVariables.HOME_HIDE_UI);
            else
                tap.animate().xBy(screenWidth/2 - tap.getWidth() - tap.getWidth() / widthReturn).setDuration(FinalVariables.HOME_HIDE_UI);

            type.startAnimation(fadeIn);
            type.setVisibility(View.VISIBLE);

            tapDrawer.startAnimation(fadeOut);
            tapDrawer.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tap.setEnabled(true);
                    type.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }
        else{
            isTapClicked = true;
            tap.setEnabled(false);
            type.setEnabled(false);

            if(!isRtl)
                tap.animate().xBy(screenWidth / 2 - tap.getWidth() - tap.getWidth() / widthReturn).setDuration(FinalVariables.HOME_SHOW_UI);
            else
                tap.animate().xBy((screenWidth / 2 - tap.getWidth() - tap.getWidth() / widthReturn) * -1).setDuration(FinalVariables.HOME_SHOW_UI);

            type.startAnimation(fadeOut);
            type.setVisibility(View.INVISIBLE);

            tapDrawer.startAnimation(fadeIn);
            tapDrawer.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tap.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }
    }

    public void typeClick(View v){
        if (typeDrawer.getVisibility() == View.VISIBLE){
            isTypeClicked = false;
            type.setEnabled(false);

            if(!isRtl)
                type.animate().xBy(screenWidth/2 - type.getWidth() - type.getWidth() / widthReturn).setDuration(FinalVariables.HOME_SHOW_UI);
            else
                type.animate().xBy((screenWidth/2 - type.getWidth() - type.getWidth() / widthReturn) * -1).setDuration(FinalVariables.HOME_SHOW_UI);

            tap.startAnimation(fadeIn);
            tap.setVisibility(View.VISIBLE);

            typeDrawer.startAnimation(fadeOut);
            typeDrawer.setVisibility(View.INVISIBLE);

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
                type.animate().xBy((screenWidth / 2 - tap.getWidth() - type.getWidth() / widthReturn) * -1).setDuration(FinalVariables.HOME_HIDE_UI);
            else
                type.animate().xBy(screenWidth / 2 - tap.getWidth() - type.getWidth() / widthReturn).setDuration(FinalVariables.HOME_HIDE_UI);

            tap.startAnimation(fadeOut);
            tap.setVisibility(View.INVISIBLE);

            typeDrawer.startAnimation(fadeIn);
            typeDrawer.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    type.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }
    }

    public void tapPveClick(View v){
        startForResult(FinalVariables.TAP_PVE);
    }

    public void tapPvpClick(View v){
        startForResult(FinalVariables.TAP_PVP);
    }

    public void tapPvpOnlineClick(View v){
        startForResult(FinalVariables.TAP_PVP_ONLINE);
    }

    public void typePveClick(View v){
        startForResult(FinalVariables.TYPE_PVE);
    }

    public void typePvpOnlineClick(View v){
        startForResult(FinalVariables.TYPE_PVP_ONLINE);
    }

    @SuppressLint("RestrictedApi")
    private void startForResult(int gameMode){
        //logEvent(gameMode);
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, gameMode);
        if(gameMode >= FinalVariables.TYPE_PVE) {
            intent.putExtra(FinalVariables.LANGUAGE_NAME, language);
            ((MyApplication)getApplication()).setGameLanguage(language);
        }
        startActivityForResult(intent, FinalVariables.REQUEST_CODE, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        //startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

/*
    private void logEvent(int gameMode) {
        //String name = getResources().getStringArray(R.array.game_modes)[gameMode];
        Bundle bundle = new Bundle();
        if (gameMode >= FinalVariables.TYPE_PVE)
            bundle.putString("language", getResources().getStringArray(R.array.game_languages)[language]);

        String type;
        if (gameMode == FinalVariables.TAP_PVP_ONLINE || gameMode == FinalVariables.TYPE_PVP_ONLINE)
            type = "online_game";
        else
            type = "offline_game";
        bundle.putString("game_type", type);

        //bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "game_mode");
        bundle.putString("game_mode", getResources().getStringArray(R.array.game_modes)[gameMode]);
        mFirebaseAnalytics.logEvent("games", bundle);
    }*/

    //endregion

    private void bindUI(){
        tap = findViewById(R.id.imageButton_tap);
        type = findViewById(R.id.imageButton_type);

        tapDrawer = findViewById(R.id.layout_tap_drawer);
        typeDrawer = findViewById(R.id.layout_type_drawer);

        heb = findViewById(R.id.imageView_heb);
        eng = findViewById(R.id.imageView_eng);
        infoBtn = findViewById(R.id.button_information);

        winScore = findViewById(R.id.textView_winner_score);

        highScoreTitle = findViewById(R.id.textView_high_score_title);

        highScoreTap = findViewById(R.id.textView_high_tap_score);
        highScoreType = findViewById(R.id.textView_high_type_score);
        highScoreTapTitle = findViewById(R.id.textView_high_tap_title);
        highScoreTypeTitle = findViewById(R.id.textView_high_type_title);
    }

    private void loadFromSharedPreferences() {
        highTaps = SharedPreferencesHandler.getInt(this, FinalVariables.HIGH_SCORE_TAP_KEY);
        highTypes = SharedPreferencesHandler.getInt(this, FinalVariables.HIGH_SCORE_TYPE_KEY);
        language = SharedPreferencesHandler.getInt(this, FinalVariables.LANGUAGE_NAME);
    }

    private void showHighScores(){
        highScoreTap.setText(String.valueOf(highTaps));
        highScoreType.setText(String.valueOf(highTypes));
    }

    private void saveToSharedPreferences(int key, int value){
        switch(key){
            case FinalVariables.TAP_PVE:
                highTaps = value;
                SharedPreferencesHandler.writeInt(this, FinalVariables.HIGH_SCORE_TAP_KEY, value);
                break;
            case FinalVariables.TYPE_PVE:
                highTypes = value;
                SharedPreferencesHandler.writeInt(this, FinalVariables.HIGH_SCORE_TYPE_KEY, value);
                break;
            case FinalVariables.LANGUAGE:
                SharedPreferencesHandler.writeInt(this, FinalVariables.LANGUAGE_NAME, value);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == FinalVariables.REQUEST_CODE){
            if(resultCode == RESULT_OK){
                int gameMode = data.getIntExtra(FinalVariables.GAME_MODE, 0);
                switch (gameMode){
                    case FinalVariables.TAP_PVE:
                        score = data.getIntExtra(FinalVariables.SCORE, 0);
                        winScore.setText(getString(R.string.score).concat(" " + score));
                        //winScore.setText("Score: " + score);
                        if(score > highTaps) {
                            highScoreTitle.setText(getString(R.string.new_high_score));
                            saveToSharedPreferences(FinalVariables.TAP_PVE, score);
                        }
                        break;
                    case FinalVariables.TAP_PVP:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner.concat(" " + getString(R.string.tap_pvp_winner_msg)));
                        //winScore.setText("Winner: " + winner);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner);
                        break;
                    case FinalVariables.TYPE_PVE:
                        score = (int)data.getFloatExtra(FinalVariables.SCORE, 0f);
                        winScore.setText(getString(R.string.score).concat(" " + score));
                        //winScore.setText("words per minute: " + score);
                        if(score > highTypes) {
                            highScoreTitle.setText(getString(R.string.new_high_score));
                            saveToSharedPreferences(FinalVariables.TYPE_PVE, score);
                        }
                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        score = (int)data.getFloatExtra(FinalVariables.SCORE, 0f);
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner);
                        if(score > highTypes) {
                            highScoreTitle.setText(getString(R.string.new_high_score));
                            saveToSharedPreferences(FinalVariables.TYPE_PVE, score);
                        }
                        break;
                }
                showHighScores();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(isTapClicked)
            tap.performClick();
        /*else if(isInfoShow)
            infoBtn.performClick();*/
        else if(isTypeClicked)
            type.performClick();
        else
            super.onBackPressed();
    }
}
