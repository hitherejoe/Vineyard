package com.hitherejoe.androidboilerplate.data.model;

public class Authentication {
    public String code;
    public Data data;
    public boolean success;
    public String error;

    public static class Data {
        public String username;
        public String userId;
        public String key;
    }

}