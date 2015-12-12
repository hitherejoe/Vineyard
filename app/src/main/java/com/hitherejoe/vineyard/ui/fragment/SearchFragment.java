package com.hitherejoe.vineyard.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.text.TextUtils;

import com.hitherejoe.vineyard.R;
import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.ui.activity.BaseActivity;
import com.hitherejoe.vineyard.ui.activity.PlaybackActivity;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;
import com.hitherejoe.vineyard.ui.adapter.PaginationAdapter;
import com.hitherejoe.vineyard.ui.adapter.PostAdapter;
import com.hitherejoe.vineyard.ui.adapter.TagAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SearchFragment extends android.support.v17.leanback.app.SearchFragment
        implements android.support.v17.leanback.app.SearchFragment.SearchResultProvider {

    private static final boolean FINISH_ON_RECOGNIZER_CANCELED = true;
    private static final int REQUEST_SPEECH = 0x00000010;

    @Inject DataManager mDataManager;

    private ArrayObjectAdapter mRowsAdapter;
    private Subscription mUserSubscription;
    private Subscription mSubscription;
    private Subscription mTagSubscription;
    private String mSearchQuery;
    private Object mCurrentFilter;
    private TagAdapter mSearchResultsAdapter;
    private PostAdapter mPostResultsAdapter;

    private String mTagSearchAnchor;
    private String mUserSearchAnchor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setSearchResultProvider(this);
        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);
        mSearchResultsAdapter = new TagAdapter(getActivity(), "");
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                    try {
                        startActivityForResult(getRecognizerIntent(), REQUEST_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Timber.e("Cannot find activity for speech recognizer", e);
                    }
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mSubscription != null) mSubscription.unsubscribe();
        if (mTagSubscription != null) mTagSubscription.unsubscribe();
        if (mUserSubscription != null) mUserSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SPEECH:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        setSearchQuery(data, true);
                        break;
                    case Activity.RESULT_CANCELED:
                        if (FINISH_ON_RECOGNIZER_CANCELED) {
                            if (!hasResults()) {
                                getActivity().onBackPressed();
                            }
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        loadQuery(newQuery);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        loadQuery(query);
        return true;
    }

    public boolean hasResults() {
        return mRowsAdapter.size() > 0;
    }

    private boolean hasPermission(final String permission) {
        final Context context = getActivity();
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    private void loadQuery(String query) {
        if ((mSearchQuery != null && !mSearchQuery.equals(query))
                || (!TextUtils.isEmpty(query) && !query.equals("nil"))) {
            mSearchQuery = query;
            searchTaggedVideos(query);
        }
    }

    private void searchTaggedVideos(String tag) {
        mSearchResultsAdapter.setTag(tag);
        if (mRowsAdapter.size() == 0) {
            HeaderItem header = new HeaderItem(0, getString(R.string.text_search_results));
            mRowsAdapter.add(new ListRow(header, mSearchResultsAdapter));
        }
        loadSearchResults(mSearchResultsAdapter);
    }

    private void loadSearchResults(final PaginationAdapter arrayObjectAdapter) {
        if (mSubscription != null) mSubscription.unsubscribe();
        if (!arrayObjectAdapter.isShowingRowLoadingIndicator()) {
            arrayObjectAdapter.showRowLoadingIndicator();
        }

        String tag = arrayObjectAdapter.getRowTag();
        int nextPageFirst = arrayObjectAdapter.getNextPage();
        int nextPageSecond = arrayObjectAdapter.getNextPage();

        Observable<CombinedSearchResponse> observable =
                mDataManager.search(
                        tag, nextPageFirst, mTagSearchAnchor, nextPageSecond, mUserSearchAnchor);

        mSubscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<CombinedSearchResponse>() {
                    @Override
                    public void onCompleted() {
                        arrayObjectAdapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the videos");
                        arrayObjectAdapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onNext(CombinedSearchResponse dualResponse) {
                        arrayObjectAdapter.addAllItems(dualResponse.list);
                        mTagSearchAnchor = dualResponse.tagSearchAnchor;
                        mUserSearchAnchor = dualResponse.userSearchAnchor;
                    }
                });
    }

    private void addPageLoadSubscriptionByTag(final PaginationAdapter adapter) {
        unSubscribeSearchObservables();

        if (!adapter.isShowingRowLoadingIndicator()) {
            adapter.showRowLoadingIndicator();
        }

        String tag = adapter.getRowTag();
        String anchor = adapter.getAnchor();
        int nextPage = adapter.getNextPage();

        mTagSubscription = mDataManager.getPostsByTag(tag, nextPage, anchor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                        adapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the videos");
                        adapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onNext(VineyardService.PostResponse postResponse) {
                        adapter.setAnchor(postResponse.data.anchorStr);
                        adapter.addAllItems(postResponse.data.records);
                    }
                });
    }

    private void addPageLoadSubscriptionByUser(final PaginationAdapter adapter) {
        unSubscribeSearchObservables();
        if (!adapter.isShowingRowLoadingIndicator()) {
            adapter.showRowLoadingIndicator();
        }

        String tag = adapter.getRowTag();
        String anchor = adapter.getAnchor();
        int nextPage = adapter.getNextPage();

        mUserSubscription = mDataManager.getPostsByUser(tag, nextPage, anchor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                        adapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onError(Throwable e) {
                        adapter.removeLoadingIndicator();
                        Timber.e(e, "There was an error loading the videos");
                    }

                    @Override
                    public void onNext(VineyardService.PostResponse postResponse) {
                        adapter.setAnchor(postResponse.data.anchorStr);
                        adapter.setNextPage(postResponse.data.nextPage);
                        adapter.addAllItems(postResponse.data.records);
                    }
                });
    }

    private void unSubscribeSearchObservables() {
        if (mUserSubscription != null) mUserSubscription.unsubscribe();
        if (mTagSubscription != null) mTagSubscription.unsubscribe();
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                Post post = (Post) item;
                int index = mRowsAdapter.indexOf(row);
                PostAdapter arrayObjectAdapter =
                        ((PostAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                ArrayList<Post> postList = (ArrayList<Post>) arrayObjectAdapter.getAllItems();
                startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
            } else if (item instanceof Tag) {
                Tag tag = (Tag) item;
                startActivity(PostGridActivity.newStartIntent(getActivity(), tag));
            } else if (item instanceof User) {
                User user = (User) item;
                startActivity(PostGridActivity.newStartIntent(getActivity(), user));
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Tag || item instanceof User) {
                boolean isValid = true;
                if (mCurrentFilter != null && mCurrentFilter.equals(item)) isValid = false;
                mCurrentFilter = item;
                if (isValid) {
                    int index = mRowsAdapter.indexOf(row);
                    PaginationAdapter arrayObjectAdapter =
                            ((PaginationAdapter) ((ListRow) mRowsAdapter.get(index)).getAdapter());
                    List<Object> posts = arrayObjectAdapter.getItems();
                    if (item.equals(posts.get(posts.size() - 1))) {
                        if (arrayObjectAdapter.shouldLoadNextPage()) {
                            loadSearchResults(arrayObjectAdapter);
                        }
                    }
                    if (item instanceof Tag) {
                        Tag tagOne = (Tag) item;
                        String tag = tagOne.tag;
                        arrayObjectAdapter.setTag(tag);

                        setListAdapterData(tag);
                        addPageLoadSubscriptionByTag(mPostResultsAdapter);
                    } else {
                        User user = (User) item;
                        String tag = user.userId;
                        arrayObjectAdapter.setTag(tag);

                        setListAdapterData(tag);
                        addPageLoadSubscriptionByUser(mPostResultsAdapter);
                    }
                }
            }
        }
    };

    private void setListAdapterData(String tag) {
        if (mPostResultsAdapter == null) {
            mPostResultsAdapter = new PostAdapter(getActivity(), tag);
            HeaderItem header = new HeaderItem(1, getString(R.string.text_post_results_title, tag));
            mRowsAdapter.add(new ListRow(header, mPostResultsAdapter));
        }
        mPostResultsAdapter.setTag(tag);
        mPostResultsAdapter.setAnchor("");
        if (mPostResultsAdapter.isShowingRowLoadingIndicator()) {
            mPostResultsAdapter.removeItems(1, mPostResultsAdapter.size() - 2);
        } else {
            mPostResultsAdapter.clear();
        }
    }

    public static class CombinedSearchResponse {
        public String tagSearchAnchor;
        public String userSearchAnchor;
        public ArrayList<Object> list;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CombinedSearchResponse that = (CombinedSearchResponse) o;

            if (tagSearchAnchor != null
                    ? !tagSearchAnchor.equals(that.tagSearchAnchor)
                    : that.tagSearchAnchor != null)
                return false;
            if (userSearchAnchor != null
                    ? !userSearchAnchor.equals(that.userSearchAnchor)
                    : that.userSearchAnchor != null)
                return false;
            return !(list != null ? !list.equals(that.list) : that.list != null);

        }

        @Override
        public int hashCode() {
            int result = tagSearchAnchor != null ? tagSearchAnchor.hashCode() : 0;
            result = 31 * result + (userSearchAnchor != null ? userSearchAnchor.hashCode() : 0);
            result = 31 * result + (list != null ? list.hashCode() : 0);
            return result;
        }
    }

}