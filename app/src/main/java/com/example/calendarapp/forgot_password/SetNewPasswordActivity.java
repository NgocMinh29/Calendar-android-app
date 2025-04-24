package com.example.calendarapp.forgot_password;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;

import java.util.regex.Pattern;

public class SetNewPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private ImageView toggleNewPasswordVisibility, toggleConfirmPasswordVisibility;
    private Button confirmButton;
    private ImageButton backButton;
    private String email;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    // Mẫu kiểm tra mật khẩu mạnh
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_new_password);

        // Lấy email từ Intent
        email = getIntent().getStringExtra("EMAIL");

        // Ánh xạ các thành phần giao diện
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        toggleNewPasswordVisibility = findViewById(R.id.toggleNewPasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        confirmButton = findViewById(R.id.confirmButton);
        backButton = findViewById(R.id.backButton);

        // Xử lý sự kiện khi nhấn nút xác nhận
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validatePasswords()) {
                    resetPassword();
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

        // Xử lý sự kiện khi nhấn nút hiển thị/ẩn mật khẩu mới
        toggleNewPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNewPasswordVisibility();
            }
        });

        // Xử lý sự kiện khi nhấn nút hiển thị/ẩn xác nhận mật khẩu
        toggleConfirmPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleConfirmPasswordVisibility();
            }
        });
    }

    // Kiểm tra tính hợp lệ của mật khẩu
    private boolean validatePasswords() {
        String newPassword = newPasswordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 8) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 8 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Kiểm tra mật khẩu mạnh (có thể bỏ qua nếu không cần)
        if (!isStrongPassword(newPassword)) {
            Toast.makeText(this, "Mật khẩu phải chứa ít nhất một chữ hoa, một chữ thường, một số và một ký tự đặc biệt", Toast.LENGTH_LONG).show();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!confirmPassword.equals(newPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Kiểm tra mật khẩu mạnh
    private boolean isStrongPassword(String password) {
        // Bỏ qua kiểm tra mật khẩu mạnh trong môi trường demo
        // Trong ứng dụng thực tế, bạn nên bật tính năng này
        // return PASSWORD_PATTERN.matcher(password).matches();
        return true;
    }

    // Đặt lại mật khẩu
    private void resetPassword() {
        String newPassword = newPasswordEditText.getText().toString();

        // Trong thực tế, bạn sẽ gửi yêu cầu đến server để cập nhật mật khẩu
        // Đây là mã giả để mô phỏng quá trình

        // Lưu mật khẩu mới vào SharedPreferences (chỉ để demo)
        // Trong ứng dụng thực tế, mật khẩu sẽ được lưu trữ an toàn trên server
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putString(email + "_password", newPassword)
                .apply();

        // Chuyển đến màn hình thành công
        Intent intent = new Intent(SetNewPasswordActivity.this, ResetSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    // Hiển thị/ẩn mật khẩu mới
    private void toggleNewPasswordVisibility() {
        if (isNewPasswordVisible) {
            // Ẩn mật khẩu
            newPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleNewPasswordVisibility.setImageResource(R.drawable.ic_visibility);
            isNewPasswordVisible = false;
        } else {
            // Hiển thị mật khẩu
            newPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleNewPasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            isNewPasswordVisible = true;
        }
        // Đặt con trỏ về cuối văn bản
        newPasswordEditText.setSelection(newPasswordEditText.getText().length());
    }

    // Hiển thị/ẩn xác nhận mật khẩu
    private void toggleConfirmPasswordVisibility() {
        if (isConfirmPasswordVisible) {
            // Ẩn mật khẩu
            confirmPasswordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            toggleConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility);
            isConfirmPasswordVisible = false;
        } else {
            // Hiển thị mật khẩu
            confirmPasswordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            toggleConfirmPasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
            isConfirmPasswordVisible = true;
        }
        // Đặt con trỏ về cuối văn bản
        confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
    }
}
