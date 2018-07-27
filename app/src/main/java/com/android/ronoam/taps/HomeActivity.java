package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.SharedPreferencesHandler;


public class HomeActivity extends AppCompatActivity {

    private final String TAG = "HomeActivity";
    private ImageButton tap, type, tapPve, tapPvp, tapPvpOnline, typePve, typePvpOnline;

    private TextView highScoreTitle, highScoreTapTitle, highScoreTypeTitle;
    private TextView highScoreTap, highScoreType;
    private TextView winScore;

    //private Bundle data;
    //private int gameMode;
    private int highTaps, highTypes;
    private int score;
    private String winner;

    final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fadeIn.setDuration(FinalVariables.HOME_SHOW_UI);
        fadeOut.setDuration(FinalVariables.HOME_HIDE_UI);

        bindUI();
        setDesign();

        loadHighScores();
        showHighScores();
    }

    private void setDesign() {
        //using imported font
        Typeface AssistantBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-Bold.ttf");

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

    public void tapClick(View v){
        if (tapPve.getVisibility() == View.VISIBLE){
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
                    type.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }else{
            type.setEnabled(false);

            type.startAnimation(fadeOut);
            type.setVisibility(View.INVISIBLE);

            tapPve.startAnimation(fadeIn);
            tapPvp.startAnimation(fadeIn);
            tapPvpOnline.startAnimation(fadeIn);

            tapPve.setVisibility(View.VISIBLE);
            tapPvp.setVisibility(View.VISIBLE);
            tapPvpOnline.setVisibility(View.VISIBLE);
        }

        /*
        typePve.setVisibility(View.INVISIBLE);
        typePvpOnline.setVisibility(View.INVISIBLE);

        if (tapPve.getVisibility() == View.VISIBLE){
            tapPve.setVisibility(View.INVISIBLE);
            tapPvp.setVisibility(View.INVISIBLE);
            tapPvpOnline.setVisibility(View.INVISIBLE);
        }else{
            tapPve.setVisibility(View.VISIBLE);
            tapPvp.setVisibility(View.VISIBLE);
            tapPvpOnline.setVisibility(View.VISIBLE);
        }
        */
    }

    public void typeClick(View v){
        if (typePve.getVisibility() == View.VISIBLE){
            tap.startAnimation(fadeIn);
            tap.setVisibility(View.VISIBLE);

            typePve.startAnimation(fadeOut);
            typePvpOnline.startAnimation(fadeOut);

            typePve.setVisibility(View.INVISIBLE);
            typePvpOnline.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tap.setEnabled(true);
                }
            }, FinalVariables.HOME_SHOW_UI);
        }else{
            tap.setEnabled(false);

            tap.startAnimation(fadeOut);
            tap.setVisibility(View.INVISIBLE);

            typePve.startAnimation(fadeIn);
            typePvpOnline.startAnimation(fadeIn);

            typePve.setVisibility(View.VISIBLE);
            typePvpOnline.setVisibility(View.VISIBLE);
        }

        /*
        tapPve.setVisibility(View.INVISIBLE);
        tapPvp.setVisibility(View.INVISIBLE);
        tapPvpOnline.setVisibility(View.INVISIBLE);

        if(typePve.getVisibility() == View.VISIBLE){
            typePve.setVisibility(View.INVISIBLE);
            typePvpOnline.setVisibility(View.INVISIBLE);
        }else{
            typePve.setVisibility(View.VISIBLE);
            typePvpOnline.setVisibility(View.VISIBLE);
        }
        */
    }

    public void tapPveClick(View v){
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        startForResult(FinalVariables.TAP_PVE);
    }

    public void tapPvpClick(View v){
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        startForResult(FinalVariables.TAP_PVP);
    }

    public void tapPvpOnlineClick(View v){
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        startForResult(FinalVariables.TAP_PVP_ONLINE);
    }

    public void typePveClick(View v){
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        startForResult(FinalVariables.TYPE_PVE);
        /*Intent intent = new Intent(this, CountDownActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVE);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);*/
    }

    public void typePvpOnlineClick(View v){
        winScore.setText("");
        highScoreTitle.setText(getString(R.string.HomeActivity_textView_highScore_title));
        Intent intent = new Intent(this, ConnectionOnlineActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVP_ONLINE);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

    private void startForResult(int gameMode){
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, gameMode);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

    //endregion

    private void bindUI(){
        tap = findViewById(R.id.imageButton_tap);
        type = findViewById(R.id.imageButton_type);

        tapPve = findViewById(R.id.button_tap_pve);
        tapPvp = findViewById(R.id.button_tap_pvp);
        tapPvpOnline = findViewById(R.id.button_tap_pvp_online);
        typePve = findViewById(R.id.button_type_pve);
        typePvpOnline = findViewById(R.id.button_type_pvp_online);

        winScore = findViewById(R.id.textView_winner_score);

        highScoreTap = findViewById(R.id.textView_high_tap_score);
        highScoreType = findViewById(R.id.textView_high_type_score);
        highScoreTitle = findViewById(R.id.textView_high_score_title);
        highScoreTapTitle = findViewById(R.id.textView_high_tap_title);
        highScoreTypeTitle = findViewById(R.id.textView_high_type_title);
    }

    private void loadHighScores() {
        highTaps = SharedPreferencesHandler.getInt(this, FinalUtilsVariables.HIGH_SCORE_TAP_KEY);
        highTypes = SharedPreferencesHandler.getInt(this, FinalUtilsVariables.HIGH_SCORE_TYPE_KEY);
    }

    private void showHighScores(){
        highScoreTap.setText(String.valueOf(highTaps));
        highScoreType.setText(String.valueOf(highTypes));
    }

    private void saveHighScore(int key, int score){
        switch(key){
            case FinalVariables.TAP_PVE:
                highTaps = score;
                SharedPreferencesHandler.writeInt(this, FinalUtilsVariables.HIGH_SCORE_TAP_KEY, score);
                break;
            case FinalVariables.TYPE_PVE:
                highTypes = score;
                SharedPreferencesHandler.writeInt(this, FinalUtilsVariables.HIGH_SCORE_TYPE_KEY, score);
                break;
        }
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
                        winScore.setText(getString(R.string.score) + " " + score);
                        //winScore.setText("Score: " + score);
                        if(score > highTaps) {
                            highScoreTitle.setText(getString(R.string.new_high_score));
                            saveHighScore(FinalVariables.TAP_PVE, score);
                        }
                        break;
                    case FinalVariables.TAP_PVP:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner + " " + getString(R.string.tap_pvp_winner_msg));
                        //winScore.setText("Winner: " + winner);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner);
                        break;
                    case FinalVariables.TYPE_PVE:
                        score = (int)data.getFloatExtra(FinalVariables.WORDS_PER_MIN, 0f);
                        winScore.setText(getString(R.string.score) + " " + score);
                        //winScore.setText("words per minute: " + score);
                        if(score > highTypes) {
                            highScoreTitle.setText(getString(R.string.new_high_score));
                            saveHighScore(FinalVariables.TYPE_PVE, score);
                        }
                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText(winner);
                        break;
                }
                showHighScores();
            }
        }
    }
}
