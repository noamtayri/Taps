package com.android.ronoam.taps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;
import com.android.ronoam.taps.Utils.SharedPreferencesHandler;

import java.text.Format;

public class HomeActivity extends AppCompatActivity {

    private Button tapPve, tapPvp, tapPvpOnline, typePve, typePvpOnline;

    private TextView head, highScoreTap, highScoreType;
    private TextView winScore;

    //private Bundle data;
    //private int gameMode;
    private int highTaps, highTypes;
    private int score;
    private String winner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindUI();

        loadHighScores();
        showHighScores();
    }

    //region onClicks

    public void tapClick(View v){
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
    }

    public void typeClick(View v){
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
    }

    public void tapPveClick(View v){
        winScore.setText("");
        Intent intent = new Intent(this, CountDownActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVE);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

    public void tapPvpClick(View v){
        winScore.setText("");
        Intent intent = new Intent(this, CountDownActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

    public void tapPvpOnlineClick(View v){
        winScore.setText("");
        new MyLog("Test","tapPvpOnlineClick");
        Intent intent = new Intent(this, ConnectionOnlineActivity.class);
        intent.putExtra(FinalVariables.TIME_BEFORE_FINISH, FinalVariables.TIMER_LIMIT);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
        //new MyToast(this, "tapPvpOnlineClick");
    }

    public void typePveClick(View v){
        winScore.setText("");
        Intent intent = new Intent(this, CountDownActivity.class);
        intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVE);
        startActivityForResult(intent, FinalVariables.REQUEST_CODE);
    }

    public void typePvpOnlineClick(View v){
        winScore.setText("");
        new MyLog("Test","typePvpOnlineClick");
        Intent intent = new Intent(this, ConnectionOnlineActivity.class);
        intent.putExtra(FinalVariables.TIME_BEFORE_FINISH, FinalVariables.KEYBORAD_GAME_TIME);
        //new MyToast(this, "typePvpOnlineClick");
    }

    //endregion

    private void bindUI(){
        //tap = findViewById(R.id.button_tap);
        //type = findViewById(R.id.button_type);

        tapPve = findViewById(R.id.button_tap_pve);
        tapPvp = findViewById(R.id.button_tap_pvp);
        tapPvpOnline = findViewById(R.id.button_tap_pvp_online);
        typePve = findViewById(R.id.button_type_pve);
        typePvpOnline = findViewById(R.id.button_type_pvp_online);

        head = findViewById(R.id.textView_head);
        winScore = findViewById(R.id.textView_winner_score);

        highScoreTap = findViewById(R.id.textView_high_tap_score);
        highScoreType = findViewById(R.id.textView_high_type_score);
    }

    private void loadHighScores() {
        highTaps = SharedPreferencesHandler.getInt(this, FinalUtilsVariables.HIGH_SCORE_TAP_KEY);
        highTypes = SharedPreferencesHandler.getInt(this, FinalUtilsVariables.HIGH_SCORE_TYPE_KEY);
    }

    private void showHighScores(){
        highScoreTap.setText(Integer.toString(highTaps));
        highScoreType.setText(Integer.toString(highTypes));
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
                        winScore.setText("Score: " + score);
                        if(score > highTaps)
                            saveHighScore(FinalVariables.TAP_PVE, score);
                        break;
                    case FinalVariables.TAP_PVP:
                        winner = data.getStringExtra(FinalVariables.WINNER);
                        winScore.setText("Winner: " + winner);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        //todo: move for tap_pvp_online game mode
                        break;
                    case FinalVariables.TYPE_PVE:
                        score = (int)data.getFloatExtra(FinalVariables.WORDS_PER_MIN, 0f);
                        winScore.setText("words per minute: " + score);
                        if(score > highTypes)
                            saveHighScore(FinalVariables.TYPE_PVE, score);
                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        //todo: move for type_pvp_online game mode
                        break;
                }
                showHighScores();
            }
        }

        //winScore.setText("");
    }
}
