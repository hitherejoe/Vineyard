package com.hitherejoe.vineyard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import com.hitherejoe.vineyard.ui.fragment.AutoLoopStepFragment;

/**
 * Activity that showcases different aspects of GuidedStepFragments.
 */
public class GuidedStepActivity extends BaseActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, GuidedStepActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivityComponent().inject(this);
        GuidedStepFragment.addAsRoot(this, new AutoLoopStepFragment(), android.R.id.content);
    }

}