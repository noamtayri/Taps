package com.android.ronoam.taps.Fragments;

import android.Manifest;
import android.app.Activity;
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
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.Network.SetupConnectionLogic.BluetoothSetupLogic;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyEntry;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.List;

public class BluetoothConnectionSetupFragment extends Fragment {

    private static final String TAG = "BluetoothSetupFragment";

    private static final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private GameActivity activity;
    private NetworkConnection mConnection;
    private BluetoothAdapter mBtAdapter;

    private RecyclerView recyclerViewPaired, recyclerViewNewDevices;
    private DeviceAdapter mPairedAdapter, mNewDevicesAdapter;
    TextView textViewStatus;
    Button scanButton;
    ProgressBar progressBar;


    MyViewModel model;

    private BluetoothSetupLogic connectionLogic;

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

        model = ViewModelProviders.of(activity).get(MyViewModel.class);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, filter);
        mConnection = activity.getConnection();

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        connectionLogic = new BluetoothSetupLogic(activity, mHandler);
        setListener();

    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case FinalVariables.MESSAGE_STATE_CHANGE:
                    setStatus((String)msg.obj);
                    break;
                /*case FinalVariables.MESSAGE_DEVICE_NAME:
                    //new MyToast(activity, "Connected to " + String.valueOf(msg.obj));
                    break;*/
                case FinalVariables.MESSAGE_TOAST:
                    new MyToast(activity, msg.getData().getString(FinalVariables.TOAST));
                    break;
                case FinalVariables.NO_ERRORS:
                    new MyToast(activity, "Connected to " + model.getOpponentName().getValue());
                    finishFragment(FinalVariables.NO_ERRORS);
            }
            return true;
        }
    });

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
                    // Permission has already been granted
                }

                //v.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Makes this device discoverable for 90 seconds (1.5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 90);
            startActivityForResult(discoverableIntent, REQUEST_ENABLE_DISCOVERABLE);
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


    private void finishFragment(final int code) {
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
        mPairedAdapter = new DeviceAdapter(pairedListForApp, connectionLogic.getAdapterHandler());
        recyclerViewPaired.setAdapter(mPairedAdapter);

        RecyclerView.LayoutManager mLayoutManagerNew = new LinearLayoutManager(activity);
        recyclerViewNewDevices.setLayoutManager(mLayoutManagerNew);
        recyclerViewNewDevices.setHasFixedSize(true);

        mNewDevicesAdapter = new DeviceAdapter(connectionLogic.getAdapterHandler());
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
        if(mBtAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            progressBar.setVisibility(View.VISIBLE);

            new MyLog(TAG, "doDiscovery()");

            // Indicate scanning in the title
            new MyToast(activity, R.string.scanning);

            // If we're already discovering, stop it
            if (mBtAdapter.isDiscovering()) {
                mBtAdapter.cancelDiscovery();
            }

            // Request discover from BluetoothAdapter
            mBtAdapter.startDiscovery();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (GameActivity)getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        activity.unregisterReceiver(mReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectionLogic.registerObserver();
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
        new MyLog(TAG, "onStop");
        super.onStop();
        connectionLogic.removeObserver();
        if(mConnection != null && !activity.connectionEstablished)
            mConnection.tearDown();
        //activity.cancelAsyncConnect();
        /*if(myAsyncConnect != null)
            myAsyncConnect.cancel(false);*/
    }

    @Override
    public void onResume() {
        super.onResume();
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
            case REQUEST_ENABLE_DISCOVERABLE:
                new MyLog(TAG, "results from discovery intent");
                doDiscovery();
        }
    }

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
                //new MyToast(activity, "Found device " + device.getName());
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && getBTMajorDeviceClass(device.getBluetoothClass().getMajorDeviceClass())) {
                    if(!mNewDevicesAdapter.contains(device))
                        mNewDevicesAdapter.add(device);
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
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