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
import android.widget.Toast;

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
import com.hitherejoe.vineyard.ui.presenter.CardPresenter;

import java.util.ArrayList;
import java.util.Map;

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

    private ArrayObjectAdapter mResultsAdapter;
    private HeaderItem mResultsHeader;
    private Object mSelectedTag;
    private PostAdapter mPostResultsAdapter;
    private Subscription mSearchResultsSubscription;
    private Subscription mTagSubscription;
    private Subscription mUserSubscription;
    private TagAdapter mSearchResultsAdapter;

    private String mSearchQuery;
    private String mTagSearchAnchor;
    private String mUserSearchAnchor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        mResultsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        mSearchResultsAdapter = new TagAdapter(getActivity(), "");
        setSearchResultProvider(this);
        setListeners();
    }

    @Override
    public void onDestroy() {
        if (mSearchResultsSubscription != null) mSearchResultsSubscription.unsubscribe();
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
                            if (!hasResults()) getActivity().onBackPressed();
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mResultsAdapter;
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
        return mResultsAdapter.size() > 0;
    }

    private void setListeners() {
        setOnItemViewClickedListener(mOnItemViewClickedListener);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);
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

    private boolean hasPermission(final String permission) {
        final Context context = getActivity();
        return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(
                permission, context.getPackageName());
    }

    private void loadQuery(String query) {
        if ((mSearchQuery != null && !mSearchQuery.equals(query))
                && query.trim().length() > 0
                || (!TextUtils.isEmpty(query) && !query.equals("nil"))) {
            mSearchQuery = query;
            searchTaggedPosts(query);
        }
    }

    private void searchTaggedPosts(String tag) {
        mSearchResultsAdapter.setTag(tag);
        mResultsAdapter.clear();
        mResultsHeader = new HeaderItem(0, getString(R.string.text_search_results));
        mResultsAdapter.add(new ListRow(mResultsHeader, mSearchResultsAdapter));
        performSearch(mSearchResultsAdapter);
    }

    private void performSearch(final PaginationAdapter adapter) {
        if (adapter.shouldShowLoadingIndicator()) adapter.showLoadingIndicator();
        if (mSearchResultsSubscription != null && !mSearchResultsSubscription.isUnsubscribed()) {
            mSearchResultsSubscription.unsubscribe();
        }
        if (mPostResultsAdapter != null) mPostResultsAdapter.clear();
        adapter.clear();

        Map<String, String> options = adapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        Observable<CombinedSearchResponse> observable =
                mDataManager.search(
                        tag, nextPage, mTagSearchAnchor, nextPage, mUserSearchAnchor);

        mSearchResultsSubscription = observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<CombinedSearchResponse>() {
                    @Override
                    public void onCompleted() {
                        adapter.removeLoadingIndicator();
                    }

                    @Override
                    public void onError(Throwable e) {
                        //TODO: Handle error
                        adapter.removeLoadingIndicator();
                        Toast.makeText(
                                getActivity(),
                                getString(R.string.error_message_retrieving_results),
                                Toast.LENGTH_SHORT
                        ).show();
                        Timber.e("There was an error loading the videos", e);
                    }

                    @Override
                    public void onNext(CombinedSearchResponse dualResponse) {
                        if (dualResponse.list.isEmpty()) {
                            mResultsAdapter.clear();
                            mResultsHeader = new HeaderItem(0, getString(R.string.text_no_results));
                            mResultsAdapter.add(new ListRow(mResultsHeader, adapter));
                            mTagSearchAnchor = "";
                            mUserSearchAnchor = "";
                        } else {
                            adapter.addAllItems(dualResponse.list);
                            mTagSearchAnchor = dualResponse.tagSearchAnchor;
                            mUserSearchAnchor = dualResponse.userSearchAnchor;
                        }
                    }
                });
    }

    private void addPageLoadSubscriptionByTag(final PaginationAdapter adapter) {
        unSubscribeSearchObservables();
        if (adapter.shouldShowLoadingIndicator()) adapter.showLoadingIndicator();

        Map<String, String> options = adapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        mTagSubscription = mDataManager.getPostsByTag(tag, nextPage, anchor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0) {
                            adapter.showTryAgainCard();
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
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0 && postResponse.data.records.isEmpty()) {
                            adapter.showReloadCard();
                        } else {
                            adapter.setAnchor(postResponse.data.anchorStr);
                            adapter.setNextPage(postResponse.data.nextPage);
                            adapter.addAllItems(postResponse.data.records);
                        }
                    }
                });
    }

    private void addPageLoadSubscriptionByUser(final PaginationAdapter adapter) {
        unSubscribeSearchObservables();
        if (adapter.shouldShowLoadingIndicator()) adapter.showLoadingIndicator();

        Map<String, String> options = adapter.getAdapterOptions();
        String tag = options.get(PaginationAdapter.KEY_TAG);
        String anchor = options.get(PaginationAdapter.KEY_ANCHOR);
        String nextPage = options.get(PaginationAdapter.KEY_NEXT_PAGE);

        mUserSubscription = mDataManager.getPostsByUser(tag, nextPage, anchor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VineyardService.PostResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0) {
                            adapter.showTryAgainCard();
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
                        adapter.removeLoadingIndicator();
                        if (adapter.size() == 0 && postResponse.data.records.isEmpty()) {
                            adapter.showReloadCard();
                        } else {
                            adapter.setAnchor(postResponse.data.anchorStr);
                            adapter.setNextPage(postResponse.data.nextPage);
                            adapter.addAllItems(postResponse.data.records);
                        }
                    }
                });
    }

    private void unSubscribeSearchObservables() {
        if (mUserSubscription != null && !mUserSubscription.isUnsubscribed()) {
            mUserSubscription.unsubscribe();
        }
        if (mTagSubscription != null && !mTagSubscription.isUnsubscribed()) {
            mTagSubscription.unsubscribe();
        }
    }

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Post) {
                Post post = (Post) item;
                int index = mResultsAdapter.indexOf(row);
                PostAdapter arrayObjectAdapter =
                        ((PostAdapter) ((ListRow) mResultsAdapter.get(index)).getAdapter());
                ArrayList<Post> postList = (ArrayList<Post>) arrayObjectAdapter.getAllItems();
                startActivity(PlaybackActivity.newStartIntent(getActivity(), post, postList));
            } else if (item instanceof Tag) {
                Tag tag = (Tag) item;
                startActivity(PostGridActivity.getStartIntent(getActivity(), PostGridActivity.TYPE_TAG, tag.tag));
            } else if (item instanceof User) {
                User user = (User) item;
                startActivity(PostGridActivity.getStartIntent(getActivity(), PostGridActivity.TYPE_USER, user.userId));
            } else if (item instanceof String) {
                if (item.equals(CardPresenter.ITEM_RELOAD) ||
                        item.equals(CardPresenter.ITEM_TRY_AGAIN)) {
                    int index = mResultsAdapter.indexOf(row);
                    PostAdapter adapter =
                            ((PostAdapter) ((ListRow) mResultsAdapter.get(index)).getAdapter());
                    adapter.removeReloadCard();
                    if (mSelectedTag instanceof Tag) {
                        addPageLoadSubscriptionByTag(adapter);
                    } else if (mSelectedTag instanceof User) {
                        addPageLoadSubscriptionByUser(adapter);
                    }
                }
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Tag || item instanceof User) {
                boolean isValid = true;
                if (mSelectedTag != null && mSelectedTag.equals(item)) isValid = false;
                mSelectedTag = item;
                if (isValid) {
                    int index = mResultsAdapter.indexOf(row);
                    PaginationAdapter adapter =
                            ((PaginationAdapter) ((ListRow) mResultsAdapter.get(index)).getAdapter());

                    if (item instanceof Tag) {
                        Tag tagOne = (Tag) item;
                        String tag = tagOne.tag;
                        adapter.setTag(tag);

                        setListAdapterData(tag);
                        addPageLoadSubscriptionByTag(mPostResultsAdapter);
                    } else {
                        User user = (User) item;
                        String tag = user.userId;
                        adapter.setTag(tag);

                        setListAdapterData(tag);
                        addPageLoadSubscriptionByUser(mPostResultsAdapter);
                    }
                }
            }
        }
    };

    private void setListAdapterData(String tag) {
        if (mPostResultsAdapter != null) {
            mResultsAdapter.remove(mPostResultsAdapter);
        }
        if (mPostResultsAdapter == null) {
            mPostResultsAdapter = new PostAdapter(getActivity(), tag);
        }
        mResultsAdapter.removeItems(1, 1);
        HeaderItem postResultsHeader = new HeaderItem(1, getString(R.string.text_post_results_title, tag));
        mResultsAdapter.add(new ListRow(postResultsHeader, mPostResultsAdapter));

        mPostResultsAdapter.setTag(tag);
        mPostResultsAdapter.setAnchor("");
        if (!mPostResultsAdapter.shouldShowLoadingIndicator()) {
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