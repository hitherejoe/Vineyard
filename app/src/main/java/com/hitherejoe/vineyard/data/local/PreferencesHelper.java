package com.hitherejoe.vineyard.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class PreferencesHelper {

    private static SharedPreferences mPref;

    public static final String PREF_FILE_NAME = "vineyard_pref_file";
    private static final String PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN";
    private static final String PREF_KEY_USER_NAME = "PREF_KEY_USER_NAME";
    private static final String PREF_KEY_USER_ID = "PREF_KEY_USER_ID";
    private static final String PREF_KEY_AUTO_LOOP_VIDEOS = "PREF_KEY_AUTO_LOOP_VIDEOS";


    public PreferencesHelper(Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public void putAccessToken(String accessToken) {
        mPref.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply();
    }

    public void putUsername(String username) {
        mPref.edit().putString(PREF_KEY_USER_NAME, username).apply();
    }

    public void putUserId(String userId) {
        mPref.edit().putString(PREF_KEY_USER_ID, userId).apply();
    }

    public void putAutoLoop(boolean shouldAutoplay) {
        mPref.edit().putBoolean(PREF_KEY_AUTO_LOOP_VIDEOS, shouldAutoplay).apply();
    }

    @Nullable
    public String getAccessToken() {
        return mPref.getString(PREF_KEY_ACCESS_TOKEN, null);
    }

    @Nullable
    public String getUsername() {
        return mPref.getString(PREF_KEY_USER_NAME, null);
    }

    @Nullable
    public String getUserId() {
        return mPref.getString(PREF_KEY_USER_ID, null);
    }

    public boolean getShouldAutoLoop() {
        return mPref.getBoolean(PREF_KEY_AUTO_LOOP_VIDEOS, false);
    }
}
