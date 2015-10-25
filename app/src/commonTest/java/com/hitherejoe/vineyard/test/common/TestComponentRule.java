package com.hitherejoe.vineyard.test.common;

import android.content.Context;

import com.hitherejoe.vineyard.VineyardApplication;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.injection.component.DaggerTestComponent;
import com.hitherejoe.vineyard.test.common.injection.component.TestComponent;
import com.hitherejoe.vineyard.test.common.injection.module.ApplicationTestModule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Test rule that creates and sets a Dagger TestComponent into the application overriding the
 * existing application component.
 * Use this rule in your test case in order for the app to use mock dependencies.
 * It also exposes some of the dependencies so they can be easily accessed from the tests, e.g. to
 * stub mocks etc.
 */
public class TestComponentRule implements TestRule {

    private TestComponent mTestComponent;
    private VineyardApplication mVineyardApplication;
    private boolean mMockableDataManager;

    public TestComponentRule(VineyardApplication vineyardApplication) {
        mVineyardApplication = vineyardApplication;
        mMockableDataManager = false;
    }

    /**
     * If mockableDataManager is true, it will crate a data manager using Mockity.spy()
     * Spy objects call real methods unless they are stubbed. So the DataManager will work as
     * usual unless an specific method is mocked.
     * A full mock DataManager is not an option because there are several methods that still
     * need to return the real value, i.e dataManager.getSubscribeScheduler()
     */
    public TestComponentRule(VineyardApplication vineyardApplication, boolean mockableDataManager) {
        mVineyardApplication = vineyardApplication;
        mMockableDataManager = mockableDataManager;
    }

    public Context getApplication() {
        return mVineyardApplication;
    }

    public TestComponent getTestComponent() {
        return mTestComponent;
    }

    public TestDataManager getDataManager() {
        return (TestDataManager) mTestComponent.dataManager();
    }

    public VineyardService getMockRibotService() {
        return getDataManager().getRibotService();
    }

    public PreferencesHelper getPreferencesHelper() {
        return getDataManager().getPreferencesHelper();
    }

    private void setupDaggerTestComponentInApplication() {
        if (mVineyardApplication.getComponent() instanceof TestComponent) {
            mTestComponent = (TestComponent) mVineyardApplication.getComponent();
        } else {
            mTestComponent = DaggerTestComponent.builder()
                    .applicationTestModule(
                            new ApplicationTestModule(mVineyardApplication, mMockableDataManager))
                    .build();
            mVineyardApplication.setComponent(mTestComponent);
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setupDaggerTestComponentInApplication();
                    base.evaluate();
                } finally {
                    mTestComponent = null;
                }
            }
        };
    }
}
