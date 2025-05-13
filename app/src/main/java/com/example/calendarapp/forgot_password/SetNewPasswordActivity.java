package com.example.calendarapp.forgot_password;
import java.io.InputStream;
import android.widget.LinearLayout;
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
import android.app.ProgressDialog;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONObject;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
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
    // Thêm biến ProgressDialog ở đây
    private ProgressDialog progressDialog;

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

        // Khởi tạo và hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật mật khẩu...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    // Sửa URL từ calendar_api thành schedule_api
                    URL url = new URL("http://10.0.2.2/schedule_api/update_password.php");

                    Log.d("API_REQUEST", "Sending request to: " + url.toString());

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Chuẩn bị dữ liệu để gửi
                    String data = "email=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(newPassword, "UTF-8");

                    Log.d("API_REQUEST", "Data: " + data);

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Đọc phản hồi từ server
                    int responseCode = conn.getResponseCode();
                    Log.d("API_RESPONSE", "Response code: " + responseCode);

                    // Đọc phản hồi dù là thành công hay lỗi
                    InputStream inputStream;
                    if (responseCode >= 200 && responseCode < 300) {
                        inputStream = conn.getInputStream();
                    } else {
                        inputStream = conn.getErrorStream();
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    // Log phản hồi thô để debug
                    final String responseStr = response.toString();
                    Log.d("API_RESPONSE", "Raw response: " + responseStr);

                    // Kiểm tra xem phản hồi có phải là JSON hợp lệ không
                    if (responseStr.trim().startsWith("{") && responseStr.trim().endsWith("}")) {
                        try {
                            // Phân tích phản hồi JSON
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            final boolean success = jsonResponse.getBoolean("success");
                            final String message = jsonResponse.getString("message");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.dismiss();

                                    if (success) {
                                        // Lưu mật khẩu mới vào SharedPreferences (tùy chọn)
                                        getSharedPreferences("UserData", MODE_PRIVATE)
                                                .edit()
                                                .putString(email + "_password", newPassword)
                                                .apply();

                                        // Chuyển đến màn hình thành công
                                        Intent intent = new Intent(SetNewPasswordActivity.this, ResetSuccessActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Hiển thị thông báo lỗi
                                        Toast.makeText(SetNewPasswordActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            showError("Lỗi phân tích JSON: " + e.getMessage() + "\nPhản hồi: " + responseStr);
                        }
                    } else {
                        showError("Phản hồi không phải là JSON hợp lệ: " + responseStr);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Lỗi kết nối: " + e.getMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        }).start();
    }
    private void testConnection() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra kết nối...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    // Thử kết nối đến file test.php
                    URL url = new URL("http://10.0.2.2/schedule_api/test.php");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Gửi dữ liệu test
                    String data = "test_email=test@example.com&test_password=password123";

                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Đọc phản hồi
                    int responseCode = conn.getResponseCode();

                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    final String responseStr = response.toString();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(SetNewPasswordActivity.this,
                                    "Kết nối thành công: " + responseStr,
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(SetNewPasswordActivity.this,
                                    "Lỗi kết nối: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
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
    // Helper method để hiển thị lỗi trên UI thread
    private void showError(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(SetNewPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e("RESET_PASSWORD", errorMessage);
            }
        });
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
