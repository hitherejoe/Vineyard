package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.test.common.ClearDataRule;
import com.hitherejoe.vineyard.test.common.MockModelFabric;
import com.hitherejoe.vineyard.test.common.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.LauncherActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final TestComponentRule component = new TestComponentRule(
            VineyardApplication.get(InstrumentationRegistry.getTargetContext()),
            false);

    @Rule
    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    @Rule
    public final ActivityTestRule<LauncherActivity> main =
            new ActivityTestRule<>(LauncherActivity.class, false, false);

    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    @Test
    public void testAllCategoriesShown() {
        doReturn(Observable.just(MockModelFabric.createMockListOfPosts(17)))
                .when(component.getDataManager())
                .getPopularPosts(anyInt(), anyString());
        doReturn(Observable.just(MockModelFabric.createMockListOfPosts(17)))
                .when(component.getDataManager())
                .getPostsByTag(anyString(), anyInt(), anyString());

        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));
    }

}