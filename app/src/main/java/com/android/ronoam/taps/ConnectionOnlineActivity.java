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
import android.widget.TextView;

import com.android.ronoam.taps.Keyboard.WordsStorage;
import com.android.ronoam.taps.Network.ChatConnection;
import com.android.ronoam.taps.Network.NsdHelper;
import com.android.ronoam.taps.Utils.MyLog;
import com.android.ronoam.taps.Utils.MyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectionOnlineActivity extends AppCompatActivity {

    NsdHelper mNsdHelper;
    private TextView mStatusTextView;
    private Handler mUpdateHandler;
    public static final String TAG = "Establish Connection";
    ChatConnection mConnection;
    ChatApplication application;

    AsyncTaskCheckStatus mAsyncTask;
    boolean triedExit, firstMessage, isConnectionEstablished, beingStopped, wordsCreated, meResolvedPeer;

    Bundle data;
    int gameMode;
    private String serviceName;
    private List<String> words;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ChatApplication) getApplication();
        firstMessage = true;
        isConnectionEstablished = false;
        beingStopped = false;

        new MyLog(TAG, "Creating Connection activity");
        setContentView(R.layout.activity_connection_online);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        data = getIntent().getExtras();
        gameMode = data.getInt(FinalVariables.GAME_MODE, FinalVariables.TAP_PVP_ONLINE);
        if(gameMode == FinalVariables.TAP_PVP_ONLINE)
            serviceName = FinalVariables.TAP_PVP_SERVICE;
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE)
            serviceName = FinalVariables.TYPE_PVP_SERVICE;

        bindUI();
        initHandler();
    }

    private void initHandler() {
        if(gameMode == FinalVariables.TAP_PVP_ONLINE){
            mUpdateHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    String chatLine = msg.getData().getString("msg");
                    if(msg.arg1 == FinalVariables.FROM_MYSELF) {
                        return true;
                    }
                    if(msg.arg1 == FinalVariables.FROM_OPPONENT){
                        if(chatLine == null) {
                            new MyToast(getApplicationContext(), "Opponent disconnected");
                            new MyLog(TAG, "Opponent disconnected");
                            mAsyncTask.cancel(true);
                            finish();
                            return true;
                        }
                        else{
                            addChatLine(chatLine);
                            if(firstMessage && !isConnectionEstablished) {
                                isConnectionEstablished = true;
                                initialSend();
                                firstMessage = false;
                            }
                            startGameDelayed();
                        }
                    }
                    return true;
                }
            });
        }
        else if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
            mUpdateHandler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    //new MyLog(TAG, "msg #" + count);
                    //count++;
                    String chatLine = msg.getData().getString("msg");
                    if(msg.arg1 == FinalVariables.FROM_MYSELF) {
                        return true;
                    }
                    else if(msg.arg1 == FinalVariables.FROM_OPPONENT){
                        if(chatLine == null) {
                            new MyToast(getApplicationContext(), "Opponent disconnected");
                            new MyLog(TAG, "Opponent disconnected");
                            mAsyncTask.cancel(true);
                            finish();
                            return true;
                        }
                        else if(firstMessage && !isConnectionEstablished) {
                            addChatLine(chatLine);
                            isConnectionEstablished = true;
                            initialSend();
                            firstMessage = false;
                        }
                        else if(!firstMessage && isConnectionEstablished && !wordsCreated) {
                            new MyLog(TAG, "received words");
                            new MyLog(TAG, chatLine);
                            words = new ArrayList<>(Arrays.asList(chatLine.split(",")));
                            wordsCreated = true;
                            startGameDelayed();
                        }
                        else if(firstMessage && isConnectionEstablished) {
                            if (meResolvedPeer) {
                                addChatLine(chatLine);
                                sendWords();
                                startGameDelayed();
                            }
                        }

                    }
                    return true;
                }
            });
        }
    }

    private void bindUI(){
        mStatusTextView = findViewById(R.id.textView_status_connection_online);
    }

    private void startGameDelayed() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ConnectionOnlineActivity.this, CountDownActivity.class);
                if(gameMode == FinalVariables.TAP_PVP_ONLINE) {
                    //intent.putExtra(FinalVariables.SCREEN_SIZE, screenHeight);
                } else if(gameMode == FinalVariables.TYPE_PVP_ONLINE){
                    intent.putStringArrayListExtra(FinalVariables.WORDS_LIST, new ArrayList<>(words));
                }
                intent.putExtra(FinalVariables.GAME_MODE, gameMode);
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }

    public void initialSend() {
        if(mConnection == null)
            finish();
        else
            mConnection.sendMessage(Settings.Secure.getString(getContentResolver(), "bluetooth_name"));
    }

    private void sendWords(){
        if(!wordsCreated) {
            new MyLog(TAG, "create and send words");
            WordsStorage wordsStorage = new WordsStorage(this);
            words = wordsStorage.getAllWords();
            wordsCreated = true;

            String joinedStr = joinList(words);
            new MyLog(TAG, "created words");
            new MyLog(TAG, joinedStr);
            mConnection.sendMessage(joinedStr);
        }
    }

    private String joinList(List<String> words) {
        String joinedStr = words.toString();
        joinedStr = joinedStr.substring(1);
        joinedStr = joinedStr.substring(0, joinedStr.length()-1);
        joinedStr = joinedStr.replaceAll(" ", "");

        return joinedStr;
    }

    public void addChatLine(String line) {
        mStatusTextView.setText(line);
    }


    //region NSD Methods (unused methods, will be deleted)

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
        if(!beingStopped) {
            mConnection = application.createChatConnection(mUpdateHandler);
            mNsdHelper = new NsdHelper(this, serviceName);
            mNsdHelper.initializeNsd();
            //mNsdHelper.registerService(mConnection.getLocalPort());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNsdHelper.registerService(mConnection.getLocalPort());
                }
            },100);
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        new MyLog(TAG, "Resuming.");
        super.onResume();
        if(!beingStopped) {
            if (mNsdHelper != null) {
                mNsdHelper.discoverServices();
            }
            //if(mAsyncTask == null || mAsyncTask.isCancelled())
            mAsyncTask = new AsyncTaskCheckStatus();
            mAsyncTask.execute();
            triedExit = false;
        }
        else if(beingStopped)
            finish();
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
    protected void onStop() {
        new MyLog(TAG, "Being stopped.");
        beingStopped = true;
        if(mNsdHelper != null) {
            mNsdHelper.tearDown();
            mNsdHelper = null;
        }
        if(!isConnectionEstablished && mConnection != null){
            application.setChatConnectionHandler(null);
            mConnection = null;
            application.ChatConnectionTearDown();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        new MyLog(TAG, "Being destroyed.");
        super.onDestroy();
    }

    @Override public void onBackPressed() {
        if(triedExit) {
            new MyLog(TAG, "BackPressed");
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
            if(isCancelled())
                return;
            NsdServiceInfo service = mNsdHelper.getChosenServiceInfo();
            if (service != null) {
                new MyLog(TAG, "Connecting.");
                mConnection.connectToServer(service.getHost(),
                        service.getPort());
                isConnectionEstablished = true;
                meResolvedPeer = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initialSend();
                    }
                },300);
            } else
                new MyLog(TAG, "No service to connect to!");
        }
    }
}
