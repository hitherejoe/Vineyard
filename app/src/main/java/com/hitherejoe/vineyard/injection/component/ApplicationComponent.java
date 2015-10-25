package com.hitherejoe.vineyard.injection.component;

import android.app.Application;

import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.injection.module.ApplicationModule;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import rx.subscriptions.CompositeSubscription;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {


    Application application();
    DataManager dataManager();
    Bus eventBus();
    CompositeSubscription compositeSubscription();
}
