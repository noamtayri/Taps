package com.android.ronoam.taps.Fragments;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Fragments.Adapter.DeviceAdapter;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.MyApplication;
import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BluetoothConnectionSetupFragment extends Fragment {

    private static final String TAG = "DeviceList";

    private static final int REQUEST_ENABLE_BT = 3;
    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;

    private GameActivity activity;
    private BluetoothConnection mConnection;
    private BluetoothAdapter mBtAdapter;
    private String mConnectedDeviceName;
    //private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private RecyclerView recyclerViewPaired, recyclerViewNewDevices;
    private DeviceAdapter mPairedAdapter, mNewDevicesAdapter;
    TextView textViewStatus;
    Button scanButton;
    ProgressBar progressBar;
    private Handler mHandler;

    private boolean resolvedUnpairedDevice, firstMessage = true, wordsCreated, finishFragment;
    private int gameMode;
    MyViewModel model;
    Observer<Message> messageInObserver;
    BluetoothDevice mDevice;

    private List<String> words;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_connection_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        scanButton = view.findViewById(R.id.button_scan);
        progressBar = view.findViewById(R.id.progressBar_bluetooth_connection);
        textViewStatus = view.findViewById(R.id.textView_status_bluetooth_connection);
        recyclerViewPaired = view.findViewById(R.id.paired_devices);
        recyclerViewNewDevices = view.findViewById(R.id.new_devices);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, filter);
        mConnection = activity.getConnection().getBluetoothConnection();

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        gameMode = activity.gameMode;
        setHandler();
        setListener();

        messageInObserver = new Observer<Message>() {
            @Override
            public void onChanged(@Nullable Message message) {
                bluetoothInfoReceiver(message);
            }
        };
    }

    private void setHandler(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case FinalVariables.OPPONENT_PRESSED:
                        Bundle data = msg.getData();
                        String deviceName = data.getString(FinalVariables.DEVICE_NAME);
                        final String address = data.getString(FinalVariables.DEVICE_ADDRESS);
                        final BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
                        mDevice = device;
                        new MyLog("Handler", deviceName + " clicked");
                        mConnection.start(device);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mConnection.connect(device);
                            }
                        },500);
                        break;
                    case FinalVariables.OPPONENT_RELEASED:
                        if(!finishFragment){
                            if(isPaired(msg.getData().getString(FinalVariables.DEVICE_ADDRESS)))
                                mConnection.tearDown();
                        }
                        break;
                    case FinalVariables.OPPONENT_CANCELED:
                        if(!finishFragment)
                            mConnection.tearDown();
                        break;
                }

                /*boolean isPaired = true;
                for(BluetoothDevice device : mNewDevicesAdapter.getDevices()){
                    if(device.getAddress().equals(address))
                        isPaired = false;
                }
                meResolvedDevice = true;
                activity.connectToDevice(address);
                //mBtAdapter.cancelDiscovery();
                if(!isPaired){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new MyLog(TAG, "try again");
                            activity.connectToDevice(address);
                        }
                    }, 2500);
                }*/
                return true;
            }
        });
    }

    private boolean isPaired(String address){
        for(BluetoothDevice device : mNewDevicesAdapter.getDevices()){
            if(device.getAddress().equals(address))
                return false;
        }
        return true;
    }

    private void setListener(){
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    // Permission is not granted
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        new MyLog(TAG, "need to ask with explanation");
                        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                        alertBuilder.setCancelable(true);
                        alertBuilder.setTitle("location permission necessary");
                        alertBuilder.setMessage("Taps need coarse location permission find your opponents.");
                        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
                            }
                        });

                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        // Show an explanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                    } else {
                        // No explanation needed; request the permission
                        new MyLog(TAG, "need to ask");
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                MY_PERMISSIONS_REQUEST_COARSE_LOCATION);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                } else {
                    new MyLog(TAG, "permission granted");
                    ensureDiscoverable();
                    doDiscovery();
                    progressBar.setVisibility(View.VISIBLE);
                    // Permission has already been granted
                }

                //v.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 100);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new MyLog(TAG, "permission granted");
                    ensureDiscoverable();
                    doDiscovery();
                    progressBar.setVisibility(View.VISIBLE);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    new MyLog(TAG, "permission dismissed");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Updates the status on the status textView.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        setStatus(getString(resId));
    }

    /**
     * Updates the status on the status textView.
     *
     * @param text status
     */
    private void setStatus(CharSequence text) {
        textViewStatus.setText(text);
    }

    private void bluetoothInfoReceiver(Message msg){
        switch (msg.what) {
            case FinalVariables.MESSAGE_STATE_CHANGE:
                switch (msg.arg2) {
                    case BluetoothConnection.STATE_CONNECTED:
                        setStatus("status " + getString(R.string.title_connected_to) + mConnectedDeviceName);
                        //mConversationArrayAdapter.clear();
                        break;
                    case BluetoothConnection.STATE_CONNECTING:
                        setStatus(R.string.title_connecting);
                        break;
                    case BluetoothConnection.STATE_LISTEN:
                        setStatus(R.string.scanning);
                    case BluetoothConnection.STATE_NONE:
                        setStatus(R.string.title_not_connected);
                        break;
                }
                break;
            case FinalVariables.MESSAGE_WRITE:
                /*if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
                    typeMessageReceiver(msg);*/
                break;
            case FinalVariables.MESSAGE_READ:
                if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
                    receiveWords(msg);
                break;
            case FinalVariables.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(FinalVariables.DEVICE_NAME);
                model.setOpponentName(mConnectedDeviceName);
                new MyToast(activity, "Connected to " + mConnectedDeviceName);

                activity.connectionEstablished = true;
                if(gameMode == FinalVariables.TAP_PVP_ONLINE)
                    tapMessageReceiver(msg);
                else if(mDevice.getAddress().compareTo(android.provider.Settings.Secure.getString(
                        activity.getContentResolver(), "bluetooth_address")) < 0) {
                    sendWords();
                    finishFragment(FinalVariables.NO_ERRORS);
                }
                break;
            case FinalVariables.MESSAGE_TOAST:
                if (activity != null) {
                    new MyToast(activity, msg.getData().getString(FinalVariables.TOAST));
                }
                break;
        }
    }

    private void receiveWords(Message msg){
        if(msg.arg1 == FinalVariables.FROM_OPPONENT && !wordsCreated) {
            String chatLine = msg.getData().getString("msg");
            new MyLog(TAG, "received words");
            new MyLog(TAG, chatLine);
            //receive words
            words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
            wordsCreated = true;
            model.setWords(words);
            finishFragment(FinalVariables.NO_ERRORS);
        }
    }

    private void typeMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");
        if(msg.arg1 == FinalVariables.NETWORK_UNINITIALIZED){
           /*if(meResolvedDevice && firstMessage){
               sendWords();
           }*/
            firstMessage = false;
           return;
        }
        if(msg.arg1 == FinalVariables.FROM_MYSELF){
            if(chatLine == null) {
                finishFragment(FinalVariables.I_EXIT);
                new MyToast(activity, "Error resolving connection");
                return;
            }
        }
        if(msg.arg1 == FinalVariables.FROM_OPPONENT){
            if(chatLine == null) {
                new MyToast(activity, "Opponent disconnected");
                new MyLog(TAG, "Opponent disconnected");
                finishFragment(FinalVariables.OPPONENT_EXIT);
            }
            else if(!firstMessage && !wordsCreated) {
                new MyLog(TAG, "received words");
                new MyLog(TAG, chatLine);
                //receive words
                words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                wordsCreated = true;
                model.setWords(words);
                finishFragment(FinalVariables.NO_ERRORS);
            }
        }
    }

    private void tapMessageReceiver(Message msg) {
        String chatLine = msg.getData().getString("msg");

        activity.connectionEstablished = true;
        finishFragment(FinalVariables.NO_ERRORS);
    }

    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(getActivity(), activity.language);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            new MyLog(TAG, joinedStr);
            model.setWords(words);
            model.setOutMessage(joinedStr);
        }
    }

    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }

    private void finishFragment(final int code) {
        finishFragment = true;
        if (code == FinalVariables.NO_ERRORS) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.moveToNextFragment(null);
                }
            }, 1500);
        } else
            setFinishEntry(code);
    }

    private void setFinishEntry(final int code){
        model.setFinish(new MyEntry(code, null));
    }

    private void initRecyclers(){
        RecyclerView.LayoutManager mLayoutManagerPaired = new LinearLayoutManager(activity);
        recyclerViewPaired.setLayoutManager(mLayoutManagerPaired);
        recyclerViewPaired.setHasFixedSize(true);

        // specify an adapter
        List<BluetoothDevice> pairedListAll = new ArrayList<>(mBtAdapter.getBondedDevices());
        List<BluetoothDevice> pairedListForApp = new ArrayList<>();
        for (BluetoothDevice device : pairedListAll){
            if(getBTMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass()))
                pairedListForApp.add(device);
        }
        mPairedAdapter = new DeviceAdapter(pairedListForApp, mHandler);
        recyclerViewPaired.setAdapter(mPairedAdapter);

        RecyclerView.LayoutManager mLayoutManagerNew = new LinearLayoutManager(activity);
        recyclerViewNewDevices.setLayoutManager(mLayoutManagerNew);
        recyclerViewNewDevices.setHasFixedSize(true);

        mNewDevicesAdapter = new DeviceAdapter(mHandler);
        recyclerViewNewDevices.setAdapter(mNewDevicesAdapter);
    }

    private boolean getBTMajorDeviceClass(int major){
        switch(major){
            /*case BluetoothClass.Device.Major.AUDIO_VIDEO:
                return true;*/
            case BluetoothClass.Device.Major.PHONE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        new MyLog(TAG, "doDiscovery()");

        // Indicate scanning in the title
        new MyToast(activity, R.string.scanning);

        // Turn on sub-title for new devices
        //recyclerViewNewDevices.setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        activity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        model.getConnectionInMessages().observe(getActivity(), messageInObserver);
        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else{
            initRecyclers();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        model.getConnectionInMessages().removeObserver(messageInObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mConnection != null && !finishFragment)
            mConnection.tearDown();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*if(mConnection != null){
            if(mConnection.getState() == BluetoothConnection.STATE_NONE)
                mConnection.start();
            else{
                mConnection.tearDown();
                mConnection.start();
            }
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initRecyclers();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    new MyLog(TAG, "BT not enabled");
                    new MyToast(activity, R.string.bt_not_enabled_leaving);
                    setFinishEntry(FinalVariables.I_EXIT);
                }
                break;
        }
    }

    /*
    *//**
     * The on-click listener for all devices in the ListViews
     *//*
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };*/


    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                new MyToast(activity, "Found device " + device.getName());
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && getBTMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass())) {
                    mNewDevicesAdapter.add(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                progressBar.setVisibility(View.INVISIBLE);
                setStatus(R.string.scan_finished);
                String finish = getResources().getString(R.string.scan_finished);
                if (mNewDevicesAdapter.getItemCount() == 0) {
                    finish += "\n" + getResources().getString(R.string.none_found);
                    new MyToast(activity, finish);
                    //mNewDevicesAdapter.add(noDevices);
                }
            }
        }
    };

}
