package com.android.ronoam.taps;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
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

import com.android.ronoam.taps.Fragments.BluetoothConnectionSetupFragment;
import com.android.ronoam.taps.Fragments.ChooseConnectionTypeFragment;
import com.android.ronoam.taps.Fragments.WifiConnectionSetupFragment;
import com.android.ronoam.taps.Fragments.CountDownFragment;
import com.android.ronoam.taps.Fragments.TapPveFragment;
import com.android.ronoam.taps.Fragments.TapPvpFragment;
import com.android.ronoam.taps.Fragments.TypeFragment;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
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
    NetworkConnection mConnection;

    MyApplication application;

    int currentFragment;
    public int gameMode, language;
    public boolean isGameFinished, connectionEstablished;
    private boolean triedExit, pvpOnline, setupPostFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //getWindow().setExitTransition(new Explode());

        application = (MyApplication) getApplication();
        currentFragment = 0;
        gameMode = getIntent().getExtras().getInt(FinalVariables.GAME_MODE);
        application.gameMode = gameMode;

        if(gameMode >= FinalVariables.TYPE_PVE)
            language = getIntent().getExtras().getInt(FinalVariables.LANGUAGE_NAME);

        if(gameMode == FinalVariables.TAP_PVP_ONLINE || gameMode == FinalVariables.TYPE_PVP_ONLINE){
            pvpOnline = true;
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            setConnectionHandler();
        }
        setViewModel();
        setupPreFragments();
    }

    private void setupPreFragments(){
        if(pvpOnline) {
            mFragmentList.add(new ChooseConnectionTypeFragment());
        }
        else
            setupPostFragments();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.game_fragment_container, mFragmentList.get(0));
        fragmentTransaction.commit();
    }

    private void setupPostFragments(){
        if(setupPostFragments){
            mFragmentList.clear();
            mFragmentList.add(new ChooseConnectionTypeFragment());
        }
        setupPostFragments = true;
        if(pvpOnline) {
            createConnection();
            if (application.getConnectionMethod() == FinalVariables.BLUETOOTH_MODE)
                mFragmentList.add(new BluetoothConnectionSetupFragment());
            else
                mFragmentList.add(new WifiConnectionSetupFragment());
        }
        mFragmentList.add(new CountDownFragment());
        switch (gameMode){
            case FinalVariables.TAP_PVE:
                mFragmentList.add(new TapPveFragment());
                break;
            case FinalVariables.TAP_PVP:
                mFragmentList.add(new TapPvpFragment());
                break;
            case FinalVariables.TYPE_PVE:
                mFragmentList.add(new TypeFragment());
                break;
            case FinalVariables.TAP_PVP_ONLINE:
                mFragmentList.add(new TapPvpFragment());
                break;
            case FinalVariables.TYPE_PVP_ONLINE:
                mFragmentList.add(new TypeFragment());
                break;
        }
    }

    public void moveToNextFragment(Bundle bundle){
        currentFragment++;
        if(pvpOnline){
            if(currentFragment == 1)
                setupPostFragments();
            if(currentFragment == 2)
                setGameHandler();
        }
        if(currentFragment < mFragmentList.size()) {
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
        if(isGameFinished)
            return;

        isGameFinished = true;

        Intent resIntent = new Intent(GameActivity.this, HomeActivity.class);
        if(exitCode == FinalVariables.NO_ERRORS){
            resIntent.putExtras(bundle);
            setResult(RESULT_OK, resIntent);
        }
        else
            setResult(RESULT_CANCELED);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(R.anim.activity_slide_bottom_exit, android.R.anim.fade_out);
            }
        }, FinalVariables.HOME_HIDE_UI);
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
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what != FinalVariables.MESSAGE_READ &&
                        msg.what != FinalVariables.MESSAGE_WRITE)
                    return true;
                if(msg.arg1 == FinalVariables.FROM_MYSELF)
                    return true;
                String chatLine = msg.getData().getString("msg");
                if (chatLine == null && !isGameFinished) {
                    new MyLog(TAG + "game", "null");
                    if (msg.arg1 == FinalVariables.FROM_OPPONENT) {
                        new MyToast(getApplicationContext(), "Connection Lost");
                        stopGameWithError(FinalVariables.OPPONENT_EXIT, null);
                    }
                } else if (chatLine != null && !isGameFinished) {
                    //new MyLog(TAG, chatLine);
                    model.setInMessage(chatLine);
                }
                return true;
            }
        });
        application.setConnectionHandler(mUpdateHandler);
    }

    public int getLocalPort(){
        return mConnection.getLocalPort();
    }

    public NetworkConnection getConnection(){
        return mConnection;
    }

    public void connectToService(NsdServiceInfo service){
        mConnection.connectToServer(service.getHost(), service.getPort());
    }

    public void connectToDevice(BluetoothDevice device){
        //mConnection.connectToDevice(address);
        mConnection.startListening(device);
        if(device.getAddress().compareTo(android.provider.Settings.Secure.getString(
                getContentResolver(), "bluetooth_address")) < 0)
            mConnection.startAsyncConnect(device);
    }

    public void setFinishAsyncFlag(boolean flag){
        mConnection.setFinishFragmentFlag(flag);
    }

    public void cancelAsyncConnect(){
        setFinishAsyncFlag(true);
    }

    private void createConnection(){
        mConnection = application.createNetworkConnection(mUpdateHandler);
    }

    public void sendMessage(String msg){
        if(mConnection != null){
            if(application.connectionMethod == FinalVariables.WIFI_MODE) {
                if(mConnection.getLocalPort() > -1)
                    mConnection.sendMessage(msg);
            }else
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
        if(mConnection != null && pvpOnline)
            application.setConnectionHandler(mUpdateHandler);
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        application.hideSystemUI(getWindow().getDecorView());
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
            application.setConnectionHandler(null);
        }
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        new MyLog(TAG, "Being destroyed");
        if(mConnection != null && pvpOnline) {
            mConnection = null;
            application.connectionTearDown();
        }
    }

    @Override public void onBackPressed() {
        if(pvpOnline && currentFragment > 0 && currentFragment < mFragmentList.size() - 2){
            currentFragment -= 2;
            moveToNextFragment(null);
        }
        else {
            if (triedExit) {
                stopGameWithError(FinalVariables.I_EXIT, null);
            } else {
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
    }

    //endregion
}



