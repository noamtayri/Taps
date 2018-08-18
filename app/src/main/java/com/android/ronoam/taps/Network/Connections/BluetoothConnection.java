/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ronoam.taps.Network.Connections;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.ronoam.taps.FinalVariables;
import com.android.ronoam.taps.Utils.FinalUtilsVariables;
import com.android.ronoam.taps.Utils.MyLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothConnection {
    // Debugging
    private static final String TAG = "BluetoothConnection";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothGameService";
    //private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private UUID myUUID;
    private static final UUID MY_UUID_TAP =
            UUID.fromString("a903844d-f5f3-4e30-bc17-c5dca6c68f12");
    private static final UUID MY_UUID_TYPE_ENG =
            UUID.fromString("570ea265-dc3e-464e-8c80-702e196401e0");
    private static final UUID MY_UUID_TYPE_HEB =
            UUID.fromString("b901f5b9-dfa9-497b-a7d4-4ea4efc6773b");

    // Member fields
    private BluetoothAdapter mAdapter;
    private Handler mUpdateHandler;
    private AcceptThread mSecureAcceptThread;
    //private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothConnection(Handler handler, int gameMode, int lang) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mUpdateHandler = handler;
        if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            if(lang == FinalUtilsVariables.HEBREW)
                myUUID = MY_UUID_TYPE_HEB;
            else
                myUUID = MY_UUID_TYPE_ENG;
        }
        else
            myUUID = MY_UUID_TAP;
    }

    public BluetoothConnection(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mUpdateHandler = handler;
        myUUID = MY_UUID_TAP;
    }

    public void setHandler(Handler handler){
        mUpdateHandler = handler;
    }

    private synchronized void updateMessages(String msg, boolean local, int what) {
        if(mUpdateHandler != null) {
            Message message = mUpdateHandler.obtainMessage(what);

            String logStr = msg != null ? msg : "null";
            Log.e(TAG, "Updating message: " + logStr);
            if (local) {
                message.arg1 = FinalVariables.FROM_MYSELF;
            } else {
                message.arg1 = FinalVariables.FROM_OPPONENT;
            }
            /*if(isFirstMessage)
                isFirstMessage = false;*/
            Bundle messageBundle = new Bundle();
            messageBundle.putString("msg", msg);

            message.setData(messageBundle);
            mUpdateHandler.sendMessage(message);
        }
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        //Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        if(mUpdateHandler != null)
            mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_STATE_CHANGE, -1, mNewState).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start(BluetoothDevice device) {
        new MyLog(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        /*if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(device);
            mSecureAcceptThread.start();
        }*/
        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        mSecureAcceptThread = new AcceptThread(device);
        mSecureAcceptThread.start();

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        new MyLog(TAG, "connect to: " + device.getName());

        if(mState == STATE_CONNECTED)
            return;
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, final BluetoothDevice
            device, final String socketType) {
        new MyLog(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }


        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();


        // Send the name of the connected device back to the UI Activity
        //Message msg = mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_DEVICE_NAME);
        Message msg = new Message();
        msg.what = FinalVariables.MESSAGE_DEVICE_NAME;
        Bundle bundle = new Bundle();
        bundle.putString(FinalVariables.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mUpdateHandler.sendMessage(msg);
        // Update UI title
        //updateUserInterfaceTitle();
    }

    /**
     * Stop all threads
     */
    public synchronized void tearDown() {
        new MyLog(TAG, "tearDown");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }


        mState = STATE_NONE;
        // Update UI title
        //updateUserInterfaceTitle();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The String to write
     * @see ConnectedThread#write(String)
     */
    public void sendMessage(String out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        new MyLog(TAG, "connection failed");
        // Send a failure message back to the Activity
        /*Message msg = mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(FinalVariables.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mUpdateHandler.sendMessage(msg);*/

        if(mSecureAcceptThread != null && mSecureAcceptThread.isAlive())
            mState = STATE_LISTEN;
        else
            mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        //BluetoothConnection.this.start();
        //BluetoothConnection.this.tearDown();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        new MyLog(TAG, "connection lost");
        if(mUpdateHandler != null) {
            // Send a failure message back to the Activity
            Message msg = mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(FinalVariables.TOAST, "Device connection was lost");
            msg.setData(bundle);
            mUpdateHandler.sendMessage(msg);
        }

        mState = STATE_NONE;
        // Update UI title
        //updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        //BluetoothConnection.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;
        private BluetoothDevice mDesiredDevice;

        public AcceptThread(BluetoothDevice device) {
            BluetoothServerSocket tmp = null;
            mSocketType = "Secure";
            mDesiredDevice = device;
            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, myUUID);

            } catch (IOException e) {
                new MyLog(TAG, "Socket Type: " + mSocketType + "listen() failed");
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run() {
            new MyLog(TAG, "Socket Type: " + mSocketType +
                    " BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                    new MyLog(TAG, "Socket Type: " + mSocketType + "accept() succeeded");
                } catch (IOException e) {
                    new MyLog(TAG, "Socket Type: " + mSocketType + "accept() failed");
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothConnection.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                new MyLog(TAG, "call connected");
                                // Situation normal. Start the connected thread.
                                if(mConnectThread != null)
                                    mConnectThread.cancel();
                                if(mConnectedThread != null)
                                    if(mConnectedThread.isAlive())
                                        break;
                                if(socket.getRemoteDevice().getAddress().equals(mDesiredDevice.getAddress()))
                                    connected(socket, socket.getRemoteDevice(),
                                            mSocketType);
                                break;
                            case STATE_NONE:
                                break;
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                new MyLog(TAG, "Already connected");
                                /*if(socket.getRemoteDevice().getAddress().equals(mDesiredDevice.getAddress()))
                                    connected(socket, socket.getRemoteDevice(),
                                        mSocketType);*/
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    new MyLog(TAG, "Could not close unwanted socket");
                                }
                                break;
                        }
                    }
                }
            }
            new MyLog(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            new MyLog(TAG, "Socket Type" + mSocketType + "cancel " + this);
            //this.interrupt();
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                new MyLog(TAG, "Socket Type" + mSocketType + "close() of server failed");
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = "Secure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(myUUID);

            } catch (IOException e) {
                new MyLog(TAG, "Socket Type: " + mSocketType + "create() failed");
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            new MyLog(TAG, "BEGIN mConnectThread SocketType: " + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    new MyLog(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure");
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnection.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                new MyLog(TAG, "close() of connect " + mSocketType + " socket failed");
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket, String socketType) {
            new MyLog(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                new MyLog(TAG, "temp sockets not created");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            new MyLog(TAG, "BEGIN mConnectedThread");
            //byte[] buffer = new byte[1024];
            //int bytes;

            // Keep listening to the InputStream while connected
            BufferedReader input = new BufferedReader(new InputStreamReader(
                    mmInStream));

            String messageStr;
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    //bytes = mmInStream.read(buffer);
                    messageStr = input.readLine();

                    updateMessages(messageStr, false, FinalVariables.MESSAGE_READ);
                    new MyLog(TAG, "connected received: " + messageStr);
                    //buffer = new byte[1024];
                    // Send the obtained bytes to the UI Activity
                    /*mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/
                } catch (IOException e) {
                    //new MyLog(TAG, "disconnected");
                    new MyLog(TAG, "connected failed received: null");
                    updateMessages(null, false, FinalVariables.MESSAGE_READ);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param msg The String to write
         */
        public void write(String msg) {
            try {
                //mmOutStream.write(buffer);

                if (mmSocket == null) {
                    new MyLog(TAG, "Socket is null, wtf?");
                    updateMessages(null, true, FinalVariables.MESSAGE_WRITE);
                } else if (mmSocket.getOutputStream() == null) {
                    new MyLog(TAG, "Socket output stream is null, wtf?");
                    updateMessages(null, true, FinalVariables.MESSAGE_WRITE);
                }else {
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(mmOutStream)), true);
                    out.println(msg);
                    out.flush();

                    updateMessages(msg, true, FinalVariables.MESSAGE_WRITE);

                    // Share the sent message back to the UI Activity
                /*mUpdateHandler.obtainMessage(FinalVariables.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();*/
                }
            } catch (IOException e) {
                updateMessages(null, false, FinalVariables.MESSAGE_WRITE);
                new MyLog(TAG, "Exception during write");
                //new MyLog(TAG, "connected sent: null");
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                new MyLog(TAG, "close() of connect socket failed");
            }
        }
    }
}