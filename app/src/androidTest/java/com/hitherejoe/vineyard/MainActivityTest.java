package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.hitherejoe.vineyard.util.CustomMatchers.withItemText;
import static com.hitherejoe.vineyard.util.EspressoTestMatchers.withDrawable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<MainActivity>(MainActivity.class, false, false);

    @Rule
    public final TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void testAllCategoriesShown() {
        stubVideoFeedData();

        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));

        String[] categories = getCategoriesArray();
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Popular");
        categoryList.add("Editors Picks");
        categoryList.addAll(Arrays.asList(categories));
        categoryList.add("Options");
        for (int i = 0; i < categoryList.size(); i++) {
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
            onView(withItemText(categoryList.get(i), R.id.browse_headers))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testErrorFragmentDisplayed() {
        //TODO: When implemented
    }

    @Test
    public void testCategoryRemovedIfErrorWhenLoading() {
        //TODO: When implemented
    }

    @Test
    public void testCategoryTitlesDisplay() throws InterruptedException {

        stubVideoFeedData();

        main.launchActivity(null);
        onView(withId(R.id.main_browse_fragment))
                .check(matches(isDisplayed()));

        String[] categories = getCategoriesArray();
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Popular");
        categoryList.add("Editors Picks");
        categoryList.addAll(Arrays.asList(categories));
        categoryList.add("Options");
        for (int i = 0; i < categoryList.size(); i++) {
            if (i > 0) {
                onView(withId(R.id.browse_headers))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));
            }
            onView(withId(R.id.browse_headers))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(i, click()));

            onView(withItemText(categoryList.get(i), R.id.browse_container_dock))
                    .check(matches(isDisplayed()));

            pressBack();
            Thread.sleep(3000);
        }

    }

    @Test
    public void testBadgeDrawableIsDisplayed() {
        stubVideoFeedData();

        main.launchActivity(null);
        onView(withId(R.id.title_badge))
                .check(matches(isDisplayed()));
        onView(withDrawable(R.drawable.banner))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSearchActivityOpens() {
        stubVideoFeedData();

        main.launchActivity(null);
        onView(withId(R.id.title_orb))
                .perform(click());
        onView(withId(R.id.search_fragment))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testPostsDisplayAndAreBrowseeable() {
        stubVideoFeedData();

        main.launchActivity(null);
        String[] categories = InstrumentationRegistry.getContext().getResources().getStringArray(R.array.categories);
        onView(withText(categories[0]))
                .perform(scrollTo(), click());
    }

    @Test
    public void testOptionsDisplayAndAreBrowsable() {
        stubVideoFeedData();

        main.launchActivity(null);
        String settingsText = InstrumentationRegistry.getContext().getResources().getString(R.string.header_text_settings);
        onView(withText(settingsText))
                .perform(scrollTo(), click());
        // check items displayed
    }

    @Test
    public void testSettingsGuidedStepOpens() {
        stubVideoFeedData();

        main.launchActivity(null);
        String settingsText = InstrumentationRegistry.getContext().getResources().getString(R.string.header_text_settings);
        onView(withText(settingsText))
                .perform(scrollTo(), click());
        // onView(withId(R.id.recycler_view))
        //       .perform(
        //             RecyclerViewActions.actionOnItemAtPosition(27, click()));
        // click on auto-loop and check it launches
    }

    @Test
    public void testPlaybackActivityOpens() {
        stubVideoFeedData();

        main.launchActivity(null);
        String[] categories = InstrumentationRegistry.getContext().getResources().getStringArray(R.array.categories);
        onView(withText(categories[0]))
                .perform(scrollTo(), click());
    }

    @Test
    public void testBackgroundChangesOnItemSelected() {
        //TODO: When implemented
    }

    @Test
    public void testLoadingIndicatorIsShown() {
        //TODO: When implemented
    }

    private String[] getCategoriesArray() {
        String[] categories = InstrumentationRegistry.getTargetContext().getResources().getStringArray(R.array.categories);
        int categoriesSize = categories.length;
        //categories[categoriesSize + 1] = InstrumentationRegistry.getTargetContext().getResources().getString(R.string.header_text_settings);
        return categories;
    }

    private void stubVideoFeedData() {
        List<Post> mockPosts = TestDataFactory.createMockListOfPosts(17);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;

        when(component.getMockDataManager().getPopularPosts(anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        List<Post> mockTagPosts = TestDataFactory.createMockListOfPosts(17);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = mockTagPosts;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        List<Post> mockEditorsPosts = TestDataFactory.createMockListOfPosts(17);
        VineyardService.PostResponse postEditosResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data editorsData = new VineyardService.PostResponse.Data();
        editorsData.records = mockEditorsPosts;
        postEditosResponse.data = editorsData;

        when(component.getMockDataManager().getEditorsPicksPosts(anyString(), anyString()))
                .thenReturn(Observable.just(postEditosResponse));
    }

}