package com.hitherejoe.vineyard.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.hitherejoe.vineyard.R;

/*
 * This class demonstrates how to extend ErrorFragment
 */
public class ErrorFragment extends android.support.v17.leanback.app.ErrorFragment {
    private static final String TAG = "ErrorFragment";
    private static final boolean TRANSLUCENT = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.app_name));
    }

    public void setErrorContent() {
        setImageDrawable(getResources().getDrawable(R.drawable.lb_ic_sad_cloud));
        setMessage("ERROR");
        setDefaultBackground(TRANSLUCENT);

        setButtonText("DISMISS");
        setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getFragmentManager().beginTransaction().remove(ErrorFragment.this).commit();
            }
        });
    }
}