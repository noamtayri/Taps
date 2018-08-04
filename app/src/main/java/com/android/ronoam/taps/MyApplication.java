package com.android.ronoam.taps;

import android.app.Application;

import com.android.ronoam.taps.Network.Connections.BluetoothConnection;
import com.android.ronoam.taps.Network.Connections.Connection;
import com.android.ronoam.taps.Network.Connections.WifiConnection;
import com.android.ronoam.taps.Network.NetworkConnection;

import android.os.Handler;
import android.view.View;


public class MyApplication extends Application {

    NetworkConnection networkConnection;
    int connectionMethod;

    @Override
    public void onCreate() {
        super.onCreate();
        connectionMethod = -1;
    }

    public void setConnectionMethod(int method){
        connectionMethod = method;
    }

    public NetworkConnection createNetworkConnection(Handler handler){
        if(networkConnection != null)
            networkConnection.tearDown();
        networkConnection = new NetworkConnection(handler, connectionMethod);
        return networkConnection;
    }

    public void setConnectionHandler(Handler handler){
        if(networkConnection != null)
            networkConnection.setHandler(handler);
    }

    public void connectionTearDown(){
        if(networkConnection != null)
            networkConnection.tearDown();
        networkConnection = null;
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
