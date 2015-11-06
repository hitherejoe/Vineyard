package com.hitherejoe.vineyard.util;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MockModelsUtil {

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
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
        user.code = "code";
        user.success = true;
        user.error = "";
        User.Data data = new User.Data();
        data.username = "hitherejoe";
        data.following = new Random().nextInt(2000);
        data.followerCount = new Random().nextInt(2000);
        data.verified = 0;
        data.description = generateRandomString();
        data.avatarUrl = generateRandomString();
        data.twitterId = new Random().nextInt(2000);
        data.userId = generateRandomString();
        data.twitterConnected = 0;
        data.likeCount = new Random().nextInt(2000);
        data.facebookConnected = 0;
        data.postCount = new Random().nextInt(2000);
        data.phoneNumber = generateRandomString();
        data.location = generateRandomString();
        data.followingCount = new Random().nextInt(2000);
        data.email = generateRandomString();
        user.data = data;
        return user;
    }

    public static Post createMockPost() {
        Post post = new Post();
        post.avatarUrl = generateRandomString();
        post.created = new Date().toString();
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

}