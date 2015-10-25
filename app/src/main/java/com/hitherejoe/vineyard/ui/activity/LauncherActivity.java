package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.hitherejoe.vineyard.util.AccountUtils;

public class LauncherActivity extends Activity {

    public LauncherActivity() { }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class intentClass = AccountUtils.isUserAuthenticated(this) ?
                MainActivity.class : ConnectActivity.class;
        startActivity(new Intent(this, intentClass));
        finish();
    }
}