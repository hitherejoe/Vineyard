package com.hitherejoe.vineyard;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.EditText;

import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.test.common.rules.TestComponentRule;
import com.hitherejoe.vineyard.ui.activity.MainActivity;
import com.hitherejoe.vineyard.ui.activity.SearchActivity;
import com.hitherejoe.vineyard.ui.fragment.SearchFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
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
import static com.hitherejoe.vineyard.util.EspressoTestMatchers.withDrawable;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
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

        SearchFragment.CombinedSearchResponse combinedSearchResponse = new SearchFragment.CombinedSearchResponse();
        combinedSearchResponse.list = new ArrayList<>();
        combinedSearchResponse.tagSearchAnchor = "c";
        combinedSearchResponse.userSearchAnchor = "c";

        when(component.getMockDataManager().search(eq("c"), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(combinedSearchResponse));

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("c"));

        onView(withText(R.string.text_no_results))
                .check(matches(isDisplayed()));
    }

    @Test
    public void queryShowsTagAndUserResults() {
        main.launchActivity(null);

        List<Object> term = stubTagUserAndPostData();

        String by;
        Object object = term.get(0);

        if(object instanceof Tag) {
            by = ((Tag) object).tag;
        } else {
            by = ((User) object).userId;
        }

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
    public void searchResultsDisplayAndAreScrollable() throws InterruptedException {
        main.launchActivity(null);

        List<Object> term = stubTagUserAndPostData();

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        Thread.sleep(2000);

        for (int n = 0; n < term.size(); n++) {
            checkItemAtPosition(n, term.get(n));
        }

        String by;
        Object object = term.get(0);

        if(object instanceof Tag) {
            by = ((Tag) object).tag;
        } else {
            by = ((User) object).userId;
        }

        onView(withText(by))
                .perform(click());
    }

    @Test
    public void postResultsDisplayAndAreScrollable() throws InterruptedException {
        main.launchActivity(null);

        SearchFragment.CombinedSearchResponse combinedSearchResponse = new SearchFragment.CombinedSearchResponse();
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

        combinedSearchResponse.list = list;
        combinedSearchResponse.tagSearchAnchor = "";
        combinedSearchResponse.userSearchAnchor = "";

        String by;
        Object object = list.get(0);

        if(object instanceof Tag) {
            by = ((Tag) object).tag;
        } else {
            by = ((User) object).userId;
        }

        when(component.getMockDataManager().search(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(combinedSearchResponse));

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
            checkItemAtPosition(n, mockPostsTwo.get(n));
        }

        onView(withText(user.username))
                .perform(click());
        for (int n = 0; n < mockPostsOne.size(); n++) {
            checkItemAtPosition(n, mockPostsOne.get(n));
        }

        onView(withText(user.username))
                .perform(click());
    }

    @Test
    public void searchResultOpensVerticalGridActivity() throws InterruptedException {
        main.launchActivity(null);

        List<Object> term = stubTagUserAndPostData();

        onView(withId(R.id.lb_search_text_editor))
                .perform(replaceText("cat"));

        pressImeActionButton();

        pressKey(KeyEvent.KEYCODE_SEARCH);

        Thread.sleep(2000);

        String by;
        Object object = term.get(0);

        if(object instanceof Tag) {
            by = ((Tag) object).tag;
        } else {
            by = ((User) object).username;
        }

        onView(withText(by))
                .perform(click());

        onView(withText(by))
                .perform(click());

        onView(withId(R.id.frame_container_post_grid))
                .check(matches(isDisplayed()));

        pressBack();
    }

    private List<Object> stubTagUserAndPostData() {
        List<Post> mockPosts = TestDataFactory.createMockListOfPosts(10);
        VineyardService.PostResponse postResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data data = new VineyardService.PostResponse.Data();
        data.records = mockPosts;
        postResponse.data = data;

        when(component.getMockDataManager().getPostsByUser(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        when(component.getMockDataManager().getPostsByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(postResponse));

        SearchFragment.CombinedSearchResponse combinedSearchResponse = new SearchFragment.CombinedSearchResponse();
        ArrayList<Object> list = new ArrayList<>();
        List<Tag> tags = TestDataFactory.createMockListOfTags(10);
        list.addAll(tags);
        List<User> users = TestDataFactory.createMockListOfUsers(10);
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

        combinedSearchResponse.list = list;
        combinedSearchResponse.tagSearchAnchor = "";
        combinedSearchResponse.userSearchAnchor = "";

        String by;
        Object object = list.get(0);

        if(object instanceof Tag) {
            by = ((Tag) object).tag;
        } else {
            by = ((User) object).userId;
        }

        when(component.getMockDataManager().search(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(combinedSearchResponse));
        return list;
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

    private VineyardService.PostResponse createMockPostResponse() {
        List<Post> mockTagPosts = TestDataFactory.createMockListOfPosts(17);
        Collections.sort(mockTagPosts);
        VineyardService.PostResponse postTagResponse = new VineyardService.PostResponse();
        VineyardService.PostResponse.Data tagData = new VineyardService.PostResponse.Data();
        tagData.records = mockTagPosts;
        postTagResponse.data = tagData;
        return postTagResponse;
    }

    private void checkItemAtPosition(int position, Object object) throws InterruptedException {

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