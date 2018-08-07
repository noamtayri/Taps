package com.android.ronoam.taps.Network;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.Connections.WifiConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

import java.net.InetAddress;

public class NetworkConnection {

    private int mode;
    private WifiConnection wifiConnection;
    private BluetoothConnection bluetoothConnection;

    public NetworkConnection(Handler handler, int connectionMethod, int gameMode, int language){
        mode = connectionMethod;
        if(mode == FinalVariables.BLUETOOTH_MODE){
            bluetoothConnection = new BluetoothConnection(handler, gameMode, language);
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

    public void connectToDevice(String address){
        if(bluetoothConnection != null){
            BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
            bluetoothConnection.connect(device);
        }
    }

    public int getLocalPort(){
        if(wifiConnection != null)
            return wifiConnection.getLocalPort();
        else
            return -1;
    }

    public BluetoothConnection getBluetoothConnection() {
        return bluetoothConnection;
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
