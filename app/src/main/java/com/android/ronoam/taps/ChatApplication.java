package com.android.ronoam.taps;

import android.app.Application;

import com.android.ronoam.taps.Network.ChatConnection;

import android.os.Handler;
import android.view.View;


public class ChatApplication extends Application {

    ChatConnection chatConnection;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ChatConnection createChatConnection(Handler handler){
        if(chatConnection != null)
            chatConnection.tearDown();
        chatConnection = new ChatConnection(handler);
        return chatConnection;
    }

    public void setChatConnectionHandler(Handler handler){
        if(chatConnection != null)
            chatConnection.setHandler(handler);
    }

    public ChatConnection getChatConnection() {
        return chatConnection;
    }

    public void ChatConnectionTearDown(){
        if(chatConnection != null)
            chatConnection.tearDown();
        chatConnection = null;
    }


    public void hideSystemUI(View mDecorView) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        mDecorView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    public void showSystemUI(View mDecorView) {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
