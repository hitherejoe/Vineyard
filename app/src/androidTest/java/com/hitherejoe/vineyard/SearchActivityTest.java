package com.hitherejoe.vineyard;


import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.PostGridActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.hitherejoe.vineyard.util.CustomMatchers.withItemText;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<SearchActivity> main =
            new ActivityTestRule<SearchActivity>(SearchActivity.class, false, false);

    @Rule
    public final TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void blankQueryShowsNoResults() {
        main.launchActivity(null);

        onView(withText(R.string.text_search_results))
                .check(doesNotExist());
        onView(withText(R.string.text_no_results))
                .check(doesNotExist());

        onView(withId(R.id.lb_search_text_editor))
                .perform(typeText(" "));

        onView(withText(R.string.text_search_results))
                .check(doesNotExist());
        onView(withText(R.string.text_no_results))
                .check(doesNotExist());

        onView(withId(R.id.lb_search_text_editor))
                .perform(clearText());

        onView(withText(R.string.text_search_results))
                .check(doesNotExist());
        onView(withText(R.string.text_no_results))
                .check(doesNotExist());
    }

    @Test
    public void queryShowsNoTagOrUserResults() {
        main.launchActivity(null);

        VineyardService.KeywordSearchResponse keywordSearchResponse = new VineyardService.KeywordSearchResponse();
        keywordSearchResponse.list = new ArrayList<>();
        keywordSearchResponse.tagSearchAnchor = "c";
        keywordSearchResponse.userSearchAnchor = "c";

        when(component.getMockDataManager().search(eq("c"), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(keywordSearchResponse));

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("c"));

        onView(withText(R.string.text_no_results))
                .check(matches(isDisplayed()));
    }

    @Test
    public void queryShowsTagAndUserResults() {
        main.launchActivity(null);

        ArrayList<Object> objectList = createMockObjectList();
        stubTagUserAndPostData(objectList);

        String by = getSearchByItem(objectList.get(0));

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        onView(withText(R.string.text_no_results))
                .check(doesNotExist());
        onView(withText(R.string.text_search_results))
                .check(matches(isDisplayed()));

        onView(withText("Posts for " + by))
                .check(matches(isDisplayed()));
    }

    @Test
    public void searchResultsDisplayAndAreScrollable() {
        main.launchActivity(null);

        ArrayList<Object> objectList = createMockObjectList();
        stubTagUserAndPostData(objectList);

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        for (int n = 0; n < objectList.size(); n++) {
            checkItemAtPosition(objectList.get(n));
        }
    }

    @Test
    public void postResultsDisplayAndAreScrollable() {
        main.launchActivity(null);

        VineyardService.KeywordSearchResponse keywordSearchResponse = new VineyardService.KeywordSearchResponse();
        ArrayList<Object> list = new ArrayList<>();
        List<Tag> tags = TestDataFactory.createMockListOfTags(1);
        list.addAll(tags);
        List<User> users = TestDataFactory.createMockListOfUsers(1);
        list.addAll(users);
        Collections.sort(list, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                if (lhs instanceof Tag) {
                    Tag tag = (Tag) lhs;
                    if (rhs instanceof Tag) {
                        Tag tagTwo = (Tag) rhs;
                        return (int) (tag.postCount - tagTwo.postCount);
                    } else if (rhs instanceof User) {
                        User user = (User) rhs;
                        return (int) (tag.postCount - user.followerCount);
                    }
                } else if (lhs instanceof User) {
                    User user = (User) lhs;
                    if (rhs instanceof Tag) {
                        Tag tagTwo = (Tag) rhs;
                        return (int) (user.followerCount - tagTwo.postCount);
                    } else if (rhs instanceof User) {
                        User userTwo = (User) rhs;
                        return user.followerCount - userTwo.followerCount;
                    }
                }
                return 0;
            }
        });

        keywordSearchResponse.list = list;
        keywordSearchResponse.tagSearchAnchor = "";
        keywordSearchResponse.userSearchAnchor = "";

        when(component.getMockDataManager().search(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(keywordSearchResponse));

        List<Post> mockPostsOne = TestDataFactory.createMockListOfPosts(5);
        Collections.sort(mockPostsOne);
        VineyardService.PostResponse postResponseOne = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data dataOne = new VineyardService.PostResponse.Data();
        dataOne.records = mockPostsOne;
        postResponseOne.data = dataOne;

        List<Post> mockPostsTwo = TestDataFactory.createMockListOfPosts(5);
        Collections.sort(mockPostsTwo);
        VineyardService.PostResponse postResponseTwo = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data dataTwo = new VineyardService.PostResponse.Data();
        dataTwo.records = mockPostsTwo;
        postResponseTwo.data = dataTwo;

        String tag = tags.get(0).tag;
        User user = users.get(0);
        when(component.getMockDataManager().getPostsByUser(eq(user.userId), anyString(), anyString()))
                .thenReturn(Observable.just(postResponseOne));

        when(component.getMockDataManager().getPostsByTag(eq(tag), anyString(), anyString()))
                .thenReturn(Observable.just(postResponseTwo));

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        onView(withText(tag))
                .perform(click());
        for (int n = 0; n < mockPostsTwo.size(); n++) {
            checkItemAtPosition(mockPostsTwo.get(n));
        }

        onView(withText(user.username))
                .perform(click());
        for (int n = 0; n < mockPostsOne.size(); n++) {
            checkItemAtPosition(mockPostsOne.get(n));
        }

        onView(withText(user.username))
                .perform(click());
    }

    @Test
    public void searchResultOpensVerticalGridActivity() {
        main.launchActivity(null);

        ArrayList<Object> objectList = createMockObjectList();
        stubTagUserAndPostData(objectList);

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        String by = getSearchByItem(objectList.get(0));

        onView(withText(by))
                .perform(click());

        onView(withText(by))
                .perform(click());

        onView(withId(R.id.frame_container_post_grid))
                .check(matches(isDisplayed()));
    }

    private ArrayList<Object> createMockObjectList() {
        ArrayList<Object> objectList = new ArrayList<>();
        objectList.addAll(TestDataFactory.createMockListOfTags(10));
        objectList.addAll(TestDataFactory.createMockListOfUsers(10));
        Collections.sort(objectList, new Comparator<Object>() {
            @Override
            public int compare(Object lhs, Object rhs) {
                if (lhs instanceof Tag) {
                    Tag tag = (Tag) lhs;
                    if (rhs instanceof Tag) {
                        Tag tagTwo = (Tag) rhs;
                        return (int) (tag.postCount - tagTwo.postCount);
                    } else if (rhs instanceof User) {
                        User user = (User) rhs;
                        return (int) (tag.postCount - user.followerCount);
                    }
                } else if (lhs instanceof User) {
                    User user = (User) lhs;
                    if (rhs instanceof Tag) {
                        Tag tagTwo = (Tag) rhs;
                        return (int) (user.followerCount - tagTwo.postCount);
                    } else if (rhs instanceof User) {
                        User userTwo = (User) rhs;
                        return user.followerCount - userTwo.followerCount;
                    }
                }
                return 0;
            }
        });
        return objectList;
    }

    private void stubTagUserAndPostData(ArrayList<Object> list) {
        List<Post> mockPosts = TestDataFactory.createMockListOfPosts(10);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;

        when(component.getMockDataManager().getPostsByUser(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        VineyardService.KeywordSearchResponse keywordSearchResponse = new VineyardService.KeywordSearchResponse();


        keywordSearchResponse.list = list;
        keywordSearchResponse.tagSearchAnchor = "";
        keywordSearchResponse.userSearchAnchor = "";

        when(component.getMockDataManager().search(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(keywordSearchResponse));
    }

    private String getSearchByItem(Object object) {
        if (object instanceof Tag) {
            return ((Tag) object).tag;
        } else {
            return ((User) object).username;
        }
    }

    private void checkItemAtPosition(Object object) {

        String item = null;
        if (object instanceof User) {
            item = ((User) object).username;
        } else if (object instanceof Tag) {
            item = ((Tag) object).tag;
        } else if (object instanceof Post) {
            item = ((Post) object).description;
        }

        onView(withItemText(item, R.id.lb_results_frame)).perform(click());
        onView(withItemText(item, R.id.lb_results_frame)).check(matches(isDisplayed()));
    }

}