package com.android.ronoam.taps;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.android.ronoam.taps.Fragments.BluetoothConnectionSetupFragment;
import com.android.ronoam.taps.Fragments.ChooseConnectionTypeFragment;
import com.android.ronoam.taps.Fragments.WifiConnectionSetupFragment;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    private final String TAG = "HomeActivity";
    private final List<Fragment> mFragmentList = new ArrayList<>();
    public int currentFragment;

    MyApplication application;
    NetworkConnection mConnection;
    private MyViewModel model;
    private Handler mUpdateHandler;
    public boolean connectionEstablished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        application = (MyApplication)getApplication();
        currentFragment = 0;
        connectionEstablished = false;
        setViewModel();
        setConnectionHandler();
        setupFragment();
    }

    private void setupFragment() {
        mFragmentList.add(new ChooseConnectionTypeFragment());

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.home_fragment_container, mFragmentList.get(0));
        fragmentTransaction.commit();

        fragmentTransaction.addToBackStack(null);
    }

    public void moveToNextFragment(int connectionMode) {
        currentFragment++;

        if(connectionMode >= 0 && mConnection == null)
            createConnection();

        switch (connectionMode){
            case FinalVariables.WIFI_MODE:
                mFragmentList.add(new WifiConnectionSetupFragment());
                break;
            case FinalVariables.BLUETOOTH_MODE:
                mFragmentList.add(new BluetoothConnectionSetupFragment());
                break;
            default:
                startGame(false);
                return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(
                R.anim.fragment_slide_left_enter,
                R.anim.fragment_slide_left_exit,
                R.anim.fragment_slide_right_enter,
                R.anim.fragment_slide_right_exit);
        fragmentTransaction.replace(R.id.home_fragment_container, mFragmentList.get(currentFragment));
        fragmentTransaction.commit();
        fragmentTransaction.addToBackStack(null);
    }

    @SuppressLint("RestrictedApi")
    public void startGame(boolean isOnline){
        Intent intent = new Intent(HomeActivity.this, GameActivity.class);
        intent.putExtra(FinalVariables.ONLINE_GAME, isOnline);
        startActivityForResult(intent, 0,ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    //region Network Related
    private void setViewModel(){
        model = ViewModelProviders.of(this).get(MyViewModel.class);
        model.getOutMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                sendMessage(s);
            }
        });
    }

    private void setConnectionHandler(){
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                model.setConnectionInMessages(msg);
                return true;
            }
        });
    }

    public void createConnection(){
        mConnection = application.createNetworkConnection(mUpdateHandler);
    }

    public NetworkConnection getConnection() {
        return mConnection;
    }

    public void connectToService(NsdServiceInfo service){
        mConnection.connectToServer(service.getHost(), service.getPort());
    }

    public int getLocalPort(){
        return mConnection.getLocalPort();
    }


    public void sendMessage(String msg){
        if(mConnection != null){
            if(application.getConnectionMethod() == FinalVariables.WIFI_MODE) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragmentList.clear();
        if(currentFragment == 1)
            getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().popBackStack();
        currentFragment = 0;
        setupFragment();
    }

    //region Activity Overrides

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    protected void onPause() {
        new MyLog(TAG, "Pausing.");
        super.onPause();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
    }

    @Override
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        if(!connectionEstablished) {
            application.connectionTearDown();
            mConnection = null;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        currentFragment--;
        if(currentFragment < 0)
            finish();
        else
            super.onBackPressed();
    }

    //endregion
}
