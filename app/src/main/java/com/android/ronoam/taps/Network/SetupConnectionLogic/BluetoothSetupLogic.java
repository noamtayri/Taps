package com.android.ronoam.taps.Network.SetupConnectionLogic;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.GameActivity;
import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.MyViewModel;
import com.android.ronoam.taps.Network.NetworkConnection;
import com.android.ronoam.taps.R;
import com.android.ronoam.taps.Utils.MyLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * class that handles bluetooth setup connection
 * observes network incoming messages with {@link #model}
 * detects {@link android.view.MotionEvent#ACTION_DOWN}
 *         {@link android.view.MotionEvent#ACTION_UP}
 *         {@link android.view.MotionEvent#ACTION_CANCEL}
 *         {@link android.view.MotionEvent}
 * with {@link #mAdapterHandler}
 * and set connection setup messages by order*/
public class BluetoothSetupLogic {
    private final String TAG = "wifiSetupLogic";


    private GameActivity activity;
    private Handler mHandler, mAdapterHandler;
    private MyViewModel model;
    private final Observer<Message> messageInObserver;
    private boolean isConnectionEstablished, wordsCreated;
    private String mConnectedDeviceName;
    private NetworkConnection mConnection;

    //private int gameMode;
    private List<String> words;
    private BluetoothDevice mDevice;

    public BluetoothSetupLogic(GameActivity activity, Handler handler){
        this.activity = activity;
        this.mHandler = handler;
        int gameMode = activity.gameMode;
        model = ViewModelProviders.of(activity).get(MyViewModel.class);
        mConnection = activity.getConnection();

        isConnectionEstablished = false;
        wordsCreated = false;

        setAdaptersHandler();

        if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            messageInObserver = new Observer<Message>() {
                @Override
                public void onChanged(@Nullable Message message) {
                    if(message != null)
                        typeMessageReceiver(message);
                }
            };
        } else{
            messageInObserver = new Observer<Message>() {
                @Override
                public void onChanged(@Nullable Message message) {
                    if(message != null)
                        tapMessageReceiver(message);
                }
            };
        }
    }

    public void registerObserver(){
        model.getConnectionInMessages().observe(activity, messageInObserver);
    }

    public void removeObserver(){
        model.getConnectionInMessages().removeObserver(messageInObserver);
    }

    /**
     * Tap Bluetooth initiating protocol. detect opponent connecting and finish
     * @param msg the message received from {@link #model}
     */
    private void tapMessageReceiver(Message msg){
        switch (msg.what) {
            case FinalVariables.MESSAGE_STATE_CHANGE:
                messageStateChanged(msg);
                break;
            case FinalVariables.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(FinalVariables.DEVICE_NAME);
                model.setOpponentName(mConnectedDeviceName);
                //new MyToast(activity, "Connected to " + mConnectedDeviceName);

                activity.connectionEstablished = true;
                isConnectionEstablished = true;

                mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
                break;
            case FinalVariables.MESSAGE_TOAST:
                mHandler.sendMessage(msg);
                break;
        }
    }

    /**
     * Type Bluetooth initiating protocol. share my name, send or receive {@link #words} and finish
     * @param msg the message received from {@link #model}
     */
    private void typeMessageReceiver(Message msg){
        switch (msg.what) {
            case FinalVariables.MESSAGE_STATE_CHANGE:
                messageStateChanged(msg);
                break;
            /*case FinalVariables.MESSAGE_WRITE:
                break;*/
            case FinalVariables.MESSAGE_READ:
                receiveWords(msg);
                break;
            case FinalVariables.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(FinalVariables.DEVICE_NAME);
                model.setOpponentName(mConnectedDeviceName);
                //mHandler.obtainMessage(FinalVariables.MESSAGE_DEVICE_NAME, mConnectedDeviceName);
                activity.connectionEstablished = true;
                isConnectionEstablished = true;
                //mConnection.stopAsyncConnect();
                checkSendWords();
                break;
            case FinalVariables.MESSAGE_TOAST:
                mHandler.sendMessage(msg);
                break;
        }
    }

    /**
     * determine who will create and send {@link #words} to the other
     * using {@link #compareNames()} for compare between the devices bluetooth names.
     */
    private void checkSendWords() {
        if(compareNames() < 0) {
            sendWords();
            mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
        }
    }

    /**
     * get the handler for handling {@link android.view.MotionEvent} events
     * @see #setAdaptersHandler()
     * @return the handler
     */
    public Handler getAdapterHandler(){
        return mAdapterHandler;
    }

    /**
     * set the handler for handling {@link android.view.MotionEvent} events
     * from {@link com.android.ronoam.taps.Fragments.BluetoothConnectionSetupFragment#mPairedAdapter}
     * and  {@link com.android.ronoam.taps.Fragments.BluetoothConnectionSetupFragment#mNewDevicesAdapter}
     */
    private void setAdaptersHandler(){
        mAdapterHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(isConnectionEstablished)
                    return true;
                switch (msg.what){
                    case FinalVariables.OPPONENT_PRESSED:
                        Bundle data = msg.getData();
                        //String deviceName = data.getString(FinalVariables.DEVICE_NAME);
                        String addressPressed = data.getString(FinalVariables.DEVICE_ADDRESS);
                        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addressPressed);
                        startListeningForDevice(device);
                        break;
                    case FinalVariables.OPPONENT_RELEASED:
                        String addressReleased = msg.getData().getString(FinalVariables.DEVICE_ADDRESS);
                        if(!isPaired(addressReleased) && mDevice.getAddress().equals(addressReleased))
                            break;
                    case FinalVariables.OPPONENT_CANCELED:
                        if(!isConnectionEstablished)
                            stopListeningForDevice();
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Helper method for {@link #tapMessageReceiver(Message)}
     * and {@link #typeMessageReceiver(Message)}
     * @param msg input message from {@link #model}
     */
    private void messageStateChanged(Message msg){
        String setStatus = null;
        switch (msg.arg2) {
            case BluetoothConnection.STATE_CONNECTED:
                setStatus = activity.getString(R.string.title_connected_to) + mConnectedDeviceName;
                break;
            case BluetoothConnection.STATE_CONNECTING:
                setStatus = activity.getString(R.string.title_connecting);
                break;
            case BluetoothConnection.STATE_LISTEN:
                setStatus = activity.getString(R.string.listen);
                break;
            case BluetoothConnection.STATE_NONE:
                setStatus = activity.getString(R.string.select_device);
                break;
        }
        if(setStatus != null) {
            Message message = mHandler.obtainMessage(FinalVariables.MESSAGE_STATE_CHANGE);
            message.obj = setStatus;
            message.sendToTarget();
        }
    }

    /**
     * @see #connectToDevice(BluetoothDevice)
     * @param device the potentially last connected device
     */
    private void startListeningForDevice(BluetoothDevice device){
        mDevice = device;
        connectToDevice(device);
    }

    /**
     * stop the async task trying to connect to chosen device {@link NetworkConnection#myAsyncBluetoothConnect}
     * tear down the communication threads with {@link NetworkConnection#tearDown()}
     */
    private void stopListeningForDevice(){
        new MyLog(TAG, "stop listening");
        mConnection.stopAsyncConnect();
        mConnection.tearDown();
    }

    /**
     * check if {@param address} remote device is paired to this device
     * using {@link BluetoothAdapter#getDefaultAdapter()} bonded devices
     * @param address to be checked
     * @return true if this address is of paired device, false otherwise
     */
    private boolean isPaired(String address){
        for(BluetoothDevice device : BluetoothAdapter.getDefaultAdapter().getBondedDevices()){
            if(device.getAddress().equals(address))
                return true;
        }
        return false;
    }

    /**
     * @see NetworkConnection#startListening(BluetoothDevice)
     * check if {@link #compareNames()} return value.
     * if true, start an async task from connection
     * @see NetworkConnection#startAsyncConnect(BluetoothDevice)
     * @param device bluetooth device to connect to
     */
    private void connectToDevice(BluetoothDevice device){
        mConnection.startListening(device);
        if(compareNames() < 0)
            mConnection.startAsyncConnect(device);
    }

    /**
     * compare my bluetooth name vs opponent's.
     * tried to use bluetooth MAC address comparing
     * but not working on android Oreo and above
     * needs {@link android.Manifest} android.permission.LOCAL_MAC_ADDRESS
     * @return {@link String#compareTo(String)} value
     */
    private int compareNames(){
        /*
            Not working on android Oreo and above
            if(device.getAddress().compareTo(android.provider.Settings.Secure.getString(
                activity.getContentResolver(), "bluetooth_address")) < 0)*/
        return mDevice.getName().compareTo(BluetoothAdapter.getDefaultAdapter().getName());
    }

    /**
     * @see #typeMessageReceiver(Message)
     * receiving words from the opponent
     * @param msg received from {@link #model}
     */
    private void receiveWords(Message msg){
        if(msg.arg1 == FinalVariables.FROM_OPPONENT && !wordsCreated) {
            String chatLine = msg.getData().getString("msg");
            //receive words
            if(chatLine != null) {
                new MyLog(TAG, "received words");
                new MyLog(TAG, chatLine);
                words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                wordsCreated = true;
                model.setWords(words);
                mHandler.obtainMessage(FinalVariables.NO_ERRORS).sendToTarget();
            }
        }
    }

    /**
     * @see #joinList(List)
     * create {@link #words} and send it to the opponent
     * also save it with {@link #model} for the next fragments to use.
     */
    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(activity, activity.language);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            new MyLog(TAG, joinedStr);
            model.setWords(words);
            model.setOutMessage(joinedStr);
        }
    }

    /**
     * Collapsing {@link #words} to one long String with commas ',' between every word
     * @param words the list to join
     * @return the merged string
     */
    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");
        return joinedStr;
    }
}
