package com.android.ronoam.taps;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private boolean isStopped;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView(R.layout.activity_splash);
        setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);
        intent = new Intent(SplashActivity.this, HomeActivity.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isStopped = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle());
                if(!isStopped) {
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }
}
