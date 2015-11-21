package com.hitherejoe.vineyard.ui.activity;

import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import com.hitherejoe.vineyard.ui.fragment.AutoLoopStepFragment;

/**
 * Activity that showcases different aspects of GuidedStepFragments.
 */
public class GuidedStepActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        if (null == savedInstanceState) {
            GuidedStepFragment.add(getFragmentManager(), new AutoLoopStepFragment());
        }
    }

}