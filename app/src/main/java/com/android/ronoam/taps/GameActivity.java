package com.android.ronoam.taps;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.ronoam.taps.Fragments.ConnectionSetupFragment;
import com.android.ronoam.taps.Fragments.CountDownFragment;
import com.android.ronoam.taps.Fragments.TapPveFragment;
import com.android.ronoam.taps.Fragments.TapPvpFragment;
import com.android.ronoam.taps.Fragments.TypeFragment;
import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final String TAG = "Game";

    private MyViewModel model;
    private Handler mUpdateHandler;
    ChatConnection mConnection;

    ChatApplication application;

    int currentFragment;
    public int gameMode;
    public boolean isGameFinished, connectionEstablished, pvpOnline;
    private boolean triedExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        application = (ChatApplication) getApplication();
        currentFragment = 0;
        gameMode = getIntent().getExtras().getInt(FinalVariables.GAME_MODE);

        if(gameMode == FinalVariables.TAP_PVP_ONLINE || gameMode == FinalVariables.TYPE_PVP_ONLINE){
            pvpOnline = true;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            setConnectionHandler();
            //getConnection();
        }
        setViewModel();
        setupFragments();
    }

    private void setupFragments(){
        /*if(pvpOnline)
            mFragmentList.add(new ConnectionSetupFragment());
        mFragmentList.add(new CountDownFragment());
        */
        switch (gameMode){
            case FinalVariables.TAP_PVE:
                mFragmentList.add(new CountDownFragment());
                mFragmentList.add(new TapPveFragment());
                break;
            case FinalVariables.TAP_PVP:
                mFragmentList.add(new CountDownFragment());
                mFragmentList.add(new TapPvpFragment());
                break;
            case FinalVariables.TYPE_PVE:
                mFragmentList.add(new CountDownFragment());
                mFragmentList.add(new TypeFragment());
                break;
            case FinalVariables.TAP_PVP_ONLINE:
                mFragmentList.add(new ConnectionSetupFragment());
                mFragmentList.add(new CountDownFragment());
                mFragmentList.add(new TapPvpFragment());
                break;
            case FinalVariables.TYPE_PVP_ONLINE:
                mFragmentList.add(new ConnectionSetupFragment());
                mFragmentList.add(new CountDownFragment());
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.game_fragment_container, mFragmentList.get(0));
        fragmentTransaction.commit();
    }

    public void moveToNextFragment(Bundle bundle){
        currentFragment++;
        if(currentFragment < mFragmentList.size()) {
            if(currentFragment == mFragmentList.size() - 1)
                setGameHandler();
            if (bundle != null)
                mFragmentList.get(currentFragment).setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_right_exit);
            fragmentTransaction.replace(R.id.game_fragment_container, mFragmentList.get(currentFragment));
            fragmentTransaction.commit();
        }
    }

    private void setViewModel(){
        model = ViewModelProviders.of(this).get(MyViewModel.class);
        model.getOutMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                sendMessage(s);
            }
        });
        model.getFinish().observe(this, new Observer<MyEntry>() {
            @Override
            public void onChanged(@Nullable MyEntry entry) {
                assert entry != null;
                stopGameWithError(entry.getKey(), entry.getValue());
            }
        });
    }

    private void stopGameWithError(final int exitCode, Bundle bundle) {
        new MyLog(TAG, "Stopping Game");
        if(isGameFinished){
            new MyLog(TAG, "isFinished = true");
            return;
        }
        isGameFinished = true;

        Intent resIntent = new Intent(GameActivity.this, HomeActivity.class);
        if(exitCode == FinalVariables.NO_ERRORS){
            resIntent.putExtras(bundle);
            setResult(RESULT_OK, resIntent);
        }
        else
            setResult(RESULT_CANCELED);

        finish();
    }

    //region Network Related
    private void setConnectionHandler(){
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                model.setConnectionInMessages(msg);
                return true;
            }
        });
    }

    private void setGameHandler() {
        if(gameMode == FinalVariables.TAP_PVP_ONLINE) {
            mUpdateHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String chatLine = msg.getData().getString("msg");
                    if (chatLine == null && !isGameFinished) {
                        new MyLog(TAG, "null");
                        if (msg.arg1 == FinalVariables.FROM_OPPONENT) {
                            new MyToast(getApplicationContext(), "Connection Lost");
                            stopGameWithError(FinalVariables.OPPONENT_EXIT, null);
                        }
                    } else if (chatLine != null && !isGameFinished) {
                        new MyLog(TAG, chatLine);
                        model.setInMessage(chatLine);
                    }
                    return true;
                }
            });
        }
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE){

        }
        application.setChatConnectionHandler(mUpdateHandler);
    }

    public int getLocalPort(){
        if(mConnection != null)
            return mConnection.getLocalPort();
        return -1;
    }

    public void connectToService(NsdServiceInfo service){
        mConnection.connectToServer(service.getHost(), service.getPort());
    }

    private void getConnection() {
        mConnection = application.createChatConnection(mUpdateHandler);
        /*application.setChatConnectionHandler(mUpdateHandler);
        mConnection = application.getChatConnection();*/
    }

    public void sendMessage(String msg){
        if(mConnection != null && mConnection.getLocalPort() > -1) {
            mConnection.sendMessage(msg);
        }
        else{
            new MyToast(this, "Not Connected");
            finish();
        }
    }
    //endregion

    //region Activity Overrides

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
        if(!connectionEstablished && pvpOnline)
            getConnection();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        ((ChatApplication)getApplication()).hideSystemUI(getWindow().getDecorView());
        triedExit = false;
        super.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        new MyLog(TAG, "Pausing");
    }

    @Override
    protected void onStop(){
        super.onStop();
        new MyLog(TAG, "Being stopped");
        if(!connectionEstablished && pvpOnline){
            application.setChatConnectionHandler(null);
            application.ChatConnectionTearDown();
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        new MyLog(TAG, "Being destroyed");
        if(mConnection != null && pvpOnline) {
            application.setChatConnectionHandler(null);
            mConnection = null;
            application.ChatConnectionTearDown();
        }
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            stopGameWithError(FinalVariables.I_EXIT, null);
        }
        else{
            new MyToast(this, R.string.before_exit);
            triedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    triedExit = false;
                }
            }, 1200);
        }
    }

    //endregion
}



