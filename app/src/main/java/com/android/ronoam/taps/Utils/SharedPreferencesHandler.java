package com.android.ronoam.taps.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.ronoam.taps.R;

public class SharedPreferencesHandler {
    public static void wipeSharedPreferences(Context ctx) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(ctx.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.clear();
        editor.apply();
    }

    public static void writeInt(Context context, String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static void writeString(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getInt(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, 0);
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return sharedPref.getString(key, "");
    }
}