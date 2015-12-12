package com.hitherejoe.vineyard.ui.activity;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.ErrorFragment;
import android.view.View;
import android.widget.FrameLayout;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PostGridActivity extends BaseActivity {

    @Bind(R.id.frame_container)
    FrameLayout mFragmentContainer;

    public static final String SELECTED_ITEM = "selected_item";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_grid);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        Object selectedItem = bundle.getParcelable(SELECTED_ITEM);

        Fragment fragment;
        if (selectedItem == null ||
                (!(selectedItem instanceof Tag) && !(selectedItem instanceof User))) {
            fragment = buildErrorFragment();
        } else {
            if (selectedItem instanceof Tag) {
                Tag tag = (Tag) selectedItem;
                fragment = PostGridFragment.newInstance(PostGridFragment.TYPE_TAG, tag.tag);
            } else {
                User user = (User) selectedItem;
                fragment = PostGridFragment.newInstance(PostGridFragment.TYPE_USER, user.userId);
            }
        }
        getFragmentManager().beginTransaction().replace(mFragmentContainer.getId(), fragment)
                .addToBackStack(null).commit();
    }

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }

    public static Intent newStartIntent(Context context, Tag selectedTag) {
        Intent intent = new Intent(context, PostGridActivity.class);
        intent.putExtra(SELECTED_ITEM, selectedTag);
        return intent;
    }

    public static Intent newStartIntent(Context context, User selectedUser) {
        Intent intent = new Intent(context, PostGridActivity.class);
        intent.putExtra(SELECTED_ITEM, selectedUser);
        return intent;
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