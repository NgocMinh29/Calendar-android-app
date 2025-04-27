package com.example.calendarapp.sign_up;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.RegisterRequest;
import com.example.calendarapp.models.User;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.calendarapp.MainActivity_Login;
import com.example.calendarapp.R;
import com.example.calendarapp.forgot_password.ForgotPasswordActivity;
import com.example.calendarapp.login.LoginActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private CheckBox termsCheckBox;
    private AppCompatButton signupButton, forgotPasswordButton, continueWithoutAccountButton, googleButton, facebookButton;
    private TextView loginTextView;

    // Mẫu kiểm tra email hợp lệ
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Ánh xạ các thành phần giao diện
        initViews();

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    private void initViews() {
        // TextInputLayout
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        // TextInputEditText
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

        // CheckBox
        termsCheckBox = findViewById(R.id.termsCheckBox);

        // Buttons
        signupButton = findViewById(R.id.signupButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        continueWithoutAccountButton = findViewById(R.id.continueWithoutAccountButton);
        googleButton = findViewById(R.id.googleButton);
        facebookButton = findViewById(R.id.facebookButton);

        // TextView
        loginTextView = findViewById(R.id.loginTextView);
    }

    private void setupClickListeners() {
        // Xử lý sự kiện khi nhấn nút đăng ký
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignup();
            }
        });

        // Xử lý sự kiện khi nhấn nút quên mật khẩu
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn nút tiếp tục mà không cần tài khoản
        continueWithoutAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ chuyển đến màn hình chính của ứng dụng
                // với quyền truy cập hạn chế
                Toast.makeText(SignupActivity.this, "Tiếp tục mà không cần tài khoản", Toast.LENGTH_SHORT).show();
                navigateToMainActivity(null); // null đại diện cho người dùng khách
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Google
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ tích hợp Google Sign-In SDK
                Toast.makeText(SignupActivity.this, "Đăng nhập bằng Google", Toast.LENGTH_SHORT).show();
                // Mô phỏng đăng nhập thành công
                simulateSocialLogin("google");
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Facebook
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Trong thực tế, bạn sẽ tích hợp Facebook Login SDK
                Toast.makeText(SignupActivity.this, "Đăng nhập bằng Facebook", Toast.LENGTH_SHORT).show();
                // Mô phỏng đăng nhập thành công
                simulateSocialLogin("facebook");
            }
        });

        // Xử lý sự kiện khi nhấn vào "Log in"
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void attemptSignup() {
        // Đặt lại lỗi
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        // Lấy giá trị từ các trường nhập liệu
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của các trường
        boolean cancel = false;
        View focusView = null;

        // Kiểm tra xác nhận mật khẩu
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError("Vui lòng xác nhận mật khẩu");
            focusView = confirmPasswordEditText;
            cancel = true;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordInputLayout.setError("Mật khẩu xác nhận không khớp");
            focusView = confirmPasswordEditText;
            cancel = true;
        }

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
            emailInputLayout.setError("Vui lòng nhập email");
            focusView = emailEditText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            emailInputLayout.setError("Email không hợp lệ");
            focusView = emailEditText;
            cancel = true;
        }

        // Kiểm tra đồng ý với điều khoản
        if (!termsCheckBox.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản và điều kiện", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            // Có lỗi, focus vào trường lỗi đầu tiên
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Thực hiện đăng ký
            performSignup(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private void performSignup(String email, String password) {
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Khởi tạo API service
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Tạo request
        // Thay đổi dòng nà

// Thành
        String username = email.split("@")[0]; // Tạo username từ email
        String fullName = ""; // Để trống hoặc lấy từ một EditText khác nếu có
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, fullName);

        // Gọi API
        Call<ApiResponse<User>> call = apiService.register(registerRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isStatus()) {
                        // Đăng ký thành công
                        User user = apiResponse.getData();

                        Toast.makeText(SignupActivity.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                        // Lưu thông tin tài khoản vào SharedPreferences (chỉ để demo)
                        SharedPreferences userPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
                        userPrefs.edit()
                                .putString(email + "_password", password)
                                .apply();

                        // Chuyển đến màn hình xác minh
                        Intent intent = new Intent(SignupActivity.this, SignupVerificationActivity.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                    } else {
                        // Đăng ký thất bại
                        Toast.makeText(SignupActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Lỗi từ server
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(SignupActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(SignupActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                // Lỗi kết nối
                Toast.makeText(SignupActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Fallback: Mô phỏng đăng ký thành công (chỉ để demo)
                // Tạo mã xác nhận và lưu vào SharedPreferences
                String verificationCode = generateRandomCode();
                getSharedPreferences("SignupVerification", MODE_PRIVATE)
                        .edit()
                        .putString("verification_code", verificationCode)
                        .putString("email", email)
                        .apply();

                // Thông báo cho người dùng
                Toast.makeText(SignupActivity.this, "Đã gửi mã xác nhận đến " + email, Toast.LENGTH_LONG).show();

                // Chuyển đến màn hình xác minh
                Intent intent = new Intent(SignupActivity.this, SignupVerificationActivity.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }
        });
    }

    // Tạo mã xác nhận ngẫu nhiên 6 chữ số
    private String generateRandomCode() {
        int code = (int) (Math.random() * 900000) + 100000; // Tạo số ngẫu nhiên từ 100000 đến 999999
        return String.valueOf(code);
    }

    private void navigateToMainActivity(String email) {
        // Trong thực tế, bạn sẽ chuyển đến màn hình chính của ứng dụng
        Intent intent = new Intent(SignupActivity.this, MainActivity_Login.class);
        if (email != null) {
            intent.putExtra("EMAIL", email);
        }
        startActivity(intent);
        finish(); // Đóng màn hình đăng ký
    }

    private void simulateSocialLogin(String provider) {
        // Mô phỏng đăng nhập thành công bằng tài khoản xã hội
        String email = provider + "_user@example.com";
        navigateToMainActivity(email);
    }
}
