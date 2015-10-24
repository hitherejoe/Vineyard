package com.hitherejoe.androidboilerplate.data;

import android.content.Context;

import com.hitherejoe.androidboilerplate.AndroidBoilerplateApplication;
import com.hitherejoe.androidboilerplate.data.local.PreferencesHelper;
import com.hitherejoe.androidboilerplate.data.remote.AndroidBoilerplateService;
import com.hitherejoe.androidboilerplate.injection.component.DaggerDataManagerComponent;
import com.hitherejoe.androidboilerplate.injection.module.DataManagerModule;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import rx.Scheduler;

public class DataManager {

    @Inject protected AndroidBoilerplateService mAndroidBoilerplateService;
    @Inject protected PreferencesHelper mPreferencesHelper;
    @Inject protected Scheduler mSubscribeScheduler;
    @Inject protected Bus mEventBus;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    /* This constructor is provided so we can set up a DataManager with mocks from unit test.
     * At the moment this is not possible to do with Dagger because the Gradle APT plugin doesn't
     * work for the unit test variant, plus Dagger 2 doesn't provide a nice way of overriding
     * modules */
    public DataManager(AndroidBoilerplateService watchTowerService,
                       Bus eventBus,
                       PreferencesHelper preferencesHelper,
                       Scheduler subscribeScheduler) {
        mAndroidBoilerplateService = watchTowerService;
        mEventBus = eventBus;
        mPreferencesHelper = preferencesHelper;
        mSubscribeScheduler = subscribeScheduler;
    }

    protected void injectDependencies(Context context) {
        DaggerDataManagerComponent.builder()
                .applicationComponent(AndroidBoilerplateApplication.get(context).getComponent())
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

}
