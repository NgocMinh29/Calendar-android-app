package com.example.calendarapp.forgot_password;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;
import com.example.calendarapp.login.LoginActivity;

public class ResetSuccessActivity extends AppCompatActivity {

    private Button returnToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_success);

        returnToLoginButton = findViewById(R.id.returnToLoginButton);

        returnToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay lại màn hình đăng nhập
                Intent intent = new Intent(ResetSuccessActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    // Ghi đè phương thức onBackPressed để ngăn người dùng quay lại màn hình trước đó
    @Override
    public void onBackPressed() {
        // Chuyển đến màn hình đăng nhập thay vì quay lại
        Intent intent = new Intent(ResetSuccessActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
