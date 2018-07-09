package com.android.ronoam.taps;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

public class ConnectionOnlineActivity extends AppCompatActivity {

    NsdHelper mNsdHelper;
    private TextView mStatusTextView;
    private Handler mUpdateHandler;
    public static final String TAG = "NsdChat";
    ChatConnection mConnection;
    ChatApplication application;

    AsyncTaskCheckStatus mAsyncTask;
    boolean tryedExit;

    Bundle data;
    long timeBeforeExit;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ChatApplication) getApplication();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        new MyLog(TAG, "Creating chat activity");
        setContentView(R.layout.activity_connection_online);
        mStatusTextView = findViewById(R.id.textView_status_connection_online);
        mUpdateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                addChatLine(chatLine);
                application.setChatConnectionHandler(null);
                startGameDelayed();
            }
        };

        data = getIntent().getExtras();
        timeBeforeExit = data.getLong(FinalVariables.TIME_BEFORE_FINISH);
    }

    private void startGameDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ConnectionOnlineActivity.this, CountDownActivity.class);
                if(timeBeforeExit == FinalVariables.TIMER_LIMIT) {
                    intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP_ONLINE);
                } else if(timeBeforeExit == FinalVariables.KEYBORAD_GAME_TIME){
                    intent.putExtra(FinalVariables.GAME_MODE, FinalVariables.TYPE_PVP_ONLINE);
                }
                NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
                if(service != null) {
                    intent.putExtra(FinalVariables.EXTRA_HOST, service.getHost());
                    intent.putExtra(FinalVariables.EXTRA_PORT, service.getPort());
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                //finish();
            }
        }, 2000);
        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, timeBeforeExit + 1000);*/
    }

    public void clickAdvertise(View v) {
        // Register service
        if(mConnection.getLocalPort() > -1) {
            mNsdHelper.registerService(mConnection.getLocalPort());
        } else {
            new MyLog(TAG, "ServerSocket isn't bound.");
        }
    }
    public void clickDiscover(View v) {
        mNsdHelper.discoverServices();
    }
    public void clickConnect(View v) {
        NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
        if (service != null) {
            new MyLog(TAG, "Connecting.");
            mConnection.connectToServer(service.getHost(), service.getPort());
        } else {
            new MyLog(TAG, "No service to connect to!");
        }
    }
    public void initialSend() {
        mConnection.sendMessage(Build.MODEL);
    }

    public void addChatLine(String line) {
        mStatusTextView.setText(line);
    }

    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        application.createChatConnection(mUpdateHandler);
        mConnection = application.getChatConnection();
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
        mNsdHelper.registerService(mConnection.getLocalPort());
        super.onStart();
    }

    @Override
    protected void onPause() {
        new MyLog(TAG, "Pausing.");
        if (mNsdHelper != null) {
            mNsdHelper.stopDiscovery();
        }
        if(mAsyncTask != null && !mAsyncTask.isCancelled())
            mAsyncTask.cancel(true);
        super.onPause();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
        if(mAsyncTask == null || mAsyncTask.isCancelled())
            mAsyncTask = new AsyncTaskCheckStatus();
        mAsyncTask.execute();
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
        mNsdHelper.tearDown();
        //mConnection.tearDown();
        mNsdHelper = null;
        mConnection = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
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

    private class AsyncTaskCheckStatus extends AsyncTask<Void, Integer, String> {

        private final String TAG = "async check status";
        private int sleepTime = 100;
        private int status;


        @Override
        protected void onPreExecute() {
            mStatusTextView.setText("Uninitialized");
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                while(true) {
                    Thread.sleep(sleepTime);
                    if (mNsdHelper != null) {
                        status = mNsdHelper.connection_status;
                        publishProgress(status); // Calls onProgressUpdate()
                        if (status == FinalVariables.NETWORK_RESOLVED_SERVICE) {
                            Thread.sleep(5000);
                            return "finish";
                        }
                    } else
                        return null;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                new MyLog(TAG, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                new MyLog(TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... numbers) {
            mStatusTextView.setText(getResources().getStringArray(R.array.network_statuses)[numbers[0]]);
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            //progressDialog.dismiss();
            NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
            if (service != null) {
                new MyLog(TAG, "Connecting.");
                mConnection.connectToServer(service.getHost(),
                        service.getPort());
            } else {
                new MyLog(TAG, "No service to connect to!");
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initialSend();
                }
            },1000);


            //finish();
        }
    }
}
