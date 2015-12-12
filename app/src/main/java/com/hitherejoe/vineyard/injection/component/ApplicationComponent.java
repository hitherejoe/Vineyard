package com.hitherejoe.vineyard.injection.component;

import android.app.Application;
import android.content.Context;

import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.injection.ApplicationContext;
import com.hitherejoe.vineyard.injection.module.ApplicationModule;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import rx.subscriptions.CompositeSubscription;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    @ApplicationContext
    Context context();
    Application application();
    PreferencesHelper preferencesHelper();
    DataManager dataManager();
    CompositeSubscription compositeSubscription();
    Bus eventBus();

}