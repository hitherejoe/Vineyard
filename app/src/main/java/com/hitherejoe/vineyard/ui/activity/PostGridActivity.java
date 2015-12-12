package com.hitherejoe.vineyard.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.ErrorFragment;
import android.view.View;
import android.widget.FrameLayout;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PostGridActivity extends BaseActivity {

    @Bind(R.id.frame_container)
    FrameLayout mFragmentContainer;

    public static final String EXTRA_ITEM_TYPE = "item_type";
    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String TYPE_TAG = "tag";
    public static final String TYPE_USER = "user";

    public static Intent getStartIntent(Context context, String itemType, String itemId) {
        Intent intent = new Intent(context, PostGridActivity.class);
        intent.putExtra(EXTRA_ITEM_TYPE, itemType);
        intent.putExtra(EXTRA_ITEM_ID, itemId);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_grid);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        String itemType = bundle.getString(EXTRA_ITEM_TYPE);
        String itemId = bundle.getString(EXTRA_ITEM_ID);

        Fragment fragment;
        if (itemType == null ||
                (!(itemType.equals(TYPE_TAG)) && !(itemType.equals(TYPE_USER)))) {
            fragment = buildErrorFragment();
        } else {
            fragment = PostGridFragment.newInstance(itemType, itemId);
        }
        getFragmentManager().beginTransaction().replace(mFragmentContainer.getId(), fragment)
                .addToBackStack(null).commit();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    private ErrorFragment buildErrorFragment() {
        ErrorFragment errorFragment = new ErrorFragment();
        errorFragment.setTitle(getString(R.string.text_error_oops_title));
        errorFragment.setMessage(getString(R.string.text_error_oops_message));
        errorFragment.setButtonText(getString(R.string.text_error_dismiss));
        errorFragment.setButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return errorFragment;
    }

}