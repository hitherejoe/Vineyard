package com.hitherejoe.vineyard.data.remote;

import com.hitherejoe.vineyard.data.model.Authentication;
import com.hitherejoe.vineyard.data.model.Post;
import com.hitherejoe.vineyard.data.model.Tag;
import com.hitherejoe.vineyard.data.model.User;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
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

    @GET("timelines/users/{userid}")
    Observable<VineyardService.PostResponse> getUserTimeline(@Path("userid") String userId, @Query("page") String page, @Query("anchorStr") String anchor);

    @GET("timelines/popular")
    Observable<PostResponse> getPopularPosts(@Query("page") String page, @Query("anchorStr") String anchor);

    @GET("timelines/popular")
    Call<PostResponse> getPopularPosts();

    @GET("timelines/promoted")
    Observable<PostResponse> getEditorsPicksPosts(@Query("page") String page, @Query("anchorStr") String anchor);

    @GET("timelines/tags/{tag}")
    Observable<PostResponse> getPostsByTag(@Path("tag") String tag, @Query("page") String page, @Query("anchorStr") String anchor);

    @GET("search/tags/{tag}")
    Observable<TagResponse> searchByTag(@Path("tag") String tag, @Query("page") String page, @Query("anchorStr") String anchor);

    @GET("search/users/{query}")
    Observable<UserResponse> searchByUser(@Path("query") String tag, @Query("page") String page, @Query("anchorStr") String anchor);

    /********
     * Helper class that sets up a new services
     *******/
    class Creator {
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

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.interceptors().add(interceptor);

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
            public int nextPage;
            public List<Post> records;
        }
    }

    class TagResponse {
        public String code;
        public Data data;

        public static class Data {
            public String anchorStr;
            public List<Tag> records;
        }
    }

    class UserResponse {
        public String code;
        public Data data;

        public static class Data {
            public String anchorStr;
            public List<User> records;
        }
    }

    class KeywordSearchResponse {
        public String tagSearchAnchor;
        public String userSearchAnchor;
        public ArrayList<Object> list;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            KeywordSearchResponse that = (KeywordSearchResponse) o;

            if (tagSearchAnchor != null
                    ? !tagSearchAnchor.equals(that.tagSearchAnchor)
                    : that.tagSearchAnchor != null)
                return false;
            if (userSearchAnchor != null
                    ? !userSearchAnchor.equals(that.userSearchAnchor)
                    : that.userSearchAnchor != null)
                return false;
            return !(list != null ? !list.equals(that.list) : that.list != null);

        }

        @Override
        public int hashCode() {
            int result = tagSearchAnchor != null ? tagSearchAnchor.hashCode() : 0;
            result = 31 * result + (userSearchAnchor != null ? userSearchAnchor.hashCode() : 0);
            result = 31 * result + (list != null ? list.hashCode() : 0);
            return result;
        }
    }

}
