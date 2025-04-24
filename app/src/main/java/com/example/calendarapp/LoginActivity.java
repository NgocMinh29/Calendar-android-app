package com.example.calendarapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox;
    private AppCompatButton loginButton, forgotPasswordButton;
    private AppCompatButton continueWithoutAccountButton, googleButton, facebookButton;
    private TextView signUpTextView;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ các thành phần giao diện
        initViews();

        // Kiểm tra xem người dùng đã lưu thông tin đăng nhập chưa
        checkSavedCredentials();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        // TextInputLayout
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        // TextInputEditText
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        // CheckBox
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // Buttons
        loginButton = findViewById(R.id.loginButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        continueWithoutAccountButton = findViewById(R.id.continueWithoutAccountButton);
        googleButton = findViewById(R.id.googleButton);
        facebookButton = findViewById(R.id.facebookButton);

        // TextView
        signUpTextView = findViewById(R.id.signUpTextView);
    }

    private void checkSavedCredentials() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean rememberMe = preferences.getBoolean(KEY_REMEMBER, false);

        if (rememberMe) {
            String email = preferences.getString(KEY_EMAIL, "");
            String password = preferences.getString(KEY_PASSWORD, "");

            emailEditText.setText(email);
            passwordEditText.setText(password);
            rememberMeCheckBox.setChecked(true);
        }
    }

    private void setupClickListeners() {
        // Xử lý sự kiện khi nhấn nút đăng nhập
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Xử lý sự kiện khi nhấn nút quên mật khẩu
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn nút tiếp tục mà không cần tài khoản
        continueWithoutAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ chuyển đến màn hình chính của ứng dụng
                // với quyền truy cập hạn chế
                Toast.makeText(LoginActivity.this, "Tiếp tục mà không cần tài khoản", Toast.LENGTH_SHORT).show();
                navigateToMainActivity(null); // null đại diện cho người dùng khách
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Google
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ tích hợp Google Sign-In SDK
                Toast.makeText(LoginActivity.this, "Đăng nhập bằng Google", Toast.LENGTH_SHORT).show();
                // Mô phỏng đăng nhập thành công
                simulateSocialLogin("google");
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Facebook
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ tích hợp Facebook Login SDK
                Toast.makeText(LoginActivity.this, "Đăng nhập bằng Facebook", Toast.LENGTH_SHORT).show();
                // Mô phỏng đăng nhập thành công
                simulateSocialLogin("facebook");
            }
        });

        // Xử lý sự kiện khi nhấn vào "Sign up"
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        // Đặt lại lỗi
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Lấy giá trị từ các trường nhập liệu
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của email và mật khẩu
        boolean cancel = false;
        View focusView = null;

        // Kiểm tra mật khẩu
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Vui lòng nhập mật khẩu");
            focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Mật khẩu phải có ít nhất 6 ký tự");
            focusView = passwordEditText;
            cancel = true;
        }

        // Kiểm tra email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Vui lòng nhập email hoặc số điện thoại");
            focusView = emailEditText;
            cancel = true;
        } else if (!isEmailValid(email) && !isPhoneValid(email)) {
            emailInputLayout.setError("Email hoặc số điện thoại không hợp lệ");
            focusView = emailEditText;
            cancel = true;
        }

        if (cancel) {
            // Có lỗi, focus vào trường lỗi đầu tiên
            focusView.requestFocus();
        } else {
            // Thực hiện đăng nhập
            performLogin(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPhoneValid(String phone) {
        // Kiểm tra số điện thoại đơn giản
        return phone.matches("\\d{10,11}");
    }

    private void performLogin(String email, String password) {
        // Trong thực tế, bạn sẽ gửi yêu cầu đến server để xác thực
        // Đây là mã giả để mô phỏng quá trình

        // Kiểm tra thông tin đăng nhập từ SharedPreferences (chỉ để demo)
        SharedPreferences userPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String savedPassword = userPrefs.getString(email + "_password", "");

        if (!TextUtils.isEmpty(savedPassword) && savedPassword.equals(password)) {
            // Đăng nhập thành công
            loginSuccess(email);
        } else {
            // Thử với mật khẩu mặc định cho demo
            if (password.equals("123456")) {
                loginSuccess(email);
            } else {
                // Đăng nhập thất bại
                Toast.makeText(this, "Email hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginSuccess(String email) {
        // Lưu thông tin đăng nhập nếu người dùng chọn "Remember me"
        if (rememberMeCheckBox.isChecked()) {
            saveLoginCredentials(email, passwordEditText.getText().toString());
        } else {
            clearSavedCredentials();
        }

        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

        // Chuyển đến màn hình chính
        navigateToMainActivity(email);
    }

    private void saveLoginCredentials(String email, String password) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER, true);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    private void clearSavedCredentials() {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER, false);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.apply();
    }

    private void navigateToMainActivity(String email) {
        // Trong thực tế, bạn sẽ chuyển đến màn hình chính của ứng dụng
        // Đây là mã giả để mô phỏng quá trình
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        if (email != null) {
            intent.putExtra("EMAIL", email);
        }
        startActivity(intent);
        finish(); // Đóng màn hình đăng nhập
    }

    private void simulateSocialLogin(String provider) {
        // Mô phỏng đăng nhập thành công bằng tài khoản xã hội
        String email = provider + "_user@example.com";
        navigateToMainActivity(email);
    }
}
