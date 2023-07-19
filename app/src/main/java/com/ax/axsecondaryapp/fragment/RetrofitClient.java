package com.ax.axsecondaryapp.fragment;


import com.ax.axsecondaryapp.api.Api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import static com.whackyard.myapplication.app.AppConstants.BASE_URL;

public class RetrofitClient {
    private static RetrofitClient retrofitClient;
    private static Retrofit retrofit;
    private String token = "957362|BCjqofWbk4dC4zM6nqmIwJZH5fFygYjBRUCfFq1x";

    private RetrofitClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://secondary.axcrm.ae/api/")
//                .baseUrl("https://axc.frazshabbir.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    }

    public static synchronized RetrofitClient getInstance() {
        if (retrofitClient == null) {
            retrofitClient = new RetrofitClient();
        }
        return retrofitClient;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}
