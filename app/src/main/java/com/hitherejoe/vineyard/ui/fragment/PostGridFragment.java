package com.hitherejoe.vineyard.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.VerticalGridFragment;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.util.DisplayMetrics;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.GridPaginationAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PostGridFragment extends VerticalGridFragment {

    public static final String TYPE_USER = "tag";
    public static final String TYPE_TAG = "user";

    @Inject
    CompositeSubscription mCompositeSubscription;
    @Inject
    DataManager mDataManager;

    private static final int NUM_COLUMNS = 5;
    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private GridPaginationAdapter mRowsAdapter;
    private Handler mHandler;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private BackgroundManager mBackgroundManager;
    private Runnable mBackgroundRunnable;
    private String mSelectedType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        //setTitle(getString(R.string.vertical_grid_title));

        setupFragment();
        setBadgeDrawable(null);
        prepareBackgroundManager();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBackgroundRunnable != null) {
            mHandler.removeCallbacks(mBackgroundRunnable);
            mBackgroundRunnable = null;
        }
        mBackgroundManager = null;
        mCompositeSubscription.unsubscribe();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBackgroundManager.release();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = new ColorDrawable(0xffff6666);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void setTag(Object selectedItem) {
        String tag = null;
        if (selectedItem instanceof User) {
            mSelectedType = TYPE_USER;
            tag = ((User) selectedItem).userId;
        } else if (selectedItem instanceof Tag) {
            mSelectedType = TYPE_TAG;
            tag = ((Tag) selectedItem).tag;
        }
        mRowsAdapter = new GridPaginationAdapter(getActivity(), tag);

        setAdapter(mRowsAdapter);
        addPageLoadSubscription();
    }

    private void setupFragment() {
        VerticalGridPresenter gridPresenter = new VerticalGridPresenter();
        gridPresenter.setNumberOfColumns(NUM_COLUMNS);
        setGridPresenter(gridPresenter);

        mHandler = new Handler();

        setOnSearchClickedListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);
    }

    private void startBackgroundTimer(final URI backgroundURI) {
        if (mBackgroundRunnable != null) mHandler.removeCallbacks(mBackgroundRunnable);
        mBackgroundRunnable = new Runnable() {
            @Override
            public void run() {
                if (backgroundURI != null) updateBackground(backgroundURI.toString());
            }
        };
        mHandler.postDelayed(mBackgroundRunnable, BACKGROUND_UPDATE_DELAY);
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Glide.with(getActivity())
                .load(uri)
                .asBitmap()
                .centerCrop()
                .error(mDefaultBackground)
                .into(new SimpleTarget<Bitmap>(width, height) {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                GlideAnimation<? super Bitmap>
                                                        glideAnimation) {
                        mBackgroundManager.setBitmap(resource);
                    }
                });
        if (mBackgroundRunnable != null) mHandler.removeCallbacks(mBackgroundRunnable);
    }

    private void addPageLoadSubscription() {
        mRowsAdapter.showRowLoadingIndicator();

        String tag = mRowsAdapter.getRowTag();
        String anchor = mRowsAdapter.getAnchor();
        int nextPage = mRowsAdapter.getNextPage();

        Observable<VineyardService.PostResponse> observable = null;

        if (mSelectedType.equals(TYPE_TAG)) {
            observable = mDataManager.getPostsByTag(tag, nextPage, anchor);
        } else if (mSelectedType.equals(TYPE_USER)) {
            observable = mDataManager.getPostsByUser(tag, nextPage, anchor);
        }
        if (observable != null) {
            mCompositeSubscription.add(observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(mDataManager.getSubscribeScheduler())
                    .subscribe(new Subscriber<VineyardService.PostResponse>() {
                        @Override
                        public void onCompleted() {
                            mRowsAdapter.removeLoadingIndicator();
                        }

                        @Override
                        public void onError(Throwable e) {
                            mRowsAdapter.removeLoadingIndicator();
                            Timber.e(e, "There was an error loading the videos");
                        }

                        @Override
                        public void onNext(VineyardService.PostResponse postResponse) {
                            mRowsAdapter.setAnchor(postResponse.data.anchorStr);
                            mRowsAdapter.addPosts(postResponse.data.records);
                        }
                    }));
        }
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                Post post = (Post) item;
                ArrayList<Post> postList = new ArrayList<>(mRowsAdapter.getPosts());
                startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                String backgroundUrl = ((Post) item).thumbnailUrl;
                if (backgroundUrl != null) startBackgroundTimer(URI.create(backgroundUrl));
                List<Post> posts = mRowsAdapter.getPosts();
                int itemIndex = mRowsAdapter.indexOf(item);
                int minimumIndex = posts.size() - NUM_COLUMNS;
                if (itemIndex >= minimumIndex) {
                    if (!(mRowsAdapter.isShowingRowLoadingIndicator())
                            && mRowsAdapter.isPaginationEnabled()) {
                        addPageLoadSubscription();
                    }
                }
            }
        }
    };

}