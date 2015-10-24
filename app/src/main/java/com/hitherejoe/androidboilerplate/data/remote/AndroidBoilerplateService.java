package com.hitherejoe.androidboilerplate.data.remote;

import com.hitherejoe.androidboilerplate.data.model.Authentication;
import com.hitherejoe.androidboilerplate.data.model.User;

import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;

public interface AndroidBoilerplateService {

    String ENDPOINT = "https://api.vineapp.com/";

    @FormUrlEncoded
    @POST("users/authenticate")
    Observable<Authentication> getAccessToken(@Field("username") String username, @Field("password") String password);
}
