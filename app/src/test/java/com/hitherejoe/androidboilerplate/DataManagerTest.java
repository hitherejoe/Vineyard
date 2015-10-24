package com.hitherejoe.androidboilerplate;


import com.hitherejoe.androidboilerplate.data.DataManager;
import com.hitherejoe.androidboilerplate.data.remote.AndroidBoilerplateService;
import com.hitherejoe.androidboilerplate.util.DefaultConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK, manifest = DefaultConfig.MANIFEST)
public class DataManagerTest {

    private DataManager mDataManager;
    private AndroidBoilerplateService mMockAndroidBoilerplateService;

    @Before
    public void setUp() {
        mMockAndroidBoilerplateService = mock(AndroidBoilerplateService.class);

    }

    @Test
    public void shouldDoSomething() throws Exception {

    }

}
