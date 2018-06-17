package com.android.ronoam.taps;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ronoam.taps.Utils.FinalVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

public class HomeActivity extends AppCompatActivity {

    private Button tap;
    private Button type;

    private Button tapPve;
    private Button tapPvp;
    private Button tapPvpOnline;
    private Button typePve;
    private Button typePvpOnline;
    private Button records;

    private TextView head;
    private TextView winScore;

    private Bundle data;
    private int gameMode;
    private int score;
    private String winner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindUI();
    }

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
        new MyToast(this, "tapPvpOnlineClick");
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
        new MyToast(this, "typePvpOnlineClick");
    }

    public void recordsClick(View v){
        winScore.setText("");
        new MyLog("Test","recordsClick");
        new MyToast(this, "recordsClick");
    }

    private void bindUI(){
        tap = findViewById(R.id.button_tap);
        type = findViewById(R.id.button_type);

        tapPve = findViewById(R.id.button_tap_pve);
        tapPvp = findViewById(R.id.button_tap_pvp);
        tapPvpOnline = findViewById(R.id.button_tap_pvp_online);
        typePve = findViewById(R.id.button_type_pve);
        typePvpOnline = findViewById(R.id.button_type_pvp_online);
        records = findViewById(R.id.button_records);

        head = findViewById(R.id.textView_head);
        winScore = findViewById(R.id.textView_winner_score);
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
                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        //todo: move for type_pvp_online game mode
                        break;
                }
            }
        }

        //winScore.setText("");

    }
}
