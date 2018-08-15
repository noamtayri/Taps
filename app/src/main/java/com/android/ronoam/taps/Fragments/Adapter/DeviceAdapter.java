package com.android.ronoam.taps.Fragments.Adapter;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.net.nsd.NsdServiceInfo;
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

import java.util.ArrayList;
import java.util.List;


abstract class MyDeviceViewHolder extends RecyclerView.ViewHolder{

    String serviceName;
    MyDeviceViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bind(Object object);

    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }
    public void clearAnimation(){
        itemView.clearAnimation();
    }
}
class BluetoothDeviceViewHolder extends MyDeviceViewHolder implements View.OnTouchListener{

    private Handler mHandler;
    private TextView deviceName;
    private BluetoothDevice device;

    BluetoothDeviceViewHolder(View itemView, Handler handler) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.card_device_name);
        mHandler = handler;
        itemView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if(device != null){
            Message message = null;
            Bundle bundle = new Bundle();
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(Color.DKGRAY);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_PRESSED);
                    bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
                    bundle.putString(FinalVariables.DEVICE_ADDRESS, device.getAddress());
                    message.setData(bundle);
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(Color.WHITE);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_RELEASED);
                    bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
                    bundle.putString(FinalVariables.DEVICE_ADDRESS, device.getAddress());
                    message.setData(bundle);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundColor(Color.WHITE);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_CANCELED);
                    break;
            }
            if(message != null)
                mHandler.sendMessage(message);
        }
        return true;
    }

    @Override
    public void bind(Object object) {
        if(object instanceof BluetoothDevice) {
            BluetoothDevice temp = (BluetoothDevice) object;
            deviceName.setText(temp.getName());
            this.device = temp;
        }
    }
}

class NsdDeviceViewHolder extends MyDeviceViewHolder implements View.OnTouchListener{

    private Handler mHandler;
    private TextView deviceName;
    private NsdServiceInfo device;

    NsdDeviceViewHolder(View itemView, Handler handler) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.card_device_name);
        mHandler = handler;
        itemView.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if(device != null){
            Message message = null;
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    v.setBackgroundColor(Color.DKGRAY);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_PRESSED);
                    message.obj = device;
                    break;
                case MotionEvent.ACTION_UP:
                    v.setBackgroundColor(Color.WHITE);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_RELEASED);
                    message.obj = device;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    v.setBackgroundColor(Color.WHITE);
                    message = mHandler.obtainMessage(FinalVariables.OPPONENT_CANCELED);
                    break;
            }
            if(message != null)
                mHandler.sendMessage(message);
        }
        return true;
    }

    @Override
    public void bind(Object object) {
        if(object instanceof NsdServiceInfo) {
            NsdServiceInfo temp = (NsdServiceInfo) object;
            deviceName.setText(temp.getServiceName().substring(serviceName.length()));
            this.device = temp;
        }
    }
}

public class DeviceAdapter extends RecyclerView.Adapter<MyDeviceViewHolder> {

    private Handler mHandler;
    private List<Object> devices;
    private String serviceNameWithoutDeviceName;

    private int connectionMode;
    //private int lastPosition = -1;

    public DeviceAdapter(Handler handler, int connectionMode) {
        this.connectionMode = connectionMode;
        this.devices = new ArrayList<>();
        mHandler = handler;
    }

    public DeviceAdapter(List<Object> devices, Handler handler) {
        this.connectionMode = FinalVariables.BLUETOOTH_MODE;
        this.devices = devices;
        this.mHandler = handler;
    }

    @NonNull
    @Override
    public MyDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_bluetooth_device, parent, false);

        if(connectionMode == FinalVariables.BLUETOOTH_MODE)
            return new BluetoothDeviceViewHolder(itemView, mHandler);
        else
            return new NsdDeviceViewHolder(itemView, mHandler);
    }



    @Override
    public void onBindViewHolder(@NonNull MyDeviceViewHolder holder, int position) {
        holder.setServiceName(this.serviceNameWithoutDeviceName);
        holder.bind(devices.get(position));
        setAnimation(holder.itemView);
    }

    @Override
    public int getItemCount() {
        if(devices != null)
            return devices.size();
        return 0;
    }

    public void add(Object device){
        devices.add(device);
        notifyDataSetChanged();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyDeviceViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.clearAnimation();
    }

    public void remove(NsdServiceInfo service){
        NsdServiceInfo temp;
        for (Object obj: devices) {
            temp = (NsdServiceInfo)obj;
            if(temp.getServiceName().equals(service.getServiceName())){
                devices.remove(obj);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void removeAll(){
        devices.clear();
        notifyDataSetChanged();
    }

    public boolean containsBluetooth(BluetoothDevice otherDevice){
        if(connectionMode == FinalVariables.BLUETOOTH_MODE) {
            for (Object device : devices) {
                BluetoothDevice temp = (BluetoothDevice) device;
                if (temp.getAddress().equals(otherDevice.getAddress()))
                    return true;
            }
        }
        return false;
    }

/*    public boolean containsNsd(NsdServiceInfo otherService){
        if(connectionMode == FinalVariables.WIFI_MODE) {
            for (Object device : devices) {
                NsdServiceInfo temp = (NsdServiceInfo) device;
                if (temp.getHost().getHostAddress().equals(otherService.getHost().getHostAddress()))
                    return true;
            }
        }
        return false;
    }*/

    private void setAnimation(View viewToAnimate){//, int position){
        //if(position > lastPosition) {
            Animation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(FinalVariables.HOME_SHOW_UI * 2);
            viewToAnimate.startAnimation(animation);
            //lastPosition = position;
        //}
    }

    public void setServiceName(String serviceName) {
        this.serviceNameWithoutDeviceName = serviceName;
    }
}
