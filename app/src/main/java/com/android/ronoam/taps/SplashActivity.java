package com.android.ronoam.taps;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setContentView(R.layout.activity_splash);
        setTheme(R.style.AppTheme_Launcher);
        super.onCreate(savedInstanceState);

        final Intent intent = new Intent(this, HomeActivity.class);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(SplashActivity.this).toBundle());
                startActivity(intent);
                finish();
            }
        }, 2000);
    }
}
