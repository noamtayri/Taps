package com.android.ronoam.taps;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.android.ronoam.taps.Fragments.CountDownFragment;
import com.android.ronoam.taps.Fragments.HomeFragment;
import com.android.ronoam.taps.Fragments.TapPveFragment;
import com.android.ronoam.taps.Fragments.TapPvpFragment;
import com.android.ronoam.taps.Fragments.TypeFragment;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.Network.SetupConnectionLogic.StartGameLogic;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends AppCompatActivity {

    private final String TAG = "GameActivity";

    MyApplication application;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    int currentFragment;
    public boolean isOnline, isGameFinished;

    private MyViewModel model;
    private Handler mUpdateHandler, mHandler;
    NetworkConnection mConnection;
    public StartGameLogic startGameLogic;
    public int gameMode, language;
    public HomeFragment homeFragment;
    private boolean triedExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        application = (MyApplication)getApplication();
        isOnline = getIntent().getBooleanExtra(FinalVariables.ONLINE_GAME, false);
        homeFragment = new HomeFragment();
        setupFragments();

        setViewModel();

        if(isOnline){
            mConnection = application.getNetworkConnection();
            setConnectionHandler();
            startGameLogic = new StartGameLogic(this, mHandler);
        }
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
        if(isOnline)
            startGameLogic.removeObserver();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
        if(isOnline) {
            startGameLogic.registerObserver();
            application.setConnectionHandler(mUpdateHandler);
        }
    }

    @Override
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        super.onStop();
        application.setConnectionHandler(null);
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
        if(isOnline) {
            application.connectionTearDown();
            mConnection = null;
        }
    }

    //endregion

    public void startTapOnline(){
        gameMode = FinalVariables.TAP_PVP_ONLINE;
        startGameLogic.setMyChoice(gameMode);
    }

    public void startTypeOnline(){
        gameMode = FinalVariables.TYPE_PVP_ONLINE;
        startGameLogic.setMyChoice(gameMode);
    }

    private void setConnectionHandler(){
        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                model.setConnectionInMessages(msg);
                return true;
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case FinalVariables.I_EXIT:
                        new MyToast(GameActivity.this, "Exit game");
                        finish();
                        break;
                    case FinalVariables.OPPONENT_EXIT:
                        new MyToast(GameActivity.this, "Opponent disconnected");
                        finish();
                    case FinalVariables.NO_ERRORS:
                        setupGameFragment(msg.arg1);
                        moveToNextFragment(null);
                        break;
                }
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
                if (chatLine == null) {
                    new MyLog(TAG + "game", "null");
                    if (msg.arg1 == FinalVariables.FROM_OPPONENT) {
                        new MyToast(GameActivity.this, "Connection Lost");
                        stopGameWithError(FinalVariables.OPPONENT_EXIT, null);
                    }
                } else {
                    if(chatLine.equals(FinalVariables.GAME_INTERRUPTED)) {
                        new MyToast(GameActivity.this, "Opponent exits");
                        onGameFinished(null);
                    }
                    else
                        model.setInMessage(chatLine);
                    //new MyLog(TAG, chatLine);
                }
                return true;
            }
        });
        application.setConnectionHandler(mUpdateHandler);
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

    private void sendMessage(String s) {
        if(mConnection != null)
            mConnection.sendMessage(s);
        else{
            new MyToast(this, "Not Connected");
            finish();
        }
    }

    public NetworkConnection getConnection(){
        return mConnection;
    }

    private void setupFragments(){
        mFragmentList.add(homeFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.game_fragment_container, mFragmentList.get(0));
        fragmentTransaction.commit();

        fragmentTransaction.addToBackStack(null);
    }



    public void setupGameFragment(int game){
        application.hideSystemUI(getWindow().getDecorView());
        gameMode = game;
        isGameFinished = false;
        if(isOnline){
            startGameLogic.resetChoices();
            setGameHandler();
        }

        mFragmentList.add(new CountDownFragment());
        switch (game){
            case FinalVariables.TAP_PVE:
                mFragmentList.add(new TapPveFragment());
                break;
            case FinalVariables.TAP_PVP:
            case FinalVariables.TAP_PVP_ONLINE:
                mFragmentList.add(new TapPvpFragment());
                break;
            case FinalVariables.TYPE_PVE:
            case FinalVariables.TYPE_PVP_ONLINE:
                mFragmentList.add(new TypeFragment());
                break;
        }
    }

    public void moveToNextFragment(Bundle bundle){
        currentFragment++;
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
            //if(currentFragment != 1)
                fragmentTransaction.addToBackStack(null);
        }
    }

    private void stopGameWithError(final int exitCode, final Bundle bundle){
        new MyLog(TAG, "Stopping Game");
        /*if(isGameFinished)
            return;
*/
        isGameFinished = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new MyLog(TAG, "inside stop handler");
                if(exitCode == FinalVariables.NO_ERRORS && bundle != null)
                    onGameFinished(bundle);
                else {
                    Intent resIntent = new Intent(GameActivity.this, HomeActivity.class);
                    setResult(RESULT_CANCELED, resIntent);
                    finish();
                    //overridePendingTransition(R.anim.activity_slide_bottom_exit, android.R.anim.fade_out);
                }
            }
        }, FinalVariables.HOME_HIDE_UI);
    }

    private void onGameFinished(final Bundle bundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack();
        fragmentManager.popBackStack();
        currentFragment = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                homeFragment.onGameFinished(bundle);
            }
        }, 200);

        if(isOnline) {
            setConnectionHandler();
            application.setConnectionHandler(mUpdateHandler);
            startGameLogic.registerObserver();
        }
    }

    public void onGameInterrupted(){
        sendMessage(FinalVariables.GAME_INTERRUPTED);
        onGameFinished(null);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment == 0){
            ((HomeFragment)mFragmentList.get(0)).onBackPressed();
        } else if(!isGameFinished){
            if (triedExit) {
                onGameInterrupted();
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
}
