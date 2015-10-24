package com.hitherejoe.androidboilerplate.ui.activity;

import android.os.Bundle;

import com.hitherejoe.androidboilerplate.R;

public class MainActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onSearchRequested() {
        // Start search activity
        return true;
    }

}
