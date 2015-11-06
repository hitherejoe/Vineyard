package com.hitherejoe.vineyard.data.remote;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.User;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface VineyardService {

    String ENDPOINT = "https://api.vineapp.com/";

    @FormUrlEncoded
    @POST("users/authenticate")
    Observable<Authentication> getAccessToken(@Field("username") String username, @Field("password") String password);

    @GET("users/me")
    Observable<User> getSignedInUser();

    @GET("users/profiles/{userid}")
    Observable<User> getUser(@Path("userid") String userId);

    @GET("timelines/popular")
    Observable<PostResponse> getPopularPosts(@Query("page") int page, @Query("anchorStr") String anchor);

    @GET("timelines/tags/{tag}")
    Observable<PostResponse> getPostsByTag(@Path("tag") String tag, @Query("page") int page, @Query("anchorStr") String anchor);

    class Instance {
        public static VineyardService newVineyardService() {
            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    // Catch unauthorised error
                    return response;
                }
            });

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(VineyardService.ENDPOINT)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit.create(VineyardService.class);
        }
    }

    class PostResponse {
        public String code;
        public Data data;
        public static class Data {
            public String anchorStr;
            public List<Post> records;
        }
    }
}
