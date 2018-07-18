package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.TypePveActivity;
import com.android.ronoam.taps.Utils.FinalVariables;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CountDownActivity extends AppCompatActivity {

    private TextView timeToStart;
    private CountDownTimer countDown;
    Bundle data;
    int gameMode;

    String myName;
    String friend;
    String roomId;

    boolean isConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        timeToStart = findViewById(R.id.textView_time_to_start);

        Typeface AssistantExtraBoldFont = Typeface.createFromAsset(getAssets(),  "fonts/Assistant-ExtraBold.ttf");

        timeToStart.setTypeface(AssistantExtraBoldFont);

        timeToStart.setTextColor(Color.BLACK);

        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE);

        if(gameMode == FinalVariables.TAP_PVP_ONLINE){
            myName = data.getString(FinalVariables.MY_NAME);
            roomId = data.getString(FinalVariables.ROOM_ID);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();

            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!isConnect){
                        for(DataSnapshot room: dataSnapshot.getChildren()){
                            if(!roomId.equals(room.child("roomId").getValue())){
                                continue;
                            }
                            if(room.child("user1").exists() && room.child("user2").exists()){
                                isConnect = true;
                                if(!myName.equals(room.child("user1").child("id").getValue()))
                                    friend = "user1";
                                else
                                    friend = "user2";
                                preTimerLogic();
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        if(gameMode != FinalVariables.TAP_PVP_ONLINE)
            preTimerLogic();
    }

    private void preTimerLogic() {
        countDown = new CountDownTimer(FinalVariables.PRE_TIMER, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeToStart.setX(-250f);
                timeToStart.setText("" + (millisUntilFinished / 1000));
                timeToStart.animate().translationX(new DisplayMetrics().widthPixels/2).setDuration(500);
            }

            @Override
            public void onFinish() {
                Intent intent = null;
                switch (gameMode){
                    case FinalVariables.TAP_PVE:
                        intent = new Intent(CountDownActivity.this, TapPveActivity.class);
                        break;
                    case FinalVariables.TAP_PVP:
                        intent = new Intent(CountDownActivity.this, TapPvpActivity.class);
                        break;
                    case FinalVariables.TAP_PVP_ONLINE:
                        intent = new Intent(CountDownActivity.this, TapPvpOnlineActivity.class);
                        intent.putExtra(FinalVariables.MY_NAME, myName);
                        intent.putExtra(FinalVariables.ROOM_ID, roomId);
                        intent.putExtra(FinalVariables.FRIEND, friend);
                        break;
                    case FinalVariables.TYPE_PVE:
                        intent = new Intent(CountDownActivity.this, TypePveActivity.class);

                        break;
                    case FinalVariables.TYPE_PVP_ONLINE:
                        //todo: move for type_pvp_online game mode
                        break;
                }
                if(intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    startActivity(intent);
                }
                finish();
            }
        }.start();
    }

    @Override
    protected void onStop(){
        super.onStop();
        countDown.cancel();
        finish();
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        countDown.cancel();
        finish();
    }
}
