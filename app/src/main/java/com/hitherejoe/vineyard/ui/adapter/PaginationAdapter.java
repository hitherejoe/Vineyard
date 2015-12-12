package com.hitherejoe.vineyard.ui.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;

import com.hitherejoe.vineyard.ui.widget.LoadingCardView;
import com.hitherejoe.vineyard.ui.presenter.LoadingPresenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class PaginationAdapter extends ArrayObjectAdapter {

    public static final String KEY_TAG = "tag";
    public static final String KEY_ANCHOR = "anchor";
    public static final String KEY_NEXT_PAGE = "next_page";

    private Context mContext;
    private Integer mNextPage;
    private LoadingPresenter mLoadingPresenter;
    private Presenter mPresenter;

    private String mRowTag;
    private String mAnchor;
    private int mLoadingIndicatorPosition;


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

    public List<Object> getItems() {
        return unmodifiableList();
    }

    public boolean shouldShowLoadingIndicator() {
        return mLoadingIndicatorPosition == -1;
    }

    public void showLoadingIndicator() {
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
            mNextPage = 0;
        }
    }

    public boolean shouldLoadNextPage() {
        return shouldShowLoadingIndicator() && mNextPage != 0;
    }

    public Map<String, String> getAdapterOptions() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(KEY_TAG, mRowTag);
        map.put(KEY_ANCHOR, mAnchor);
        map.put(KEY_NEXT_PAGE, String.valueOf(mNextPage.toString()));
        return map;
    }

    public abstract void addAllItems(List<?> items);

    public abstract List<?> getAllItems();


}
