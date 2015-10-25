package com.hitherejoe.vineyard.data.remote;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import timber.log.Timber;

public class RetrofitHelper {


    OkHttpClient client = new OkHttpClient();


    public VineyardService newAndroidBoilerplateService() {
        client.interceptors().add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                // Do anything with response here

                Timber.e("FUCK " + response.toString());

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
