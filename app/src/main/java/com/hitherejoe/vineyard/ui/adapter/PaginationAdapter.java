package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.hitherejoe.vineyard.ui.LoadingCardView;
import com.hitherejoe.vineyard.ui.LoadingPresenter;

import java.util.List;


public abstract class PaginationAdapter extends ArrayObjectAdapter {

    private Presenter mPresenter;
    private LoadingPresenter mLoadingPresenter;
    private int mLoadingIndicatorPosition;
    private Integer mNextPage;
    private String mRowTag;
    private Context mContext;
    private String mAnchor;

    public PaginationAdapter(Context context, Presenter presenter, String tag) {
        mContext = context;
        mPresenter = presenter;
        mLoadingPresenter = new LoadingPresenter();
        mNextPage = 1;
        mRowTag = tag;
        setPresenterSelector();
    }

    public void setTag(String tag) {
        mRowTag = tag;
    }

    public void setNextPage(int page) {
        mNextPage = page;
    }

    public void setPresenterSelector() {
        setPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                if (item instanceof LoadingCardView) {
                    return mLoadingPresenter;
                }
                return mPresenter;
            }
        });
    }

    public String getAnchor() {
        return this.mAnchor;
    }

    public Integer getNextPage() {
        return this.mNextPage;
    }

    public String getRowTag() {
        return this.mRowTag;
    }

    public List<Object> getItems() {
        return unmodifiableList();
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

    public void addPosts(List<?> posts) {
        if (posts.size() > 0) {
            addAll(size(), posts);
        } else {
            mNextPage = null;
        }
    }

    public boolean shouldLoadNextPage() {
        return !isShowingRowLoadingIndicator() && getNextPage() != 0;
    }

    public abstract void addAllItems(List<?> items);

    public abstract List<?> getAllItems();


}
