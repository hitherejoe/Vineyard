package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.util.Log;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.ui.CardPresenter;
import com.hitherejoe.vineyard.ui.LoadingCardView;
import com.hitherejoe.vineyard.ui.LoadingPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;


public class GridPaginationAdapter extends ArrayObjectAdapter {

    private CardPresenter mCardPresenter;
    private LoadingPresenter mLoadingPresenter;
    private int mLoadingIndicatorPosition;
    private int mCurrentPage;
    private String mRowTag;
    private List<Post> mRowPosts;
    private Context mContext;
    private String mAnchor;
    private boolean mPaginationEnabled;

    public GridPaginationAdapter(Context context, String tag) {
        mContext = context;
        mCardPresenter = new CardPresenter();
        mLoadingPresenter = new LoadingPresenter();
        mPaginationEnabled = true;
        mCurrentPage = 0;
        mRowTag = tag;
        mRowPosts = new ArrayList<>();
        setPresenterSelector();
    }

    public void setTag(String tag) {
        mRowTag = tag;
    }

    public void setFirstPage(int page) {
        mCurrentPage = page;
    }

    public void setPresenterSelector() {
        setPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                if (item instanceof LoadingCardView) {
                    return mLoadingPresenter;
                }
                return mCardPresenter;
            }
        });
    }

    public boolean isPaginationEnabled() {
        return this.mPaginationEnabled;
    }

    public String getAnchor() {
        return this.mAnchor;
    }

    public int getNextPage() {
        mCurrentPage++;
        return this.mCurrentPage;
    }

    public String getRowTag() {
        return this.mRowTag;
    }

    public List<Post> getPosts() {
        return mRowPosts;
    }

    public void addPosts(List<Post> posts) {
        if (posts.size() > 0) {
            Collections.sort(posts);
            mRowPosts.addAll(posts);
            addAll(size(), posts);
            mPaginationEnabled = true;
        } else {
            mPaginationEnabled = false;
        }
    }

    public boolean isShowingRowLoadingIndicator() {
        return mLoadingIndicatorPosition != -1;
    }

    public void showRowLoadingIndicator() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mLoadingIndicatorPosition = size();
                add(mLoadingIndicatorPosition, new LoadingCardView(mContext));
                notifyItemRangeInserted(mLoadingIndicatorPosition, 1);
            }
        });

    }

    public void removeLoadingIndicator() {
        removeItems(mLoadingIndicatorPosition, 1);
        notifyItemRangeRemoved(mLoadingIndicatorPosition, 1);
        mLoadingIndicatorPosition = -1;
    }

    public void setAnchor(String anchor) {
        mAnchor = anchor;
    }

}
