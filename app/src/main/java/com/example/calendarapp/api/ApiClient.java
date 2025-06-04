package com.example.calendarapp.api;

import android.util.Log;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class   ApiClient {
    // SỬA: Đổi từ 10.0.2.15 thành 10.0.2.2
    private static final String BASE_URL = "http://10.0.2.2/schedule_api/";
    private static Retrofit retrofit = null;
    private static final String TAG = "ApiClient";

    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Khởi tạo API Client với URL: " + BASE_URL);

            // Tạo logging interceptor để xem log request/response
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}