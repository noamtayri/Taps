package com.android.ronoam.taps.Fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Fragments.Adapter.DeviceAdapter;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;

public class BluetoothConnectionSetupFragment extends Fragment {

    private static final String TAG = "DeviceList";
    private GameActivity activity;
    private BluetoothAdapter mBtAdapter;
    //private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private RecyclerView recyclerViewPaired, recyclerViewNewDevices;
    private DeviceAdapter mPairedAdapter, mNewDevicesAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Button scanButton;
    private Handler mHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_connection_setup, container, false);

        scanButton = view.findViewById(R.id.button_scan);
        recyclerViewPaired = view.findViewById(R.id.paired_devices);
        recyclerViewNewDevices = view.findViewById(R.id.new_devices);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        activity.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        setListener();
        initRecyclers();

        return view;
    }

    private void setHandler(){
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle data = msg.getData();
                String deviceName = data.getString(FinalVariables.DEVICE_NAME);
                String address = data.getString(FinalVariables.DEVICE_ADDRESS);


                return true;
            }
        });
    }

    private void setListener(){
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });
    }

    private void initRecyclers(){
        mLayoutManager = new LinearLayoutManager(activity);
        recyclerViewPaired.setLayoutManager(mLayoutManager);
        recyclerViewPaired.setHasFixedSize(true);

        // specify an adapter
        mPairedAdapter = new DeviceAdapter(new ArrayList<>(mBtAdapter.getBondedDevices()), mHandler);
        recyclerViewPaired.setAdapter(mPairedAdapter);

        recyclerViewNewDevices.setLayoutManager(mLayoutManager);
        recyclerViewNewDevices.setHasFixedSize(true);

        mNewDevicesAdapter = new DeviceAdapter(mHandler);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        new MyLog(TAG, "doDiscovery()");

        // Indicate scanning in the title
        new MyToast(activity, R.string.scanning);

        // Turn on sub-title for new devices
        recyclerViewNewDevices.setVisibility(View.VISIBLE);

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
        activity.unregisterReceiver(mReceiver);
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
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesAdapter.add(device);
                    //mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //setProgressBarIndeterminateVisibility(false);
                //setTitle(R.string.select_device);
                if (mNewDevicesAdapter.getItemCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    new MyToast(activity, noDevices);
                    //mNewDevicesAdapter.add(noDevices);
                }
            }
        }
    };

}
