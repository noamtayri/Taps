package com.android.ronoam.taps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Intent intent;
    boolean userExit = false, handlerFinished = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);

        intent = new Intent(this, HomeActivity.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handlerFinished = true;
                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle());
                if(!userExit) {
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userExit = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(userExit && handlerFinished){
            startActivity(intent);
            finish();
        }
    }
}
