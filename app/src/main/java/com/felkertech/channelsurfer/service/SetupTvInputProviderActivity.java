package com.felkertech.channelsurfer.service;

import android.app.Activity;
import android.media.tv.TvInputInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.felkertech.channelsurfer.TvContractUtils;
import com.felkertech.channelsurfer.sync.SyncAdapter;
import com.felkertech.channelsurfer.sync.SyncUtils;
import com.hitherejoe.vineyard.R;

/**
 * Created by guest1 on 1/6/2016.
 */
public abstract class SetupTvInputProviderActivity extends Activity {
    public static String COLUMN_CHANNEL_URL = "CHANNEL_URL";
    private String TAG = "SetupTvInputProviderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Created me");

        String info = "";
        if(getIntent() != null) {
            info = getIntent().getStringExtra(TvInputInfo.EXTRA_INPUT_ID);
            Log.d(TAG, info);
        }

        SyncUtils.setUpPeriodicSync(this, info);
        setupTvInputProvider(info);
    }

    public void setupTvInputProvider(String extraInputId) {
        setContentView(R.layout.channel_surfer_setup);
        SyncUtils.requestSync(extraInputId);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "Setup complete. Make sure you enable these channels in the channel list.", Toast.LENGTH_SHORT).show();
    }
}
