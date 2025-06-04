package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.utils.SessionManager;

public class MainActivity_Login extends AppCompatActivity {

    private TextView welcomeTextView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Lấy email từ Intent
        String email = getIntent().getStringExtra("EMAIL");

        // Nếu có email, lưu vào SessionManager
        if (email != null) {
            sessionManager.createLoginSession(email, null);
        }

        // Hiển thị thông tin người dùng
        if (sessionManager.isLoggedIn()) {
            welcomeTextView.setText("Chào mừng, " + sessionManager.getUserEmail());
        } else {
            welcomeTextView.setText("Chào mừng, Khách");
        }

        // Chuyển đến MainActivity sau 2 giây
        welcomeTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity_Login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 4000);
    }
}