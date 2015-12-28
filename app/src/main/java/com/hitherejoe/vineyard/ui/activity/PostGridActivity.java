package com.hitherejoe.vineyard.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.ErrorFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PostGridActivity extends BaseActivity {

    @Bind(R.id.frame_container_post_grid)
    FrameLayout mFragmentContainer;

    public static final String EXTRA_ITEM = "extra_item";
    private Fragment mPostGridFragment;

    public static Intent getStartIntent(Context context, Object selectedItem) {
        Intent intent = new Intent(context, PostGridActivity.class);
        if (selectedItem instanceof User) {
            intent.putExtra(EXTRA_ITEM, (User) selectedItem);
        } else if (selectedItem instanceof Tag) {
            intent.putExtra(EXTRA_ITEM, (Tag) selectedItem);
        }
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_grid);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        Object object = null;
        if (bundle != null) {
            object = bundle.getParcelable(EXTRA_ITEM);
        }

        if (object == null ||
                (!(object instanceof User) && !(object instanceof Tag))) {
            mPostGridFragment = buildErrorFragment();
        } else {
            mPostGridFragment = PostGridFragment.newInstance(object);
        }
        getFragmentManager().beginTransaction()
                .add(mFragmentContainer.getId(), mPostGridFragment).commit();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    public boolean isFragmentActive() {
        return mPostGridFragment instanceof PostGridFragment &&
                mPostGridFragment.isAdded() &&
                !mPostGridFragment.isDetached() &&
                !mPostGridFragment.isRemoving() &&
                !((PostGridFragment) mPostGridFragment).isStopping();
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