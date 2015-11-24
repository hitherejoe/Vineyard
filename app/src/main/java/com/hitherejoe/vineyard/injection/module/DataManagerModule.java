package com.hitherejoe.vineyard.injection.module;

import android.content.Context;

import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.injection.scope.PerDataManager;

import dagger.Module;
import dagger.Provides;

/**
 * Provide dependencies to the DataManager, mainly Helper classes and Retrofit services.
 */
@Module
public class DataManagerModule {

    private final Context mContext;

    public DataManagerModule(Context context) {
        mContext = context;
    }

    @Provides
    @PerDataManager
    PreferencesHelper providePreferencesHelper() {
        return new PreferencesHelper(mContext);
    }

    @Provides
    @PerDataManager
    VineyardService provideVineyardService() {
        return VineyardService.Factory.makeVineyardService();
    }
}
