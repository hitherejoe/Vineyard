package com.hitherejoe.vineyard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;
import com.hitherejoe.vineyard.ui.fragment.SearchFragment;

public class SearchActivity extends BaseActivity {

    private SearchFragment mSearchFragment;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchFragment = (SearchFragment) getFragmentManager().findFragmentById(R.id.search_fragment);
    }

    @Override
    public boolean onSearchRequested() {
        if (mSearchFragment.hasResults()) {
            startActivity(new Intent(this, SearchActivity.class));
        } else {
            mSearchFragment.startRecognition();
        }
        return true;
    }

    public boolean isFragmentActive() {
        return mSearchFragment.isAdded() &&
                !mSearchFragment.isDetached() &&
                !mSearchFragment.isRemoving() &&
                !mSearchFragment.isStopping();
    }
}