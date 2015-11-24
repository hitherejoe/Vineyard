package com.hitherejoe.vineyard.util;

import android.content.Context;

/**
 * Provide singleton instances of common SchedulerAppliers that can be used across the application
 * without having to instantiate new SchedulerApplier every time we subscribe to an observable.
 */
public final class SchedulerAppliers {

    private static SchedulerApplier mSchedulerApplierAll;
    private static SchedulerApplier mSchedulerApplierSubscribe;

    private SchedulerAppliers() {

    }

    /**
     * Returns a singleton instance of an SchedulerApplier that appliers the default
     * subscribeOn and observeOn schedulers.
     */
    @SuppressWarnings("unchecked")
    public static <T> SchedulerApplier<T> defaultSchedulers(Context context) {
        if (mSchedulerApplierAll == null) {
            mSchedulerApplierAll = new SchedulerApplier<T>(context);
        }
        return mSchedulerApplierAll;
    }

    /**
     * Returns a singleton instance of an SchedulerApplier that appliers the default
     * subscribeOn scheduler.
     */
    @SuppressWarnings("unchecked")
    public static <T> SchedulerApplier<T> defaultSubscribeScheduler(Context context) {
        if (mSchedulerApplierSubscribe == null) {
            mSchedulerApplierSubscribe = new SchedulerApplier<T>(context,
                    SchedulerApplier.DEFAULT_SUBSCRIBE);
        }
        return mSchedulerApplierSubscribe;
    }

}
