package com.hitherejoe.vineyard;


import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.View;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PostGridActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<PostGridActivity> main =
            new ActivityTestRule<>(PostGridActivity.class, false, false);

    @Rule
    public final TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void listOfUserPostsShowsAndIsScrollable() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(20);
        Collections.sort(postList);
        stubPostListUserDate(postList);

        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, PostGridActivity.TYPE_USER, "123");
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));

        checkPostsDisplayOnRecyclerView(postList);
    }


    @Test
    public void listOfTagPostsShowsAndIsScrollable() {
        List<Post> tagList = TestDataFactory.createMockListOfPosts(20);
        Collections.sort(tagList);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));

        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, PostGridActivity.TYPE_TAG, "cat");
        main.launchActivity(intent);

        onView(withText("#cat"))
                .check(matches(isDisplayed()));

        checkPostsDisplayOnRecyclerView(tagList);
    }

    @Test
    public void errorFragmentNotDisplayed() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(20);
        Collections.sort(postList);
        stubPostListUserDate(postList);

        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, PostGridActivity.TYPE_USER, "123");
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));

        String errorTitle = context.getString(R.string.text_error_oops_title);
        String errorMessage = context.getString(R.string.text_error_oops_message);
        String dismissText = context.getString(R.string.text_error_dismiss);

        onView(withText(errorTitle))
                .check(doesNotExist());
        onView(withText(errorMessage))
                .check(doesNotExist());
        onView(withText(dismissText))
                .check(doesNotExist());
    }

    private void stubPostListUserDate(List<Post> postList) {
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = postList;
        postResponse.data = data;

        when(component.getMockDataManager().getPostsByUser(anyString(), eq("1"), anyString()))
                .thenReturn(Observable.just(postResponse));
    }

    @Test
    public void errorFragmentDisplayed() {
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, null, null);
        main.launchActivity(intent);

        String errorTitle = context.getString(R.string.text_error_oops_title);
        String errorMessage = context.getString(R.string.text_error_oops_message);
        String dismissText = context.getString(R.string.text_error_dismiss);

        onView(withText(errorTitle))
                .check(matches(isDisplayed()));
        onView(withText(errorMessage))
                .check(matches(isDisplayed()));
        onView(withText(dismissText))
                .check(matches(isDisplayed()));
    }

    //TODO: Test dismiss button functionality, this will have to be done from the search activity
    // as we'll need to check if the activity finishes and returns to the previous screen

    /**
     * This method checks that the given list of posts display within the VerticalGridView. At the time
     * of writing this, RecyclerViewActions is really buggy with grid based recycler views - so this
     * method traverses through the rows of the grid (starting at left-to-right, then right-to-left
     * and vice versa). This isn't ideal, but currently proper testing doesn't seem supported.
     *
     * @param postsToCheck posts to be checked
     */
    private void checkPostsDisplayOnRecyclerView(List<Post> postsToCheck) {
        int columnCount = 5;
        int size = postsToCheck.size();

        for (int i = 0; i < size; i++) {
            // The first item starts as selected, clicking on this would open the Playback Activity
            checkItemAtPosition(i, postsToCheck.get(i));

            // If we get to the end of the current row then we need to go down to the next one
            if (((i + 1) % columnCount) == 0) {
                int nextRowStart = i + columnCount;
                int nextRowEnd = nextRowStart - columnCount + 1;
                for (int n = nextRowStart; n >= nextRowEnd; n--) {
                    checkItemAtPosition(n, postsToCheck.get(n));
                }
                // Set i to the start of the row beneath the one we've just checked
                i = i + columnCount;
            }
        }
    }

    private void checkItemAtPosition(int position, Post post) {
        // VerticalGridFragment->BaseGridView->RecyclerView means we can use RecyclerViewActions! :D
        if (position > 0) {
            onView(withId(R.id.browse_grid))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(position, click()));
        }
        onView(withItemText(post.description)).check(matches(isDisplayed()));
        onView(withItemText(post.username)).check(matches(isDisplayed()));
    }

    private Matcher<View> withItemText(final String itemText) {
        checkArgument(!TextUtils.isEmpty(itemText), "itemText cannot be null or empty");
        return new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View item) {
                return allOf(isDescendantOfA(withId(R.id.browse_grid)),
                        withText(itemText)).matches(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is isDescendantOfA RecyclerView with text " + itemText);
            }
        };
    }

}