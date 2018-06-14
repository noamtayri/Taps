package com.android.ronoam.taps;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.KeyboardActivity;
import com.android.ronoam.taps.Utils.FinalVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

public class HomeActivity extends AppCompatActivity {

    private Button tapPve;
    private Button tapPvp;
    private Button tapPvpOnline;
    private Button typePve;
    private Button typePvpOnline;
    private Button records;

    private TextView head;

    private Bundle data;
    private int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bindUI();

        data = getIntent().getExtras();
        if(data != null){
            score = data.getInt(FinalVariables.SCORE);
            head.setText("Score: " + score);
        }else
            head.setText(R.string.HomeActivity_textView_head);


    }

    public void tapPveClick(View v){
//        new MyLog("Test","tapPveClick");
//        new MyToast(this, "tapPveClick");
        Intent i = new Intent(this, CountDownActivity.class);
        i.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVE);
        startActivity(i);
    }

    public void tapPvpClick(View v){
        new MyLog("Test","tapPvpClick");
        new MyToast(this, "tapPvpClick");
    }

    public void tapPvpOnlineClick(View v){
        new MyLog("Test","tapPvpOnlineClick");
        new MyToast(this, "tapPvpOnlineClick");
    }

    public void typePveClick(View v){
        startActivity(new Intent(HomeActivity.this, KeyboardActivity.class));
        new MyLog("Test","typePveClick");
        new MyToast(this, "typePveClick");
    }

    public void typePvpOnlineClick(View v){
        new MyLog("Test","typePvpOnlineClick");
        new MyToast(this, "typePvpOnlineClick");
    }

    public void recordsClick(View v){
        new MyLog("Test","recordsClick");
        new MyToast(this, "recordsClick");
    }

    private void bindUI(){
        tapPve = findViewById(R.id.button_tap_pve);
        tapPvp = findViewById(R.id.button_tap_pvp);
        tapPvpOnline = findViewById(R.id.button_tap_pvp_online);
        typePve = findViewById(R.id.button_type_pve);
        typePvpOnline = findViewById(R.id.button_type_pvp_online);
        records = findViewById(R.id.button_records);

        head = findViewById(R.id.textView_head);
    }
}
