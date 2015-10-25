package com.hitherejoe.vineyard.data.model;

public class User {
    public String code;
    public Data data;
    public boolean success;
    public String error;

    public static class Data {
        public String username;
        public int following;
        public int followerCount;
        public int verified;
        public String description;
        public String avatarUrl;
        public int twitterId;
        public String userId;
        public int twitterConnected;
        public int likeCount;
        public int facebookConnected;
        public int postCount;
        public String phoneNumber;
        public String location;
        public int followingCount;
        public String email;
        public String error;
    }
}