package com.android.ronoam.taps;

import android.content.Intent;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.net.InetAddress;

public class TapPvpOnlineActivity extends AppCompatActivity {

    private TextView mMessageTextView;
    private Handler mUpdateHandler;
    public static final String TAG = "NsdChat";
    ChatConnection mConnection;

    private InetAddress hostAddress;
    private int hostPort = -1;
    int count = 0;

    ChatApplication application;
    Bundle data;

    boolean tryedExit;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tap_pvp_online);
        new MyLog(TAG, "Creating tap_pvp activity");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //mStatusTextView = findViewById(R.id.textView_status_connection_online);
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
                //return true;
            }
        };
        /*{
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
            }
        };
        */
        mMessageTextView = findViewById(R.id.textView_message_tap_pvp);

        data = getIntent().getExtras();
        hostAddress = data.getParcelable(FinalVariables.EXTRA_HOST);
        hostPort = data.getInt(FinalVariables.EXTRA_PORT);

        mConnection = ChatConnection.getInstance(mUpdateHandler);
        getConnection();
    }

    public void getConnection() {
        application.setChatConnectionHandler(mUpdateHandler);
        mConnection = application.getChatConnection();
        /*//NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (hostAddress != null && hostPort != -1) {
            new MyLog(TAG, "Connecting.");
            mConnection.connectToServer(hostAddress, hostPort);
        } else {
            new MyLog(TAG, "No service to connect to!");
        }*/
    }
    public void clickSend(View v) {
        if(mConnection.getSocket() != null) {
            String messageString = String.valueOf(count++);
            mConnection.sendMessage(messageString);
        }
        else
            new MyToast(this, "Not Connected");
    }

    public void addChatLine(String line) {
        mMessageTextView.setText(line);
    }

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        super.onStart();
    }
    @Override
    protected void onPause() {
        new MyLog(TAG, "Pausing.");
        super.onPause();
    }
    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();

        tryedExit = false;
    }
    // For KitKat and earlier releases, it is necessary to remove the
    // service registration when the application is stopped.  There's
    // no guarantee that the onDestroy() method will be called (we're
    // killable after onStop() returns) and the NSD service won't remove
    // the registration for us if we're killed.
    // In L and later, NsdService will automatically unregister us when
    // our connection goes away when we're killed, so this step is
    // optional (but recommended).
    @Override
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        application.ChatConnectionTearDown();
        //mConnection.tearDown();
        mConnection = null;
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(tryedExit) {
            super.onBackPressed();
        }
        else{
            new MyToast(this, R.string.before_exit);
            tryedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tryedExit = false;
                }
            }, 1500);
        }
    }
}
