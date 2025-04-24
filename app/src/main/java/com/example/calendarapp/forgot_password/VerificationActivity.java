package com.example.calendarapp.forgot_password;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;

public class VerificationActivity extends AppCompatActivity {

    private EditText digit1, digit2, digit3, digit4;
    private Button verifyButton;
    private ImageButton backButton;
    private TextView resendCodeTextView, emailAddressTextView;
    private String email;
    private CountDownTimer resendTimer;
    private boolean canResend = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // Lấy email từ Intent
        email = getIntent().getStringExtra("EMAIL");

        // Ánh xạ các thành phần giao diện
        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);
        verifyButton = findViewById(R.id.verifyButton);
        backButton = findViewById(R.id.backButton);
        resendCodeTextView = findViewById(R.id.resendCodeTextView);
        emailAddressTextView = findViewById(R.id.emailAddressTextView);

        // Hiển thị email
        emailAddressTextView.setText(email);

        // Thiết lập chuyển focus tự động giữa các ô nhập mã
        setupDigitInputs();

        // Xử lý sự kiện khi nhấn nút xác minh
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateVerificationCode()) {
                    // Chuyển đến màn hình đặt mật khẩu mới
                    Intent intent = new Intent(VerificationActivity.this, SetNewPasswordActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                }
            }
        });

        // Xử lý sự kiện khi nhấn nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Quay lại màn hình trước đó
            }
        });

        // Xử lý sự kiện khi nhấn gửi lại mã
        resendCodeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canResend) {
                    resendVerificationCode();
                    startResendTimer();
                }
            }
        });

        // Tự động focus vào ô đầu tiên
        digit1.requestFocus();
    }

    // Thiết lập chuyển focus tự động giữa các ô nhập mã
    private void setupDigitInputs() {
        digit1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    digit2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        digit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    digit3.requestFocus();
                } else if (s.length() == 0) {
                    digit1.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        digit3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    digit4.requestFocus();
                } else if (s.length() == 0) {
                    digit2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        digit4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) {
                    // Tự động xác minh khi đã nhập đủ 4 chữ số
                    verifyButton.performClick();
                } else if (s.length() == 0) {
                    digit3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Kiểm tra mã xác nhận
    private boolean validateVerificationCode() {
        String enteredCode = digit1.getText().toString() +
                digit2.getText().toString() +
                digit3.getText().toString() +
                digit4.getText().toString();

        if (enteredCode.length() < 4) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã xác nhận", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra mã xác nhận có đúng không
        String savedCode = getSharedPreferences("ForgotPassword", MODE_PRIVATE)
                .getString("verification_code", "");

        if (!enteredCode.equals(savedCode) && !enteredCode.equals("1234")) { // 1234 là mã mặc định để test
            Toast.makeText(this, "Mã xác nhận không đúng", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Gửi lại mã xác nhận
    private void resendVerificationCode() {
        // Tạo mã xác nhận mới
        String newVerificationCode = generateRandomCode();

        // Lưu mã mới vào SharedPreferences
        getSharedPreferences("ForgotPassword", MODE_PRIVATE)
                .edit()
                .putString("verification_code", newVerificationCode)
                .apply();

        // Thông báo cho người dùng
        Toast.makeText(this, "Đã gửi lại mã xác nhận đến " + email, Toast.LENGTH_LONG).show();

        // Xóa các ô nhập mã hiện tại
        digit1.setText("");
        digit2.setText("");
        digit3.setText("");
        digit4.setText("");
        digit1.requestFocus();
    }

    // Bắt đầu đếm ngược thời gian gửi lại mã
    private void startResendTimer() {
        canResend = false;
        resendCodeTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                resendCodeTextView.setText("Send again in " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                canResend = true;
                resendCodeTextView.setText("Send again");
                resendCodeTextView.setTextColor(getResources().getColor(android.R.color.holo_purple));
            }
        }.start();
    }

    // Tạo mã xác nhận ngẫu nhiên 4 chữ số
    private String generateRandomCode() {
        int code = (int) (Math.random() * 9000) + 1000; // Tạo số ngẫu nhiên từ 1000 đến 9999
        return String.valueOf(code);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
