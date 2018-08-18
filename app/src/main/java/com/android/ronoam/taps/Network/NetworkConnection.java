package com.android.ronoam.taps.Network;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Handler;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.Connections.WifiConnection;

import java.net.InetAddress;

public class NetworkConnection {

    private int mode;
    private WifiConnection wifiConnection;
    private BluetoothConnection bluetoothConnection;
    //private BluetoothDevice mDevice;
    private AsyncTask myAsyncConnect;
    private boolean finishAsync = false;

    private BluetoothDevice otherDevice;
    private NsdServiceInfo myService, otherService;


    public NetworkConnection(Handler handler, int connectionMethod, int gameMode, int language){
        mode = connectionMethod;
        if(mode == FinalVariables.BLUETOOTH_MODE){
            bluetoothConnection = new BluetoothConnection(handler, gameMode, language);
        }else{
            wifiConnection = new WifiConnection(handler);
        }
    }

    public NetworkConnection(Handler handler, int connectionMethod){
        mode = connectionMethod;
        if(mode == FinalVariables.BLUETOOTH_MODE){
            bluetoothConnection = new BluetoothConnection(handler);
        }else{
            wifiConnection = new WifiConnection(handler);
        }
    }

    public void tearDown(){
        if(mode == FinalVariables.BLUETOOTH_MODE) {
            if(bluetoothConnection != null) {
                stopAsyncConnect();
                bluetoothConnection.tearDown();
            }
        }
        else {
            if(wifiConnection != null)
                wifiConnection.tearDown();
        }
    }

    public void setHandler(Handler handler){
        if(mode == FinalVariables.BLUETOOTH_MODE) {
            if(bluetoothConnection != null)
                bluetoothConnection.setHandler(handler);
        }
        else {
            if(wifiConnection != null)
                wifiConnection.setHandler(handler);
        }
    }

    public void connectToServer(InetAddress address, int port){
        if(wifiConnection != null)
            wifiConnection.connectToServer(address, port);
    }

    public WifiConnection getWifiConnection(){
        return wifiConnection;
    }

    /*public void connectToDevice(String address){
        if(bluetoothConnection != null){
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            bluetoothConnection.connect(device);
        }
    }*/

    public void startListening(BluetoothDevice device) {
        otherDevice = device;
        if(bluetoothConnection!= null) {
            bluetoothConnection.start(device);
        }
    }

    public void startListening(NsdServiceInfo service) {
        otherService = service;
        if(wifiConnection!= null) {
            wifiConnection.setDesiredDevice(service.getHost());
        }
    }

    public void stopListening(){
        if(wifiConnection != null){
            wifiConnection.setDesiredDevice(null);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void startAsyncConnect(final BluetoothDevice device){
        otherDevice = device;
        final long duration = 1000;
        if(myAsyncConnect != null)
            myAsyncConnect.cancel(false);
        finishAsync = false;
        myAsyncConnect = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(duration);
                    if (!finishAsync && bluetoothConnection.getState() <= BluetoothConnection.STATE_CONNECTING
                            && !isCancelled())
                        bluetoothConnection.connect(device);
                    else return null;
                    while(!isCancelled()){
                        Thread.sleep(duration * 3);
                        if (!finishAsync && bluetoothConnection.getState() <= BluetoothConnection.STATE_CONNECTING
                                && !isCancelled())
                            bluetoothConnection.connect(device);
                        else break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }

    public void stopAsyncConnect(){
        finishAsync = true;
        if(myAsyncConnect != null) {
            myAsyncConnect.cancel(false);
            myAsyncConnect = null;
        }
    }

    public int getLocalPort(){
        if(wifiConnection != null)
            return wifiConnection.getLocalPort();
        else
            return -1;
    }

    public void sendMessage(String msg){
        if(mode == FinalVariables.BLUETOOTH_MODE){
            if(bluetoothConnection != null)
                bluetoothConnection.sendMessage(msg);
        }else if(wifiConnection != null && wifiConnection.getLocalPort() > -1){
            wifiConnection.sendMessage(msg);
        }
    }

    public int compareNames(){
        if(mode == FinalVariables.BLUETOOTH_MODE && bluetoothConnection != null){
            return BluetoothAdapter.getDefaultAdapter().getName().compareTo(otherDevice.getName());
        } else if(mode == FinalVariables.WIFI_MODE && wifiConnection != null){
            return myService.getServiceName().compareTo(otherService.getServiceName());
        }
        return 0;
    }

    public void setMyService(NsdServiceInfo service){
        myService = service;
    }
}
