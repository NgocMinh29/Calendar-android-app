package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

public class SocialLoginRequest {
    private String provider;

    @SerializedName("social_id")
    private String socialId;

    private String email;
    private String name;

    public SocialLoginRequest(String provider, String socialId, String email, String name) {
        this.provider = provider;
        this.socialId = socialId;
        this.email = email;
        this.name = name;
    }
}