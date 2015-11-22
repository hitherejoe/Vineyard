package com.hitherejoe.vineyard.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.ui.fragment.PostGridFragment;

import butterknife.ButterKnife;

/*
 * VerticalGridActivity that loads VerticalGridFragment
 */
public class PostGridActivity extends BaseActivity {

    public static final String SELECTED_ITEM = "selected_item";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_grid);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        Object selectedItem = bundle.getParcelable(SELECTED_ITEM);

        if (selectedItem == null) {
            throw new IllegalArgumentException();
        } else if (!(selectedItem instanceof Tag)
                && !(selectedItem instanceof User)) {
            throw new IllegalArgumentException();
        }
                //getWindow().setBackgroundDrawableResource(R.drawable.grid_bg);
        PostGridFragment mFragment = (PostGridFragment) getFragmentManager().findFragmentById(R.id.fragment_post_grid);
        mFragment.setTag(selectedItem);
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

    @Override
    public boolean onSearchRequested() {
        startActivity(new Intent(this, SearchActivity.class));
        return true;
    }
}