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

        data = getIntent().getExtras();
        if(data != null){
            gameMode = data.getInt(FinalVariables.GAME_MODE);
            switch (gameMode){
                case 1:
                    score = data.getInt(FinalVariables.SCORE);
                    winScore.setText("Score: " + score);
                    break;
                case 2:
                    winner = data.getString(FinalVariables.WINNER);
                    winScore.setText("Winner: " + winner);
                    break;
            }

        }else
            winScore.setText("");


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
//        new MyLog("Test","tapPveClick");
//        new MyToast(this, "tapPveClick");
        Intent i = new Intent(this, CountDownActivity.class);
        i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVE);
        startActivity(i);
    }

    public void tapPvpClick(View v){
        winScore.setText("");
        Intent i = new Intent(this, CountDownActivity.class);
        i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
        startActivity(i);
//        new MyLog("Test","tapPvpClick");
//        new MyToast(this, "tapPvpClick");
    }

    public void tapPvpOnlineClick(View v){
        winScore.setText("");
        new MyLog("Test","tapPvpOnlineClick");
        new MyToast(this, "tapPvpOnlineClick");
    }

    public void typePveClick(View v){
        winScore.setText("");
        Intent i = new Intent(this, CountDownActivity.class);
        i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVE);
        startActivity(i);
        new MyLog("Test","typePveClick");
        //new MyToast(this, "typePveClick");
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
}
