package com.hitherejoe.vineyard.data;

import android.content.Context;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.injection.component.DaggerDataManagerComponent;
import com.hitherejoe.vineyard.injection.module.DataManagerModule;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class DataManager {

    @Inject
    protected VineyardService mVineyardService;
    @Inject
    protected PreferencesHelper mPreferencesHelper;
    @Inject
    protected Bus mBus;
    @Inject
    protected Scheduler mSubscribeScheduler;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    /* This constructor is provided so we can set up a DataManager with mocks from unit test.
     * At the moment this is not possible to do with Dagger because the Gradle APT plugin doesn't
     * work for the unit test variant, plus Dagger 2 doesn't provide a nice way of overriding
     * modules */
    public DataManager(VineyardService ribotService,
                       Bus bus,
                       PreferencesHelper preferencesHelper,
                       Scheduler subscribeScheduler) {
        mVineyardService = ribotService;
        mBus = bus;
        mPreferencesHelper = preferencesHelper;
        mSubscribeScheduler = subscribeScheduler;
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

    public Scheduler getSubscribeScheduler() {
        return mSubscribeScheduler;
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

    public Observable<VineyardService.PostResponse> getPostsByTag(String tag, int page, String anchor) {
        return mVineyardService.getPostsByTag(tag, page, anchor);
    }
}
