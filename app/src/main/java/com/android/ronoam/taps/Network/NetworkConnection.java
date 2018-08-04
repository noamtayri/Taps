package com.android.ronoam.taps.Network;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.Connections.WifiConnection;

import android.os.Handler;

import java.net.InetAddress;

public class NetworkConnection {

    private int mode;
    private WifiConnection wifiConnection;
    private BluetoothConnection bluetoothConnection;

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
            if(bluetoothConnection != null)
                bluetoothConnection.tearDown();
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
        }else{
            if(wifiConnection != null)
                wifiConnection.sendMessage(msg);
        }
    }
}