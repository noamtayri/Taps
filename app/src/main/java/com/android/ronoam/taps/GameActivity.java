package com.android.ronoam.taps;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.ronoam.taps.Fragments.CountDownFragment;
import com.android.ronoam.taps.Fragments.TapPvpFragment;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final String TAG = "Game";

    Bundle data;
    public Bundle dataFromFragment;
    int gameMode, currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        currentFragment = 0;
        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE);

        setupFragments();
    }

    private void setupFragments(){
        Bundle bundle = new Bundle();
        bundle.putInt(FinalVariables.GAME_MODE, gameMode);
        switch (gameMode){
            case FinalVariables.TAP_PVE:
                mFragmentList.add(new CountDownFragment());
                //mFragmentList.add(new TapPveFragment());
                break;
            case FinalVariables.TAP_PVP:
                mFragmentList.add(new CountDownFragment());
                mFragmentList.add(new TapPvpFragment());
                break;
            default:
                //mFragmentList.add(new ConnectionFragment());
                mFragmentList.add(new CountDownFragment());
            case FinalVariables.TAP_PVP_ONLINE:
                Fragment fragment = new TapPvpFragment();
                fragment.setArguments(bundle);
                mFragmentList.add(fragment);
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
            if (bundle != null)
                mFragmentList.get(currentFragment).setArguments(bundle);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_right_exit);
            fragmentTransaction.replace(R.id.game_fragment_container, mFragmentList.get(currentFragment));
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
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
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        new MyLog(TAG, "Being destroyed");
    }
}



