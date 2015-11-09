package com.hitherejoe.vineyard.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.IconHeaderItemPresenter;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class MainFragment extends BrowseFragment {

    public static final String TAG_POPULAR = "Popular";
    public static final String TAG_TRENDING = "Editors Picks";
    private static final int BACKGROUND_UPDATE_DELAY = 300;

    @Inject
    CompositeSubscription mCompositeSubscription;
    @Inject
    DataManager mDataManager;

    private ArrayObjectAdapter mRowsAdapter;
    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Handler mHandler;
    private Runnable mBackgroundRunnable;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mHandler = new Handler();
        loadVideos();
        setAdapter(mRowsAdapter);
        prepareBackgroundManager();
        setupUIElements();
        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);

        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });
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

    private void loadVideos() {
        loadVideosFromFeed(TAG_TRENDING, 0);
        loadVideosFromFeed(TAG_POPULAR, 1);
        String[] categories = getResources().getStringArray(R.array.categories);
        for (int i = 2; i < categories.length; i++) loadVideosFromFeed(categories[i], i);
    }

    private void loadVideosFromFeed(String tag, int headerPosition) {
        PaginationAdapter listRowAdapter = new PaginationAdapter(getActivity(), tag);
        addPageLoadSubscription(listRowAdapter);
        HeaderItem header = new HeaderItem(headerPosition, tag);
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = new ColorDrawable(0xffff6666);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.banner));
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.primary));
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.accent));

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });
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

    private void addPageLoadSubscription(final PaginationAdapter arrayObjectAdapter) {
        arrayObjectAdapter.showRowLoadingIndicator();

        String tag = arrayObjectAdapter.getRowTag();
        String anchor = arrayObjectAdapter.getAnchor();
        int nextPage = arrayObjectAdapter.getNextPage();

        Observable<VineyardService.PostResponse> observable;

        switch (tag) {
            case TAG_POPULAR:
                observable = mDataManager.getPopularPosts(nextPage, anchor);
                break;
            case TAG_TRENDING:
                observable = mDataManager.getEditorsPicksPosts(nextPage, anchor);
                break;
            default:
                observable = mDataManager.getPostsByTag(tag, nextPage, anchor);
                break;
        }

        mCompositeSubscription.add(observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getSubscribeScheduler())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                        arrayObjectAdapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onError(Throwable e) {
                        arrayObjectAdapter.removeLoadingIndicator();
                        Timber.e(e, "There was an error loading the videos");
                    }

                    @Override
                    public void onNext(VineyardService.PostResponse postResponse) {
                        arrayObjectAdapter.setAnchor(postResponse.data.anchorStr);
                        arrayObjectAdapter.addPosts(postResponse.data.records);
                    }
                }));
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                Post post = (Post) item;
                int index = mRowsAdapter.indexOf(row);
                PaginationAdapter arrayObjectAdapter =
                        ((PaginationAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                ArrayList<Post> postList = new ArrayList<>(arrayObjectAdapter.getPosts());
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
                int index = mRowsAdapter.indexOf(row);
                PaginationAdapter arrayObjectAdapter =
                        ((PaginationAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                List<Post> posts = arrayObjectAdapter.getPosts();
                if (item.equals(posts.get(posts.size() - 1))) {
                    if (!(arrayObjectAdapter.isShowingRowLoadingIndicator())
                            && arrayObjectAdapter.isPaginationEnabled()) {
                        addPageLoadSubscription(arrayObjectAdapter);
                    }
                }
            }
        }
    };

}