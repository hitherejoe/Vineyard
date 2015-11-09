package com.hitherejoe.vineyard.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.SearchFragment;

import com.hitherejoe.vineyard.R;

/*
 * SearchActivity for SearchFragment
 */
public class SearchActivity extends BaseActivity {

    private static final String TAG = "SearchActivity";
    private SearchFragment mFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
    }

    @Override
    public boolean onSearchRequested() {
      //  if (mFragment.hasResults()) {
        //    startActivity(new Intent(this, SearchActivity.class));
     //   } else {
       //     mFragment.startRecognition();
      //  }
        return true;
    }
}