package com.android.ronoam.taps;

public class FinalVariables {
    public static final int PORT = 38787;
    public static final int PRE_TIMER = 3100;
    public static final String GAME_MODE = "game mode";
    public static final int REQUEST_CODE = 1;
    public static final String SCREEN_SIZE = "screen size";

    public static final int LANGUAGE = 10;
    public static final int TAPS_DIFFERENCE = 11;

    //Home Activity Buttons Animations
    public static final long HOME_SHOW_UI = 500;
    public static final long HOME_HIDE_UI = 600;

    //Game Modes
    public static final int TAP_PVE = 1;
    public static final int TAP_PVP = 2;
    public static final int TAP_PVP_ONLINE = 3;
    public static final int TYPE_PVE = 4;
    public static final int TYPE_PVP_ONLINE = 5;

    //Connection Types
    public static final int WIFI_MODE = 0;
    public static final int BLUETOOTH_MODE = 1;

    //Timers
    public static final long TAP_GAME_TIME = 10000;
    public static final long TYPE_GAME_TIME = 45000;
    public static final long TYPE_ONLINE_EXTRA_TIMER = 6000;

    //Keyboard Animations
    public static final long KEYBOARD_GAME_INTERVAL = 22;
    public static final long KEYBOARD_GAME_SHOW_UI = 1200;
    public static final long KEYBOARD_GAME_HIDE_UI = 600;

    public static int SCORE_REQUEST = 1;

    //Result Information From The Games
    public static final String SCORE = "score";
    public static final String WINNER = "winner";
    public static final String WORDS_PER_MIN = "words";


    //NSD Service types
    public static final String TAP_PVP_SERVICE = "TapsOnline";
    public static final String TYPE_PVP_SERVICE = "TypeOnline";

    //NSD Modes of Discovering Services Process
    public static final int NETWORK_UNINITIALIZED = 0;
    public static final int NETWORK_REGISTERED_SUCCEEDED = 1;
    public static final int NETWORK_REGISTERED_FAILED = 2;
    public static final int NETWORK_DISCOVERY_STARTED = 3;
    public static final int NETWORK_DISCOVERY_START_FAILED = 4;
    public static final int NETWORK_DISCOVERY_SERVICE_FOUND = 5;
    public static final int NETWORK_DISCOVERY_SERVICE_LOST = 6;
    public static final int NETWORK_RESOLVED_SERVICE = 7;
    public static final int NETWORK_RESOLVED_FAILED = 8;
    public static final int NETWORK_CONNECTION_LOST = 9;

    //Wifi Setup Connection Handler
    public static final int UPDATE_NETWORK_STATUS = 3;

    //Online Game Exit Modes
    public static final int NO_ERRORS = 0;
    public static final int OPPONENT_EXIT = 1;
    public static final int I_EXIT = 2;

    //WifiConnection determination in updateMessages
    public static final int FROM_MYSELF = 1;
    public static final int FROM_OPPONENT = 2;

    //Type determination in updateMessages
    public static final String NEXT_WORD = "next word";
    public static final String CURRENT_WORD = "current word";
    public static final String SUCCESS_WORDS = "success words";
    public static final String CORRECT_SO_FAR = "correct";
    public static final String IS_SUCCESSFUL_TYPE = "is successful";
    public static final int UPDATE_NEXT_WORD = 1;
    public static final int MOVE_TO_NEXT_WORD = 2;

    //Type pvp logic
    public static final int DEFAULT_KEYBOARD = 0;
    public static final int ERASE_KEYBOARD = -1;
    public static final int MIX_KEYBOARD = -2;
    public static final int WORDS_IN_A_ROW = 3;
    public static final int CHAR_STROKES_IN_A_ROW = 18;


    public static final String HIGH_SCORE_TAP_KEY = "high_score_taps";
    public static final String HIGH_SCORE_TYPE_KEY = "high_score_types";
    public static final String LANGUAGE_NAME = "language";

    // Message types sent from the BluetoothConnection Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //Bluetooth setup fragment Handler
    public static final int OPPONENT_PRESSED = 1;
    public static final int OPPONENT_RELEASED = 2;
    public static final int OPPONENT_CANCELED = 3;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "layout_device";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";
}
