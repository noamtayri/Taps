package com.android.ronoam.taps.Network.Connections;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.ronoam.taps.FinalVariables;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WifiConnection implements Connection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;
    private static final String TAG = "WifiConnection";
    private Socket mSocket;
    private int mPort = -1;

    private boolean isFirstMessage;

    public WifiConnection(Handler handler) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);

        isFirstMessage = true;
    }

    public void setHandler(Handler handler){
        mUpdateHandler = handler;
    }

    public void tearDown() {
        mChatServer.tearDown();
        if (mChatClient != null) {
            mChatClient.tearDown();
        }
    }
    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }
    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }
    public int getLocalPort() {
        return mPort;
    }
    private void setLocalPort(int port) {
        mPort = port;
    }
    private synchronized void updateMessages(String msg, boolean local, int what) {
        if(mUpdateHandler != null) {
            Message message = mUpdateHandler.obtainMessage(what);
            Log.e(TAG, "Updating message: " + msg);
            //if(msg != null) {
                if (local) {
                    message.arg1 = FinalVariables.FROM_MYSELF;
                } else {
                    message.arg1 = FinalVariables.FROM_OPPONENT;
                }
            //}
            if(isFirstMessage)
                isFirstMessage = false;
            Bundle messageBundle = new Bundle();
            messageBundle.putString("msg", msg);

            message.setData(messageBundle);
            mUpdateHandler.sendMessage(message);
        }
    }
    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }
    private Socket getSocket() {
        return mSocket;
    }
    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;
        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }
        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }
        class ServerThread implements Runnable {
            @Override
            public void run() {
                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        if(mServerSocket.isClosed())
                            return;
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }
    private class ChatClient {
        private InetAddress mAddress;
        private int PORT;
        private final String CLIENT_TAG = "ChatClient";
        private Thread mSendThread;
        private Thread mRecThread;
        public ChatClient(InetAddress address, int port) {
            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;
            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }
        class SendingThread implements Runnable {
            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;
            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }
            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");
                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }
                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();
                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }
                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }
        class ReceivingThread implements Runnable {
            @Override
            public void run() {
                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(
                            mSocket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {
                        String messageStr;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr);
                            updateMessages(messageStr, false, FinalVariables.MESSAGE_READ);
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!");
                            updateMessages(null, false, FinalVariables.MESSAGE_READ);
                            break;
                        }
                    }
                    input.close();
                } catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                    updateMessages(null, false, FinalVariables.MESSAGE_READ);
                }
            }
        }
        public void tearDown() {
            try {
                //mRecThread.interrupt();
                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }
        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                    updateMessages(null, true, FinalVariables.MESSAGE_WRITE);
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                    updateMessages(null, true, FinalVariables.MESSAGE_WRITE);
                }else {
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(getSocket().getOutputStream())), true);
                    out.println(msg);
                    out.flush();
                    if (isFirstMessage) {
                        updateMessages(msg, true, FinalVariables.MESSAGE_WRITE);
                        isFirstMessage = false;
                    }
                }
            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent message: " + msg);
        }
    }
}
