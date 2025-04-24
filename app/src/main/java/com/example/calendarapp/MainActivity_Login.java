package com.example.calendarapp;



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
    }
}
