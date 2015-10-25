package com.hitherejoe.vineyard.util;

import android.content.Context;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;

public class AccountUtils {

    public static boolean isUserAuthenticated(Context context) {
        PreferencesHelper preferencesHelper = VineyardApplication.get(context).getComponent().dataManager().getPreferencesHelper();
        return preferencesHelper.getAccessToken() != null;
    }

}
