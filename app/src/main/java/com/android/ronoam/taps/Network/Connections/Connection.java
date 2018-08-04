package com.android.ronoam.taps.Network.Connections;

import android.os.Handler;

public interface Connection {

    void setHandler(Handler handler);
    void tearDown();
    void sendMessage(String msg);
}
