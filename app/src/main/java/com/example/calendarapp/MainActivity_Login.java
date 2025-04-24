package com.example.calendarapp;



import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity_Login extends AppCompatActivity {

    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Lấy email từ Intent
        String email = getIntent().getStringExtra("EMAIL");
        if (email != null) {
            welcomeTextView.setText("Chào mừng, " + email);
        } else {
            welcomeTextView.setText("Chào mừng, Khách");
        }
        new android.os.Handler().postDelayed(() -> {
            startActivity(new Intent(MainActivity_Login.this, MainActivity.class));
            finish();
        }, 3000);
    }
}
