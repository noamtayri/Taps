package com.android.ronoam.taps.Fragments.Adapter;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.R;

import java.util.List;

class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private Handler mHandler;
    public TextView deviceName;
    public BluetoothDevice device;

    public DeviceViewHolder(View itemView, Handler handler) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.card_device_name);
        mHandler = handler;
    }

    @Override
    public void onClick(View v) {
        if(device != null){
            Message message = mHandler.obtainMessage();
            Bundle bundle = new Bundle();
            bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
            bundle.putString(FinalVariables.DEVICE_ADDRESS, device.getAddress());
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }
}

public class DeviceAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private Handler mHandler;
    private List<BluetoothDevice> devices;

    public DeviceAdapter(Handler handler) {
        super();
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
        return new DeviceViewHolder(itemView, mHandler);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        holder.deviceName.setText(devices.get(position).getName());
        holder.device = devices.get(position);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void add(BluetoothDevice device){
        devices.add(device);
        notifyDataSetChanged();
    }
}
