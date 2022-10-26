package com.example.mailbox.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthRetrofitClient {

    private Retrofit retrofit;
    private static final String BASE_URL = "https://api-mailbox.herokuapp.com/";
    private static AuthRetrofitClient mInstance;


    public AuthRetrofitClient() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
    }

    public static synchronized AuthRetrofitClient getInstance() {
        if (mInstance == null) {
            mInstance = new AuthRetrofitClient();
        }
        return mInstance;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}
