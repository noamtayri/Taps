package com.android.ronoam.taps;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.android.ronoam.taps.Utils.FinalVariables;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TapPvpOnlineActivity extends AppCompatActivity {

    private View upLayout;
    private View bottomLayout;
    private View container;
    private int screenHeight;
    final Animation animation = new AlphaAnimation(0.1f, 1.0f);

    int delta;

    String myName;
    String friend;
    String roomId;

    long myCounter = 0;
    long friendCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pvp_online);

        container = findViewById(R.id.tap_pvp_container);

        upLayout = findViewById(R.id.frameLayout_up);
        bottomLayout = findViewById(R.id.frameLayout_bottom);

        upLayout.setBackgroundColor(Color.RED);
        bottomLayout.setBackgroundColor(Color.BLUE);

        getScreenSize();

        animation.setDuration(10);

        Bundle data = getIntent().getExtras();
        myName = data.getString(FinalVariables.MY_NAME);
        roomId = data.getString(FinalVariables.ROOM_ID);
        friend = data.getString(FinalVariables.FRIEND);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot room: dataSnapshot.getChildren()){
                    String id = (String) room.child("roomId").getValue();
                    if(!roomId.equals(id))
                        continue;
                    if(friendCounter != (long)room.child(friend).child("counter").getValue()){
                        friendCounter = (long)room.child(friend).child("counter").getValue();
                        friendClick();
                        checkWin();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bottomLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        bottomLayout.startAnimation(animation);
                        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() - screenHeight / 22, bottomLayout.getRight(), bottomLayout.getBottom());
                        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() - screenHeight / 22);
                        myCounter ++;
                        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot room: dataSnapshot.getChildren()){
                                    String id = (String) room.child("roomId").getValue();
                                    if(!roomId.equals(id))
                                        continue;
                                    String me;
                                    if(myName.equals(room.child("user1").child("id").getValue()))
                                        me = "user1";
                                    else
                                        me = "user2";
                                    myRef.child(room.getKey()).child(me).child("counter").setValue(myCounter);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        checkWin();
                        return true;
                }
                return true;
            }
        });

    }

    private void friendClick(){
        upLayout.startAnimation(animation);
        upLayout.layout(upLayout.getLeft(), upLayout.getTop(), upLayout.getRight(), upLayout.getBottom() + screenHeight / 22);
        bottomLayout.layout(bottomLayout.getLeft(), bottomLayout.getTop() + screenHeight / 22, bottomLayout.getRight(), bottomLayout.getBottom());
        checkWin();
    }


    private void checkWin(){
        if(upLayout.getHeight() >=  screenHeight){
//            Intent resIntent = new Intent(TapPvpActivity.this, HomeActivity.class);
//            resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
//            resIntent.putExtra(FinalVariables.WINNER, "red / up");
//            setResult(RESULT_OK, resIntent);
            //finish();
            new MyLog("TAG", "I loose");
        }
        else if(bottomLayout.getHeight() >= screenHeight){
//            Intent resIntent = new Intent(TapPvpActivity.this, HomeActivity.class);
//            resIntent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP);
//            resIntent.putExtra(FinalVariables.WINNER, "blue / bottom");
//            setResult(RESULT_OK, resIntent);
            //finish();
            new MyLog("TAG", "I win");
        }
    }

    private void getScreenSize(){
        ViewTreeObserver viewTreeObserver = container.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    //viewWidth = container.getWidth();
                    screenHeight = container.getHeight();
                    new MyToast(TapPvpOnlineActivity.this, "screen height = " + screenHeight);
                }
            });
        }
    }
}
