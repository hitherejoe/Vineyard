package com.hitherejoe.androidboilerplate.util;

import com.hitherejoe.androidboilerplate.data.model.Authentication;

import java.util.UUID;

public class MockModelsUtil {

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    public static Authentication createMockAuthentication() {
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

}