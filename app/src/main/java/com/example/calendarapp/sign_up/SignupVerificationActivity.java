package com.example.calendarapp.sign_up;

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

public class SignupVerificationActivity extends AppCompatActivity {

    private EditText digit1, digit2, digit3, digit4, digit5, digit6;
    private Button verifyButton;
    private ImageButton backButton;
    private TextView resendCodeTextView, emailTextView;
    private String email;
    private CountDownTimer resendTimer;
    private boolean canResend = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_verification);

        // Lấy email từ Intent
        email = getIntent().getStringExtra("EMAIL");

        // Ánh xạ các thành phần giao diện
        digit1 = findViewById(R.id.digit1);
        digit2 = findViewById(R.id.digit2);
        digit3 = findViewById(R.id.digit3);
        digit4 = findViewById(R.id.digit4);
        digit5 = findViewById(R.id.digit5);
        digit6 = findViewById(R.id.digit6);
        verifyButton = findViewById(R.id.verifyButton);
        backButton = findViewById(R.id.backButton);
        resendCodeTextView = findViewById(R.id.resendCodeTextView);
        emailTextView = findViewById(R.id.emailTextView);

        // Hiển thị email
        emailTextView.setText(email);

        // Thiết lập chuyển focus tự động giữa các ô nhập mã
        setupDigitInputs();

        // Xử lý sự kiện khi nhấn nút xác minh
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateVerificationCode()) {
                    // Chuyển đến màn hình thành công
                    Intent intent = new Intent(SignupVerificationActivity.this, SignupSuccessActivity.class);
                    startActivity(intent);
                    finish();
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
        digit1.addTextChangedListener(createTextWatcher(digit1, null, digit2));
        digit2.addTextChangedListener(createTextWatcher(digit2, digit1, digit3));
        digit3.addTextChangedListener(createTextWatcher(digit3, digit2, digit4));
        digit4.addTextChangedListener(createTextWatcher(digit4, digit3, digit5));
        digit5.addTextChangedListener(createTextWatcher(digit5, digit4, digit6));
        digit6.addTextChangedListener(createTextWatcher(digit6, digit5, null));
    }

    // Tạo TextWatcher để xử lý chuyển focus
    private TextWatcher createTextWatcher(final EditText currentDigit, final EditText previousDigit, final EditText nextDigit) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && nextDigit != null) {
                    nextDigit.requestFocus();
                } else if (s.length() == 0 && previousDigit != null) {
                    previousDigit.requestFocus();
                }

                // Kiểm tra xem tất cả các ô đã được điền chưa
                checkAllDigitsFilled();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    // Kiểm tra xem tất cả các ô đã được điền chưa
    private void checkAllDigitsFilled() {
        if (!digit1.getText().toString().isEmpty() &&
                !digit2.getText().toString().isEmpty() &&
                !digit3.getText().toString().isEmpty() &&
                !digit4.getText().toString().isEmpty() &&
                !digit5.getText().toString().isEmpty() &&
                !digit6.getText().toString().isEmpty()) {

            // Kích hoạt nút xác minh
            verifyButton.setBackgroundResource(R.drawable.button_purple_background);
        } else {
            verifyButton.setBackgroundResource(R.drawable.button_gray_background);
        }
    }

    // Kiểm tra mã xác nhận
    private boolean validateVerificationCode() {
        String enteredCode = digit1.getText().toString() +
                digit2.getText().toString() +
                digit3.getText().toString() +
                digit4.getText().toString() +
                digit5.getText().toString() +
                digit6.getText().toString();

        if (enteredCode.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã xác nhận", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra mã xác nhận có đúng không
        String savedCode = getSharedPreferences("SignupVerification", MODE_PRIVATE)
                .getString("verification_code", "");

        if (!enteredCode.equals(savedCode) && !enteredCode.equals("123456")) { // 123456 là mã mặc định để test
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
        getSharedPreferences("SignupVerification", MODE_PRIVATE)
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
        digit5.setText("");
        digit6.setText("");
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

    // Tạo mã xác nhận ngẫu nhiên 6 chữ số
    private String generateRandomCode() {
        int code = (int) (Math.random() * 900000) + 100000; // Tạo số ngẫu nhiên từ 100000 đến 999999
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
