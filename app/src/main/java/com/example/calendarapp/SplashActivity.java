package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.login.LoginActivity;
import com.example.calendarapp.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIMEOUT = 1000; // 1 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Kiểm tra trạng thái đăng nhập
                SessionManager sessionManager = new SessionManager(SplashActivity.this);

                if (sessionManager.isLoggedIn()) {
                    // Nếu đã đăng nhập, chuyển đến MainActivity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Nếu chưa đăng nhập, chuyển đến LoginActivity
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}