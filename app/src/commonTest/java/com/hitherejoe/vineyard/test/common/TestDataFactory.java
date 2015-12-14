package com.hitherejoe.vineyard.test.common;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class TestDataFactory {

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    public static int generateRandomNumber() {
        return new Random().nextInt(99999999);
    }

    public static Authentication createMockSuccessAuthentication() {
        Authentication authentication = new Authentication();
        authentication.code = "200";
        authentication.error = "";
        authentication.success = true;
        Authentication.Data data = new Authentication.Data();
        data.key = generateRandomString();
        data.userId = generateRandomString();
        data.username = generateRandomString();
        authentication.data = data;
        return authentication;
    }

    public static Authentication createMockErrorAuthentication() {
        Authentication authentication = new Authentication();
        authentication.code = "400";
        authentication.error = "There was an error";
        authentication.success = false;
        authentication.data = null;
        return authentication;
    }

    public static User createMockUser() {
        User user = new User();
        user.error = "";
        user.username = generateRandomString();
        user.following = new Random().nextInt(2000);
        user.followerCount = new Random().nextInt(2000);
        user.verified = 0;
        user.description = generateRandomString();
        user.avatarUrl = generateRandomString();
        user.twitterId = new Random().nextInt(2000);
        user.userId = generateRandomString();
        user.twitterConnected = 0;
        user.likeCount = new Random().nextInt(2000);
        user.facebookConnected = 0;
        user.postCount = new Random().nextInt(2000);
        user.phoneNumber = generateRandomString();
        user.location = generateRandomString();
        user.followingCount = new Random().nextInt(2000);
        user.email = generateRandomString();
        return user;
    }

    public static Tag createMockTag(String tag) {
        Tag mockTag = new Tag();
        mockTag.tag = tag;
        mockTag.tagId = new Random().nextInt(2000);
        mockTag.postCount = new Random().nextInt(2000);
        return mockTag;
    }

    public static Post createMockPost() {
        Post post = new Post();
        post.avatarUrl = generateRandomString();
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault());
        post.created = dateFormat.format(new Date());
        post.description = generateRandomString();
        post.postId = generateRandomString();
        post.thumbnailUrl = generateRandomString();
        post.username = generateRandomString();
        post.videoUrl = generateRandomString();
        return post;
    }

    public static List<Post> createMockListOfPosts(int count) {
        List<Post> mockPosts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            mockPosts.add(createMockPost());
        }
        return mockPosts;
    }

    public static List<Tag> createMockListOfTags(int count, String tag) {
        List<Tag> mockPosts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            mockPosts.add(createMockTag(tag));
        }
        return mockPosts;
    }

    public static List<User> createMockListOfUsers(int count) {
        List<User> mockPosts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            mockPosts.add(createMockUser());
        }
        return mockPosts;
    }
}
