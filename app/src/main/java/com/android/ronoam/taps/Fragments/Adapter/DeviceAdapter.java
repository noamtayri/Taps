package com.android.ronoam.taps.Fragments.Adapter;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.ArrayList;
import java.util.List;

class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener{

    private Handler mHandler;
    public TextView deviceName;
    public BluetoothDevice device;

    public DeviceViewHolder(View itemView, Handler handler) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.card_device_name);
        mHandler = handler;
        itemView.setOnTouchListener(this);
    }


    @Override
    public void onClick(View v) {
        if(device != null){
            new MyLog("Holder", device.getName() + " item clicked");
            Message message = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
            bundle.putString(FinalVariables.DEVICE_ADDRESS, device.getAddress());
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if(device != null){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(Color.DKGRAY);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundColor(Color.WHITE);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(Color.WHITE);
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
                    bundle.putString(FinalVariables.DEVICE_ADDRESS, device.getAddress());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
            }
        }
        return true;
    }
}

public class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private Handler mHandler;
    private List<BluetoothDevice> devices;

    public DeviceAdapter(Handler handler) {
        this.devices = new ArrayList<>();
        mHandler = handler;
    }

    public DeviceAdapter(List<BluetoothDevice> devices, Handler handler) {
        this.devices = devices;
        this.mHandler = handler;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_bluetooth_device, parent, false);
        Animation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(FinalVariables.HOME_SHOW_UI * 2);
        itemView.startAnimation(animation);
        return new DeviceViewHolder(itemView, mHandler);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.deviceName.setText(devices.get(position).getName());
        holder.device = devices.get(position);
    }

    @Override
    public int getItemCount() {
        if(devices != null)
            return devices.size();
        return 0;
    }

    public List<BluetoothDevice> getDevices(){
        return devices;
    }

    public void add(BluetoothDevice device){
        devices.add(device);
        notifyDataSetChanged();
    }
}
