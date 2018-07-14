package com.android.ronoam.taps;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

public class ConnectionOnlineActivity extends AppCompatActivity {

    NsdHelper mNsdHelper;
    private TextView mStatusTextView;
    private View container;
    private Handler mUpdateHandler;
    public static final String TAG = "Establish Connection";
    ChatConnection mConnection;
    ChatApplication application;

    AsyncTaskCheckStatus mAsyncTask;
    boolean triedExit, firstMessage, isConnectionEstablished;

    Bundle data;
    int gameMode;
    private int screenHeight;
    private String serviceName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ChatApplication) getApplication();
        firstMessage = true;
        isConnectionEstablished = false;

        new MyLog(TAG, "Creating chat activity");
        setContentView(R.layout.activity_connection_online);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        bindUI();

        mUpdateHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String chatLine = msg.getData().getString("msg");
                if(chatLine.startsWith("me")) {
                    return true;
                }
                else if(chatLine.startsWith("them")) {
                    addChatLine(chatLine);
                    if(firstMessage && !isConnectionEstablished) {
                        isConnectionEstablished = true;
                        initialSend();
                        firstMessage = false;
                    }
                }
                startGameDelayed();
                return true;
            }
        });

        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP_ONLINE);
        if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            serviceName = FinalVariables.TYPE_PVP_SERVICE;

        getScreenSize();
    }

    private void bindUI(){
        mStatusTextView = findViewById(R.id.textView_status_connection_online);
        container = findViewById(R.id.connection_online_container);
    }

    private void startGameDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ConnectionOnlineActivity.this, CountDownActivity.class);
                if(gameMode == FinalVariables.TAP_PVP_ONLINE) {
                    intent.putExtra(FinalVariables.SCREEN_SIZE, screenHeight);
                } else if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
                    //todo maybe put the words array in the intent
                }
                intent.putExtra(FinalVariables.GAME_MODE, gameMode);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            }
        }, 2500);
    }

    public void initialSend() {
        mConnection.sendMessage(Settings.Secure.getString(getContentResolver(), "bluetooth_name"));
    }

    public void addChatLine(String line) {
        mStatusTextView.setText(line);
    }

    private void getScreenSize(){
        ViewTreeObserver viewTreeObserver = container.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    screenHeight = container.getHeight();
                    //new MyToast(ConnectionOnlineActivity.this, "screen height = " + screenHeight);
                }
            });
        }
    }

    //region NSD Methods

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

    //endregion

    //region Activity Overrides
    @Override
    protected void onStart() {
        new MyLog(TAG, "Starting.");
        mConnection = application.createChatConnection(mUpdateHandler);
        mNsdHelper = new NsdHelper(this, serviceName);
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
            mAsyncTask.cancel(false);
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
        triedExit = false;
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
        mNsdHelper = null;
        //application.setChatConnectionHandler(null);
        mConnection = null;
        //mConnection = null;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            super.onBackPressed();
        }
        else{
            new MyToast(this, R.string.before_exit);
            triedExit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    triedExit = false;
                }
            }, 1500);
        }
    }

    //endregion

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
                    if(isCancelled())
                        return null;
                    Thread.sleep(sleepTime);
                    if(isCancelled())
                        return null;
                    if (mNsdHelper != null) {
                        status = mNsdHelper.connection_status;
                        publishProgress(status); // Calls onProgressUpdate()
                        if (status == FinalVariables.NETWORK_RESOLVED_SERVICE) {
                            Thread.sleep(1500);
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
            if(!isConnectionEstablished)
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
                isConnectionEstablished = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initialSend();
                    }
                },1000);
            } else {
                new MyLog(TAG, "No service to connect to!");
                return;
            }
        }
    }
}
