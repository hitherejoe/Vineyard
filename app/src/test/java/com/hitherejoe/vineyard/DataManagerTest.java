package com.hitherejoe.vineyard;


import com.hitherejoe.vineyard.data.DataManager;
import com.hitherejoe.vineyard.data.local.PreferencesHelper;
import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.hitherejoe.vineyard.data.remote.VineyardService;
import com.hitherejoe.vineyard.test.common.TestDataFactory;
import com.hitherejoe.vineyard.util.MockModelsUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    @Mock PreferencesHelper mMockPreferencesHelper;
    @Mock VineyardService mMockVineyardService;
    private DataManager mDataManager;

    @Before
    public void setUp() {
        mDataManager = new DataManager(mMockPreferencesHelper, mMockVineyardService);
    }

    @Test
    public void shouldGetAccessToken() throws Exception {
        String username = "hitherejoe";
        String password = "password";

        Authentication mockAuthentication = MockModelsUtil.createMockSuccessAuthentication();
        when(mMockVineyardService.getAccessToken(eq(username), eq(password)))
                .thenReturn(Observable.just(mockAuthentication));

        TestSubscriber<Authentication> result = new TestSubscriber<>();
        mDataManager.getAccessToken(username, password).subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockAuthentication);
        verify(mMockPreferencesHelper).putAccessToken(mockAuthentication.data.key);
    }

    @Test
    public void shouldFailGetAccessToken() throws Exception {
        String username = "hitherejoe";
        String password = "password";

        Authentication mockAuthentication = MockModelsUtil.createMockErrorAuthentication();
        when(mMockVineyardService.getAccessToken(eq(username), eq(password)))
                .thenReturn(Observable.just(mockAuthentication));

        TestSubscriber<Authentication> result = new TestSubscriber<>();
        mDataManager.getAccessToken(username, password).subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockAuthentication);
        verify(mMockPreferencesHelper, never()).getAccessToken();
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
        when(mMockVineyardService.getUser(mockUser.userId))
                .thenReturn(Observable.just(mockUser));

        TestSubscriber<User> result = new TestSubscriber<>();
        mDataManager.getUser(mockUser.userId).subscribe(result);
        result.assertNoErrors();
        result.assertValue(mockUser);
    }

    @Test
    public void shouldGetPopularPosts() throws Exception {
        String page = "1";
        String anchor = "anchor";

        List<Post> mockPostLists = MockModelsUtil.createMockListOfPosts(20);
        VineyardService.PostResponse popularResponse = new VineyardService.PostResponse();
        popularResponse.data = new VineyardService.PostResponse.Data();
        popularResponse.data.records = mockPostLists;
        when(mMockVineyardService.getPopularPosts(eq(page), eq(anchor)))
                .thenReturn(Observable.just(popularResponse));

        TestSubscriber<VineyardService.PostResponse> result = new TestSubscriber<>();
        mDataManager.getPopularPosts(page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(popularResponse);
    }

    @Test
    public void shouldGetEditorsPicksPosts() throws Exception {
        String page = "1";
        String anchor = "anchor";

        List<Post> mockPostLists = MockModelsUtil.createMockListOfPosts(20);
        VineyardService.PostResponse editorsPicksResponse = new VineyardService.PostResponse();
        editorsPicksResponse.data = new VineyardService.PostResponse.Data();
        editorsPicksResponse.data.records = mockPostLists;
        when(mMockVineyardService.getEditorsPicksPosts(eq(page), eq(anchor)))
                .thenReturn(Observable.just(editorsPicksResponse));

        TestSubscriber<VineyardService.PostResponse> result = new TestSubscriber<>();
        mDataManager.getEditorsPicksPosts(page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(editorsPicksResponse);
    }

    @Test
    public void shouldGetPostsByTag() throws Exception {
        String tag = "tag";
        String page = "1";
        String anchor = "anchor";

        List<Post> mockPostLists = MockModelsUtil.createMockListOfPosts(20);
        VineyardService.PostResponse editorsPicksResponse = new VineyardService.PostResponse();
        editorsPicksResponse.data = new VineyardService.PostResponse.Data();
        editorsPicksResponse.data.records = mockPostLists;
        when(mMockVineyardService.getPostsByTag(eq(tag), eq(page), eq(anchor)))
                .thenReturn(Observable.just(editorsPicksResponse));

        TestSubscriber<VineyardService.PostResponse> result = new TestSubscriber<>();
        mDataManager.getPostsByTag(tag, page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(editorsPicksResponse);
    }

    @Test
    public void shouldGetPostsByUser() throws Exception {
        String userId = "userId";
        String page = "1";
        String anchor = "anchor";

        List<Post> mockPostLists = MockModelsUtil.createMockListOfPosts(20);
        VineyardService.PostResponse editorsPicksResponse = new VineyardService.PostResponse();
        editorsPicksResponse.data = new VineyardService.PostResponse.Data();
        editorsPicksResponse.data.records = mockPostLists;
        when(mMockVineyardService.getUserTimeline(eq(userId), eq(page), eq(anchor)))
                .thenReturn(Observable.just(editorsPicksResponse));

        TestSubscriber<VineyardService.PostResponse> result = new TestSubscriber<>();
        mDataManager.getPostsByUser(userId, page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(editorsPicksResponse);
    }

    @Test
    public void shouldGetTagsByKeyword() throws Exception {
        String tag = "tag";
        String page = "1";
        String anchor = "anchor";

        List<Tag> mockPostLists = TestDataFactory.createMockListOfTags(20, tag);
        VineyardService.TagResponse editorsPicksResponse = new VineyardService.TagResponse();
        editorsPicksResponse.data = new VineyardService.TagResponse.Data();
        editorsPicksResponse.data.records = mockPostLists;
        when(mMockVineyardService.searchByTag(eq(tag), eq(page), eq(anchor)))
                .thenReturn(Observable.just(editorsPicksResponse));

        TestSubscriber<VineyardService.TagResponse> result = new TestSubscriber<>();
        mDataManager.searchByTag(tag, page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(editorsPicksResponse);
    }

    @Test
    public void shouldGetUsersByKeyword() throws Exception {
        String userId = "userId";
        String page = "1";
        String anchor = "anchor";

        List<User> mockPostLists = TestDataFactory.createMockListOfUsers(20);
        VineyardService.UserResponse editorsPicksResponse = new VineyardService.UserResponse();
        editorsPicksResponse.data = new VineyardService.UserResponse.Data();
        editorsPicksResponse.data.records = mockPostLists;
        when(mMockVineyardService.searchByUser(eq(userId), eq(page), eq(anchor)))
                .thenReturn(Observable.just(editorsPicksResponse));

        TestSubscriber<VineyardService.UserResponse> result = new TestSubscriber<>();
        mDataManager.searchByUser(userId, page, anchor).subscribe(result);
        result.assertNoErrors();
        result.assertValue(editorsPicksResponse);
    }

    @Test
    public void shouldSearch() throws Exception {
        String tag = "tag";
        String userId = "userId";

        VineyardService.KeywordSearchResponse keywordSearchResponse =
                new VineyardService.KeywordSearchResponse();
        keywordSearchResponse.tagSearchAnchor = tag;
        keywordSearchResponse.userSearchAnchor = userId;
        keywordSearchResponse.list = new ArrayList<>();

        List<User> mockPostUsers = TestDataFactory.createMockListOfUsers(10);
        List<Tag> mockPostTags = TestDataFactory.createMockListOfTags(10, tag);

        keywordSearchResponse.list.addAll(mockPostTags);
        keywordSearchResponse.list.addAll(mockPostUsers);

        Collections.sort(keywordSearchResponse.list, new Comparator<Object>() {
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

        VineyardService.UserResponse usersResponse = new VineyardService.UserResponse();
        usersResponse.data = new VineyardService.UserResponse.Data();
        usersResponse.data.anchorStr = userId;
        usersResponse.data.records = mockPostUsers;
        when(mMockVineyardService.searchByUser(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(usersResponse));

        VineyardService.TagResponse tagsResponse = new VineyardService.TagResponse();
        tagsResponse.data = new VineyardService.TagResponse.Data();
        tagsResponse.data.anchorStr = tag;
        tagsResponse.data.records = mockPostTags;
        when(mMockVineyardService.searchByTag(anyString(), anyString(), anyString()))
                .thenReturn(Observable.just(tagsResponse));

        TestSubscriber<VineyardService.KeywordSearchResponse> result = new TestSubscriber<>();
        mDataManager.search(userId, "0", tag, "0", userId).subscribe(result);
        result.assertNoErrors();
        result.assertValue(keywordSearchResponse);
    }

}
