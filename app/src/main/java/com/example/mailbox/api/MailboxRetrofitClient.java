package com.example.mailbox.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MailboxRetrofitClient {

    private Retrofit retrofit;
    private static final String BASE_URL = "https://api-mailbox.herokuapp.com/";
    private static MailboxRetrofitClient mInstance;

    public MailboxRetrofitClient(String jwtToken) {

        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("Authorization", jwtToken).build();
                return chain.proceed(newRequest);
            }
        };

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();


        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
    }

    public static synchronized MailboxRetrofitClient getInstance(String token) {
        if (mInstance == null) {
            mInstance = new MailboxRetrofitClient(token);
        }
        return mInstance;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}
