package com.hitherejoe.vineyard;


import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Matchers.anyString;
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
        stubPostGridData();
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, PostGridActivity.TYPE_USER, "123");
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));
        //TODO: Find a proper way to scroll through and check content
    }

    @Test
    public void listOfTagPostsShowsAndIsScrollable() {
        stubPostGridData();
        Context context = InstrumentationRegistry.getTargetContext();
        Intent intent = PostGridActivity.getStartIntent(context, PostGridActivity.TYPE_TAG, "cat");
        main.launchActivity(intent);

        onView(withText("123"))
                .check(matches(isDisplayed()));
        //TODO: Find a proper way to scroll through and check content
    }

    @Test
    public void errorFragmentNotDisplayed() {
        stubPostGridData();
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

    private void stubPostGridData() {
        List<Post> postList = TestDataFactory.createMockListOfPosts(17);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = postList;
        postResponse.data = data;

        when(component.getMockDataManager().getPostsByUser(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        List<Post> tagList = TestDataFactory.createMockListOfPosts(17);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = tagList;
        postTagResponse.data = tagData;

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postTagResponse));
    }

}