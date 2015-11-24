package com.hitherejoe.vineyard.injection.module;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Provide default Rx Schedulers for observeOn and subscribeOn
 */
@Module
public class DefaultSchedulersModule {

    @Provides
    @Named(Names.SCHEDULER_OBSERVE_ON)
    Scheduler provideObserveOnScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named(Names.SCHEDULER_SUBSCRIBE_ON)
    Scheduler provideSubscribeOnScheduler() {
        return Schedulers.io();
    }

    public interface Names {
        String SCHEDULER_OBSERVE_ON = "scheduler_observe_on";
        String SCHEDULER_SUBSCRIBE_ON = "scheduler_subscribe_on";
    }
}
