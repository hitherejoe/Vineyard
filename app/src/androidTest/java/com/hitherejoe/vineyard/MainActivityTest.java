package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.MockModelFabric;
import com.hitherejoe.vineyard.test.common.rules.ClearDataRule;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext(), true);

    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<>(MainActivity.class, false, false);

    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    @Test
    public void testAllCategoriesShown() {
        List<Post> mockPosts = MockModelFabric.createMockListOfPosts(17);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;

        when(component.getMockVineyardService().getPopularPosts(anyInt(), anyString()))
                .thenReturn(Observable.just(postResponse));

        List<Post> mockTagPosts = MockModelFabric.createMockListOfPosts(17);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = mockTagPosts;
        postTagResponse.data = tagData;

        when(component.getMockVineyardService().getPostsByTag(anyString(), anyInt(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        List<Post> mockEditorsPosts = MockModelFabric.createMockListOfPosts(17);
        VineyardService.PostResponse postEditosResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data editorsData = new VineyardService.PostResponse.Data();
        editorsData.records = mockEditorsPosts;
        postEditosResponse.data = editorsData;

        when(component.getMockVineyardService().getEditorsPicksPosts(anyInt(), anyString()))
                .thenReturn(Observable.just(postEditosResponse));

        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));
    }

}