package com.example.calendarapp.api;

import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.LoginRequest;
import com.example.calendarapp.models.RegisterRequest;
import com.example.calendarapp.models.SocialLoginRequest;
import com.example.calendarapp.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("login.php")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);

    @POST("register.php")
    Call<ApiResponse<User>> register(@Body RegisterRequest registerRequest);

    @POST("social_login.php")
    Call<ApiResponse<User>> socialLogin(@Body SocialLoginRequest socialLoginRequest);


}