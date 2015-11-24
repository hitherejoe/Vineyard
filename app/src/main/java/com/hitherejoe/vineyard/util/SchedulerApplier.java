package com.hitherejoe.vineyard.util;


import android.content.Context;
import android.support.annotation.IntDef;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.injection.module.DefaultSchedulersModule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.Scheduler;

/**
 * This class transform an Observable by applying some default schedulers.
 * The default schedulers are Injected via Dagger, therefore it enables injecting different
 * default schedulers when running tests. This is useful to ensure that observable always
 * run in the same thread as the tests.
 */
public final class SchedulerApplier<T> implements Observable.Transformer<T, T> {

    @IntDef({DEFAULT_ALL, DEFAULT_SUBSCRIBE, DEFAULT_OBSERVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ApplyTarget {
    }

    public static final int DEFAULT_ALL = 0;
    public static final int DEFAULT_SUBSCRIBE = 1;
    public static final int DEFAULT_OBSERVE = 2;

    private DefaultSchedulers mDefaultSchedulers;
    private int mApplyTarget;

    /**
     * Scheduler applier that applies the default subscribeOn and observeOn schedulers
     * provided by DefaultSchedulersModule.
     * Default values are observeOn(AndroidSchedulers.mainThread()) and subscribeOn(Schedulers.io())
     * <p/>
     * This is the recommended constructor because it handles injecting different default
     * schedulers when running tests. During tests Schedulers.immediate() becomes the
     * default observeOn scheduler ensuring that the observable runs in the same thread as the tests
     */
    public SchedulerApplier(Context context) {
        mDefaultSchedulers = new DefaultSchedulers(context);
        mApplyTarget = DEFAULT_ALL;
    }

    /**
     * Scheduler applier that applies the default subscribeOn and/or observeOn schedulers
     * given an applyTarget.
     *
     * @param applyTarget indicate the target where to apply the default schedules:
     *                    DEFAULT_ALL: observeOn(mainThread()) and subscribeOn(io())
     *                    DEFAULT_SUBSCRIBE: subscribeOn(io())
     *                    DEFAULT_OBSERVE: observeOn(mainThread())
     */
    public SchedulerApplier(Context context, @ApplyTarget int applyTarget) {
        mDefaultSchedulers = new DefaultSchedulers(context);
        mApplyTarget = applyTarget;
    }

    /**
     * Use SchedulerApplier(Context) constructor if possible. Only use this one if
     * you need to apply different schedulers to the default ones.
     * If you use this constructor your observable may run on a different thread during
     * tests so be aware when writing tests for it.
     */
    public SchedulerApplier(Scheduler observeOnScheduler, Scheduler subscribeOnScheduler) {
        mDefaultSchedulers = new DefaultSchedulers(observeOnScheduler, subscribeOnScheduler);
        mApplyTarget = DEFAULT_ALL;
    }

    @Override
    public Observable<T> call(Observable<T> observable) {
        switch (mApplyTarget) {
            case DEFAULT_ALL:
                return observable.observeOn(mDefaultSchedulers.observeOnScheduler)
                        .subscribeOn(mDefaultSchedulers.subscribeOnScheduler);
            case DEFAULT_SUBSCRIBE:
                return observable.subscribeOn(mDefaultSchedulers.subscribeOnScheduler);
            case DEFAULT_OBSERVE:
                return observable.observeOn(mDefaultSchedulers.observeOnScheduler);
        }
        return observable;
    }

    public static final class DefaultSchedulers {

        @Inject
        @Named(DefaultSchedulersModule.Names.SCHEDULER_OBSERVE_ON)
        Scheduler observeOnScheduler;

        @Inject
        @Named(DefaultSchedulersModule.Names.SCHEDULER_SUBSCRIBE_ON)
        Scheduler subscribeOnScheduler;

        public DefaultSchedulers(Context context) {
            VineyardApplication.get(context).getComponent().inject(this);
        }

        public DefaultSchedulers(Scheduler observeOnScheduler, Scheduler subscribeOnScheduler) {
            this.observeOnScheduler = observeOnScheduler;
            this.subscribeOnScheduler = subscribeOnScheduler;
        }
    }

}
