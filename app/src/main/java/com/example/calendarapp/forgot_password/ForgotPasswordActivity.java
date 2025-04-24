package com.example.calendarapp.forgot_password;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.example.calendarapp.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button nextButton;
    private ImageButton closeButton;
    private TextView termsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ các thành phần giao diện
        emailEditText = findViewById(R.id.emailEditText);
        nextButton = findViewById(R.id.nextButton);
        closeButton = findViewById(R.id.closeButton);
        termsTextView = findViewById(R.id.termsTextView);

        // Thiết lập văn bản có thể nhấp với các liên kết
        setupClickableTermsText();

        // Xử lý sự kiện khi nhấn nút Next
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();

                if (validateEmail(email)) {
                    // Gửi mã xác nhận đến email
                    sendVerificationCode(email);

                    // Chuyển đến màn hình nhập mã xác nhận
                    Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                }
            }
        });

        // Xử lý sự kiện khi nhấn nút đóng
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng màn hình hiện tại và quay lại màn hình trước đó
            }
        });
    }

    // Thiết lập văn bản có thể nhấp với các liên kết
    private void setupClickableTermsText() {
        String fullText = "By tapping next you're finding an account and you agree to the Account terms and acknowledge Privacy Policy.";
        SpannableString spannableString = new SpannableString(fullText);

        // Tạo ClickableSpan cho "Account terms"
        ClickableSpan accountTermsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(ForgotPasswordActivity.this, "Account Terms clicked", Toast.LENGTH_SHORT).show();
                // Trong thực tế, bạn sẽ mở một Activity hoặc WebView để hiển thị điều khoản
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#7E57C2"));
                ds.setUnderlineText(false);
            }
        };

        // Tạo ClickableSpan cho "Privacy Policy"
        ClickableSpan privacyPolicyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(ForgotPasswordActivity.this, "Privacy Policy clicked", Toast.LENGTH_SHORT).show();
                // Trong thực tế, bạn sẽ mở một Activity hoặc WebView để hiển thị chính sách
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#7E57C2"));
                ds.setUnderlineText(false);
            }
        };

        // Tìm vị trí của "Account terms" và "Privacy Policy" trong văn bản
        int accountTermsStart = fullText.indexOf("Account terms");
        int accountTermsEnd = accountTermsStart + "Account terms".length();
        int privacyPolicyStart = fullText.indexOf("Privacy Policy");
        int privacyPolicyEnd = privacyPolicyStart + "Privacy Policy".length();

        // Áp dụng ClickableSpan vào văn bản
        spannableString.setSpan(accountTermsClickableSpan, accountTermsStart, accountTermsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyPolicyClickableSpan, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Thiết lập văn bản cho TextView
        termsTextView.setText(spannableString);
        termsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        termsTextView.setHighlightColor(Color.TRANSPARENT); // Loại bỏ màu highlight khi nhấp vào
    }

    // Kiểm tra tính hợp lệ của email hoặc số điện thoại
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email hoặc số điện thoại", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    // Phương thức gửi mã xác nhận đến email
    private void sendVerificationCode(String email) {
        // Trong thực tế, bạn sẽ gửi yêu cầu đến server để gửi email chứa mã xác nhận
        // Đây là mã giả để mô phỏng quá trình
        Toast.makeText(this, "Đã gửi mã xác nhận đến " + email, Toast.LENGTH_LONG).show();

        // Lưu mã xác nhận vào SharedPreferences để kiểm tra sau này
        // Trong ứng dụng thực tế, mã này sẽ được tạo và xác thực bởi server
        String verificationCode = generateRandomCode();
        getSharedPreferences("ForgotPassword", MODE_PRIVATE)
                .edit()
                .putString("verification_code", verificationCode)
                .putString("email", email)
                .apply();
    }

    // Tạo mã xác nhận ngẫu nhiên 4 chữ số
    private String generateRandomCode() {
        int code = (int) (Math.random() * 9000) + 1000; // Tạo số ngẫu nhiên từ 1000 đến 9999
        return String.valueOf(code);
    }
}
