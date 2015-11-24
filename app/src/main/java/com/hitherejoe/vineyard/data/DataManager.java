package com.hitherejoe.vineyard.data;

import android.content.Context;
import android.util.Log;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.injection.component.DaggerDataManagerComponent;
import com.hitherejoe.vineyard.injection.module.DataManagerModule;
import com.hitherejoe.vineyard.ui.fragment.SearchFragment;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public class DataManager {

    @Inject protected VineyardService mVineyardService;
    @Inject protected PreferencesHelper mPreferencesHelper;
    @Inject protected Bus mBus;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    protected void injectDependencies(Context context) {
        DaggerDataManagerComponent.builder()
                .applicationComponent(VineyardApplication.get(context).getComponent())
                .dataManagerModule(new DataManagerModule(context))
                .build()
                .inject(this);
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Observable<Authentication> getAccessToken(String username, String password) {
        return mVineyardService.getAccessToken(username, password).map(new Func1<Authentication, Authentication>() {
            @Override
            public Authentication call(Authentication authentication) {
                if (authentication.success) {
                    mPreferencesHelper.putAccessToken(authentication.data.key);
                    mPreferencesHelper.putUsername(authentication.data.username);
                    mPreferencesHelper.putUserId(authentication.data.userId);
                }
                return authentication;
            }
        });
    }

    public Observable<User> getSignedInUser() {
        return mVineyardService.getSignedInUser();
    }

    public Observable<User> getUser(String userId) {
        return mVineyardService.getUser(userId);
    }

    public Observable<VineyardService.PostResponse> getPopularPosts(int page, String anchor) {
        return mVineyardService.getPopularPosts(page, anchor);
    }

    public Observable<VineyardService.PostResponse> getEditorsPicksPosts(int page, String anchor) {
        return mVineyardService.getEditorsPicksPosts(page, anchor);
    }

    public Observable<VineyardService.PostResponse> getPostsByTag(String tag, int page, String anchor) {
        return mVineyardService.getPostsByTag(tag, page, anchor);
    }

    public Observable<VineyardService.PostResponse> getPostsByUser(String userId, int page, String anchor) {
        return mVineyardService.getUserTimeline(userId, page, anchor);
    }

    public Observable<VineyardService.TagResponse> searchByTag(String tag, int page, String anchor) {
        return mVineyardService.searchByTag(tag, page, anchor);
    }

    public Observable<VineyardService.UserResponse> searchByUser(String query, int page, String anchor) {
        return mVineyardService.searchByUser(query, page, anchor);
    }

    public Observable<SearchFragment.CombinedSearchResponse> search(
            String tag, int pageOne, String anchorOne, int pageTwo, String anchorTwo) {
        return Observable.zip(searchByTag(tag, pageOne, anchorOne), searchByUser(tag, pageTwo, anchorTwo),
                new Func2<VineyardService.TagResponse, VineyardService.UserResponse, SearchFragment.CombinedSearchResponse>() {
                    @Override
                    public SearchFragment.CombinedSearchResponse call(VineyardService.TagResponse tagResponse, VineyardService.UserResponse userResponse) {
                        List<Tag> tags = tagResponse.data.records;
                        List<User> users = userResponse.data.records;

                        ArrayList<Object> results = new ArrayList<>();
                        results.addAll(tags);
                        results.addAll(users);

                        Collections.sort(results, new Comparator<Object>() {
                            @Override
                            public int compare(Object lhs, Object rhs) {
                                if (lhs instanceof Tag) {
                                    Tag tag = (Tag) lhs;
                                    if (rhs instanceof Tag) {
                                        Tag tagTwo = (Tag) rhs;
                                        return (int) (tag.postCount - tagTwo.postCount);
                                    } else if (rhs instanceof User) {
                                        User user = (User) rhs;
                                        return (int) (tag.postCount - user.followerCount);
                                    }
                                } else if (lhs instanceof User) {
                                    User user = (User) lhs;
                                    if (rhs instanceof Tag) {
                                        Tag tagTwo = (Tag) rhs;
                                        return (int) (user.followerCount - tagTwo.postCount);
                                    } else if (rhs instanceof User) {
                                        User userTwo = (User) rhs;
                                        return user.followerCount - userTwo.followerCount;
                                    }
                                }
                                return 0;
                            }
                        });

                        SearchFragment.CombinedSearchResponse dualResponse = new SearchFragment.CombinedSearchResponse();
                        dualResponse.tagSearchAnchor = tagResponse.data.anchorStr;
                        dualResponse.userSearchAnchor = userResponse.data.anchorStr;
                        dualResponse.list = results;

                        return dualResponse;
                    }
                });
    }
}
