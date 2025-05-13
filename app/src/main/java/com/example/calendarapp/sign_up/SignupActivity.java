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
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
            // Kiểm tra email đã tồn tại chưa, sau đó chuyển đến màn hình xác minh
            checkEmailAndProceed(email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }



    // Replace the checkEmailAndProceed method with this improved version
    private void checkEmailAndProceed(final String email, final String password) {
        // Hiển thị ProgressDialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Sử dụng HttpURLConnection thay vì Retrofit
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://10.0.2.2/schedule_api/check_email.php");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Chuẩn bị dữ liệu
                    String data = "email=" + URLEncoder.encode(email, "UTF-8");

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Lấy mã phản hồi
                    int responseCode = conn.getResponseCode();
                    Log.d("SignupActivity", "Response code: " + responseCode);

                    // Đọc phản hồi
                    BufferedReader br;
                    if (responseCode >= 200 && responseCode < 300) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    } else {
                        br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    }

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    final String responseStr = response.toString();
                    Log.d("SignupActivity", "Response: " + responseStr);

                    // Kiểm tra xem phản hồi có phải là JSON hợp lệ không
                    if (responseStr.trim().startsWith("{") && responseStr.trim().endsWith("}")) {
                        try {
                            // Phân tích phản hồi JSON
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            final boolean success = jsonResponse.getBoolean("status");
                            final String message = jsonResponse.getString("message");
                            final boolean emailExists = jsonResponse.has("data") ? jsonResponse.getBoolean("data") : false;

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();

                                    if (success) {
                                        if (emailExists) {
                                            // Email đã tồn tại
                                            emailInputLayout.setError("Email này đã được sử dụng");
                                            emailEditText.requestFocus();
                                        } else {
                                            // Email chưa tồn tại, chuyển đến màn hình xác minh
                                            proceedToVerification(email, password);
                                        }
                                    } else {
                                        // Lỗi từ API
                                        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                                        // Fallback: Vẫn tiếp tục để demo
                                        proceedToVerification(email, password);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            final String jsonError = e.getMessage();
                            Log.e("SignupActivity", "JSON parsing error: " + jsonError);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();
                                    Toast.makeText(SignupActivity.this,
                                            "Lỗi phân tích JSON: " + jsonError, Toast.LENGTH_LONG).show();
                                    // Fallback: Vẫn tiếp tục để demo
                                    proceedToVerification(email, password);
                                }
                            });
                        }
                    } else {
                        // Phản hồi không phải là JSON
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(SignupActivity.this,
                                        "Phản hồi không phải là JSON hợp lệ", Toast.LENGTH_LONG).show();
                                // Fallback: Vẫn tiếp tục để demo
                                proceedToVerification(email, password);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    Log.e("SignupActivity", "Error: " + errorMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(SignupActivity.this,
                                    "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                            // Fallback: Vẫn tiếp tục để demo
                            proceedToVerification(email, password);
                        }
                    });
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }

    private void proceedToVerification(String email, String password) {
        // Tạo username từ email
        String username = email.split("@")[0];
        String fullName = ""; // Để trống hoặc lấy từ một EditText khác nếu có

        // Chuyển đến màn hình xác minh
        Intent intent = new Intent(SignupActivity.this, SignupVerificationActivity.class);
        intent.putExtra("EMAIL", email);
        intent.putExtra("USERNAME", username);
        intent.putExtra("PASSWORD", password);
        intent.putExtra("FULL_NAME", fullName);
        startActivity(intent);
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