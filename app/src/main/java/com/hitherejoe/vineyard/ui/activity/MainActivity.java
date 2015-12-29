package com.hitherejoe.vineyard.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.ErrorFragment;
import android.view.View;
import android.widget.FrameLayout;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.ui.fragment.MainFragment;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;
import com.hitherejoe.vineyard.util.NetworkUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @Bind(R.id.frame_container)
    FrameLayout mFragmentContainer;

    private Fragment mBrowseFragment;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (NetworkUtil.isWifiConnected(this)) {
            mBrowseFragment = MainFragment.newInstance();
        } else {
            mBrowseFragment = buildErrorFragment();
        }
        getFragmentManager().beginTransaction()
                .replace(mFragmentContainer.getId(), mBrowseFragment).commit();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    public boolean isFragmentActive() {
        return mBrowseFragment instanceof MainFragment &&
                mBrowseFragment.isAdded() &&
                !mBrowseFragment.isDetached() &&
                !mBrowseFragment.isRemoving() &&
                !((MainFragment) mBrowseFragment).isStopping();
    }

    private ErrorFragment buildErrorFragment() {
        ErrorFragment errorFragment = new ErrorFragment();
        errorFragment.setTitle(getString(R.string.text_error_oops_title));
        errorFragment.setMessage(getString(R.string.error_message_wifi_needed_app));
        errorFragment.setButtonText(getString(R.string.text_close));
        errorFragment.setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return errorFragment;
    }

}
