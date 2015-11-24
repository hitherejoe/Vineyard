package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.injection.component.ActivityComponent;
import com.hitherejoe.vineyard.injection.component.DaggerActivityComponent;

public class BaseActivity extends Activity {

    private ActivityComponent mActivityComponent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public ActivityComponent activityComponent() {
        if (mActivityComponent == null) {
            mActivityComponent = DaggerActivityComponent.builder()
                    .applicationComponent(VineyardApplication.get(this).getComponent())
                    .build();
        }
        return mActivityComponent;
    }

}
