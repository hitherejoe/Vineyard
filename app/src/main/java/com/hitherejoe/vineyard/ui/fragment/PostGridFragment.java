package com.hitherejoe.vineyard.ui.fragment;

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
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Option;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;
import com.hitherejoe.vineyard.ui.adapter.PostAdapter;
import com.hitherejoe.vineyard.util.NetworkUtil;
import com.hitherejoe.vineyard.util.ToastFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class PostGridFragment extends VerticalGridFragment {

    public static final String ARG_ITEM = "arg_item";
    public static final String TYPE_USER = "user";
    public static final String TYPE_TAG = "tag";

    @Inject CompositeSubscription mCompositeSubscription;
    @Inject DataManager mDataManager;

    private static final int NUM_COLUMNS = 5;
    private static final int BACKGROUND_UPDATE_DELAY = 300;

    private BackgroundManager mBackgroundManager;
    private DisplayMetrics mMetrics;
    private Drawable mDefaultBackground;
    private Handler mHandler;
    private PostAdapter mPostAdapter;
    private Runnable mBackgroundRunnable;
    private String mSelectedType;
    private boolean mIsStopping;

    public static PostGridFragment newInstance(Object selectedItem) {
        PostGridFragment postGridFragment = new PostGridFragment();
        Bundle args = new Bundle();
        if (selectedItem instanceof User) {
            args.putParcelable(ARG_ITEM, (User) selectedItem);
        } else if (selectedItem instanceof Tag) {
            args.putParcelable(ARG_ITEM, (Tag) selectedItem);
        }
        postGridFragment.setArguments(args);
        return postGridFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        setupFragment();
        prepareBackgroundManager();
        Bundle args = getArguments();
        setTag(args.getParcelable(ARG_ITEM));
        setSearchAffordanceColor(ContextCompat.getColor(getActivity(), R.color.accent));
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
    public void onStart() {
        super.onStart();
        mIsStopping = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        mBackgroundManager.release();
        mIsStopping = true;
    }

    public boolean isStopping() {
        return mIsStopping;
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground =
                new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.bg_light_grey));
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    public void setTag(Object selectedItem) {
        String tag = null;
        if (selectedItem instanceof User) {
            mSelectedType = TYPE_USER;
            tag = ((User) selectedItem).userId;
            setTitle(((User) selectedItem).username);
        } else if (selectedItem instanceof Tag) {
            mSelectedType = TYPE_TAG;
            tag = ((Tag) selectedItem).tag;
            setTitle(String.format("#%s", tag));
        }
        mPostAdapter = new PostAdapter(getActivity(), tag);
        setAdapter(mPostAdapter);
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
                startActivity(SearchActivity.getStartIntent(getActivity()));
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
        if (mPostAdapter.shouldShowLoadingIndicator()) mPostAdapter.showLoadingIndicator();

        Map<String, String> options = mPostAdapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        Observable<VineyardService.PostResponse> observable = null;

        if (mSelectedType.equals(TYPE_TAG)) {
            observable = mDataManager.getPostsByTag(tag, nextPage, anchor);
        } else if (mSelectedType.equals(TYPE_USER)) {
            observable = mDataManager.getPostsByUser(tag, nextPage, anchor);
        }
        if (observable != null) {
            mCompositeSubscription.add(observable
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<VineyardService.PostResponse>() {
                        @Override
                        public void onCompleted() { }

                        @Override
                        public void onError(Throwable e) {
                            mPostAdapter.removeLoadingIndicator();
                            if (mPostAdapter.size() == 0) {
                                mPostAdapter.showTryAgainCard();
                            } else {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.error_message_loading_more_posts),
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                            Timber.e("There was an error loading the posts", e);
                        }

                        @Override
                        public void onNext(VineyardService.PostResponse postResponse) {
                            mPostAdapter.removeLoadingIndicator();
                            if (mPostAdapter.size() == 0 && postResponse.data.records.isEmpty()) {
                                mPostAdapter.showReloadCard();
                            } else {
                                mPostAdapter.setAnchor(postResponse.data.anchorStr);
                                mPostAdapter.setNextPage(postResponse.data.nextPage);
                                mPostAdapter.addAllItems(postResponse.data.records);
                            }
                        }
                    }));
        } else {
            //TODO: Handle error
        }
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                if (NetworkUtil.isWifiConnected(getActivity())) {
                    Post post = (Post) item;
                    ArrayList<Post> postList = (ArrayList<Post>) mPostAdapter.getAllItems();
                    startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
                } else {
                    ToastFactory.createWifiErrorToast(getActivity()).show();
                }
            } else if (item instanceof Option) {
                Option option = (Option) item;
                if (option.title.equals(getString(R.string.title_oops)) ||
                        option.title.equals(getString(R.string.title_no_videos))) {
                    mPostAdapter.removeReloadCard();
                    addPageLoadSubscription();
                }
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
                ArrayList<Post> posts = (ArrayList<Post>) mPostAdapter.getAllItems();

                int itemIndex = mPostAdapter.indexOf(item);
                int minimumIndex = posts.size() - NUM_COLUMNS;
                if (itemIndex >= minimumIndex && mPostAdapter.shouldLoadNextPage()) {
                    addPageLoadSubscription();
                }
            }
        }
    };

}