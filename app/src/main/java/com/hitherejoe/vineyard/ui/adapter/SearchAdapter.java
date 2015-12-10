package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.ui.LoadingCardView;
import com.hitherejoe.vineyard.ui.LoadingPresenter;
import com.hitherejoe.vineyard.ui.TagPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SearchAdapter extends ArrayObjectAdapter {

    private TagPresenter mCardPresenter;
    private LoadingPresenter mLoadingPresenter;
    private int mLoadingIndicatorPosition;
    private String mRowTag;
    private List<Object> mRowPosts;
    private Context mContext;
    private boolean mPaginationEnabled;
    private int mCurrentPage;

    public SearchAdapter(Context context) {
        mContext = context;
        mCardPresenter = new TagPresenter();
        mLoadingPresenter = new LoadingPresenter();
        mPaginationEnabled = true;
        mCurrentPage = -1;
        mRowPosts = new ArrayList<>();
        setPresenterSelector();
    }

    public void setTag(String tag) {
        mRowTag = tag;
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

    public int getNextPage() {
        mCurrentPage++;
        return this.mCurrentPage;
    }

    public String getRowTag() {
        return this.mRowTag;
    }

    public List<Object> getPosts() {
        return mRowPosts;
    }

    public void addPosts(ArrayList<Object> posts) {
        if (posts.size() > 0) {
           // Collections.sort(posts);
            mRowPosts.addAll(posts);
            addAll(size(), posts);
            mPaginationEnabled = true;
        } else {
            mPaginationEnabled = false;
        }
    }

    public boolean isShowingRowLoadingIndicator() {
        return size() != 0 && get(size() - 1) instanceof LoadingCardView;
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
    }

}
