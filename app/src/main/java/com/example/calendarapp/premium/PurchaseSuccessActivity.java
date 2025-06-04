package com.example.calendarapp.premium;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.MainActivity;
import com.example.calendarapp.R;

public class PurchaseSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_success);

        // Lấy thông tin từ Intent
        String transactionId = getIntent().getStringExtra("TRANSACTION_ID");
        String paymentMethod = getIntent().getStringExtra("PAYMENT_METHOD");

        // Hiển thị thông tin giao dịch


        Button btnContinue = findViewById(R.id.btn_continue);
        btnContinue.setOnClickListener(v -> {
            // Quay về màn hình chính
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Ngăn người dùng quay lại màn hình thanh toán
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
