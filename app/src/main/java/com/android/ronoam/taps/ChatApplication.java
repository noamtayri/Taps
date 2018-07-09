package com.android.ronoam.taps;

import android.app.Application;

import com.android.ronoam.taps.Network.ChatConnection;

import android.os.Handler;

import java.net.InetAddress;

public class ChatApplication extends Application {

    ChatConnection chatConnection;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void createChatConnection(Handler handler){
        chatConnection = new ChatConnection(handler);
    }

    public void setChatConnectionHandler(Handler handler){
        if(chatConnection != null)
            chatConnection.setHandler(handler);
    }

    public void setChatClient(InetAddress hostAddress, int hostPort){
        //chatConnection = new ChatConnection(handler);
        if (hostAddress != null && hostPort >= 0)
            chatConnection.connectToServer(hostAddress, hostPort);
    }

    public ChatConnection getChatConnection() {
        return chatConnection;
    }

    public void ChatConnectionTearDown(){
        chatConnection.tearDown();
    }
}
