package com.hitherejoe.vineyard.test.common.injection.module;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class DefaultSchedulersTestModule {

    @Provides
    @Named(DefaultSchedulersModule.Names.SCHEDULER_OBSERVE_ON)
    Scheduler provideObserveOnScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Provides
    @Named(DefaultSchedulersModule.Names.SCHEDULER_SUBSCRIBE_ON)
    Scheduler provideSubscribeOnScheduler() {
        return Schedulers.immediate();
    }

}
