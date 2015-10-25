package com.hitherejoe.vineyard;


import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.util.DefaultConfig;
import com.hitherejoe.vineyard.util.MockModelsUtil;
import com.squareup.otto.Bus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK, manifest = DefaultConfig.MANIFEST)
public class DataManagerTest {

    private DataManager mDataManager;
    private VineyardService mMockVineyardService;


    @Before
    public void setUp() {
        mMockVineyardService = mock(VineyardService.class);
        mDataManager = new DataManager(mMockVineyardService,
                mock(Bus.class),
                new PreferencesHelper(RuntimeEnvironment.application),
                Schedulers.immediate());
    }

    @Test
    public void shouldGetAccessToken() throws Exception {
        Authentication mockAuthentication = MockModelsUtil.createMockSuccessAuthentication();
        when(mMockVineyardService.getAccessToken(anyString(), anyString()))
                .thenReturn(Observable.just(mockAuthentication));

        TestSubscriber<Authentication> result = new TestSubscriber<>();
        mDataManager.getAccessToken("", "").subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockAuthentication);

        assertEquals(mockAuthentication.data.key, mDataManager.getPreferencesHelper().getAccessToken());
    }

    @Test
    public void shouldFailGetAccessToken() throws Exception {
        Authentication mockAuthentication = MockModelsUtil.createMockErrorAuthentication();
        when(mMockVineyardService.getAccessToken(anyString(), anyString()))
                .thenReturn(Observable.just(mockAuthentication));

        TestSubscriber<Authentication> result = new TestSubscriber<>();
        mDataManager.getAccessToken("", "").subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockAuthentication);

        assertEquals(mDataManager.getPreferencesHelper().getAccessToken(), null);
    }

    @Test
    public void shouldGetSignedInUser() throws Exception {
        User mockUser = MockModelsUtil.createMockUser();
        when(mMockVineyardService.getSignedInUser())
                .thenReturn(Observable.just(mockUser));

        TestSubscriber<User> result = new TestSubscriber<>();
        mDataManager.getSignedInUser().subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockUser);
    }

    @Test
    public void shouldGetSignedInUserById() throws Exception {
        User mockUser = MockModelsUtil.createMockUser();
        when(mMockVineyardService.getUser(mockUser.data.userId))
                .thenReturn(Observable.just(mockUser));

        TestSubscriber<User> result = new TestSubscriber<>();
        mDataManager.getUser(mockUser.data.userId).subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockUser);
    }

    @Test
    public void shouldGetPopularPosts() throws Exception {
        List<Post> mockPostLists = MockModelsUtil.createMockListOfPosts(20);
        VineyardService.PopularResponse popularResponse = new VineyardService.PopularResponse();
        popularResponse.code = "200";
        popularResponse.data = new VineyardService.PopularResponse.Data();
        popularResponse.data.count = 20;
        popularResponse.data.records = mockPostLists;
        when(mMockVineyardService.getPopularPosts())
                .thenReturn(Observable.just(popularResponse));

        TestSubscriber<List<Post>> result = new TestSubscriber<>();
        mDataManager.getPopularPosts().subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockPostLists);
    }

}
