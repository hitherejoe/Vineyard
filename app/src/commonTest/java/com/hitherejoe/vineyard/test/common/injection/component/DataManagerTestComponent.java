package com.hitherejoe.vineyard.test.common.injection.component;

import com.hitherejoe.vineyard.injection.component.DataManagerComponent;
import com.hitherejoe.vineyard.injection.scope.PerDataManager;
import com.hitherejoe.vineyard.test.common.injection.module.DataManagerTestModule;

import dagger.Component;

@PerDataManager
@Component(dependencies = TestComponent.class, modules = DataManagerTestModule.class)
public interface DataManagerTestComponent extends DataManagerComponent {
}
