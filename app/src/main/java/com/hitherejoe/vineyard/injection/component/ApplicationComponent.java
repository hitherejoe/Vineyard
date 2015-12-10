package com.hitherejoe.vineyard.injection.component;

import android.app.Application;

import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.injection.module.ApplicationModule;
import com.hitherejoe.vineyard.injection.module.DefaultSchedulersModule;
import com.hitherejoe.vineyard.util.SchedulerApplier;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import rx.subscriptions.CompositeSubscription;

@Singleton
@Component(modules = {ApplicationModule.class, DefaultSchedulersModule.class})
public interface ApplicationComponent {

    void inject(SchedulerApplier.DefaultSchedulers defaultSchedulers);

    Application application();

    DataManager dataManager();

    Bus eventBus();

    CompositeSubscription compositeSubscription();
}
