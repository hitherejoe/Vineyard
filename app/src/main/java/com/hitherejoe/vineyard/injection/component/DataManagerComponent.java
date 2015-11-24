package com.hitherejoe.vineyard.injection.component;


import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.injection.module.DataManagerModule;
import com.hitherejoe.vineyard.injection.scope.PerDataManager;

import dagger.Component;

@PerDataManager
@Component(dependencies = ApplicationComponent.class, modules = DataManagerModule.class)
public interface DataManagerComponent {

    void inject(DataManager dataManager);
}
