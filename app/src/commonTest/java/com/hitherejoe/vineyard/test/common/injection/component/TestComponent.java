package com.hitherejoe.vineyard.test.common.injection.component;


import com.hitherejoe.vineyard.injection.component.ApplicationComponent;
import com.hitherejoe.vineyard.test.common.injection.module.ApplicationTestModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationTestModule.class)
public interface TestComponent extends ApplicationComponent {

}