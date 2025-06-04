package com.example.calendarapp.forgot_password;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private EditText emailEditText;
    private Button nextButton;
    private ImageButton closeButton;
    private TextView termsTextView;
    private ProgressDialog progressDialog;

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
                    // Kiểm tra email và gửi mã xác nhận
                    checkEmailAndSendCode(email);
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

    // Kiểm tra tính hợp lệ của email
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    // Kiểm tra email và gửi mã xác nhận
    private void checkEmailAndSendCode(final String email) {
        // Hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra...");
        progressDialog.setCancelable(false);
        progressDialog.show();

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
                    Log.d(TAG, "Response code: " + responseCode);

                    // Đọc phản hồi
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    final String responseStr = response.toString();
                    Log.d(TAG, "Response: " + responseStr);

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
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    if (success) {
                                        if (emailExists) {
                                            // Email tồn tại, gửi mã xác nhận
                                            sendVerificationCode(email);
                                        } else {
                                            // Email không tồn tại
                                            Toast.makeText(ForgotPasswordActivity.this,
                                                    "Không tìm thấy tài khoản với email này",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        // Lỗi từ API
                                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            final String jsonError = e.getMessage();
                            Log.e(TAG, "JSON parsing error: " + jsonError);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Lỗi phân tích JSON: " + jsonError, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        // Phản hồi không phải là JSON
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Phản hồi không phải là JSON hợp lệ", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    Log.e(TAG, "Error: " + errorMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
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

    // Phương thức gửi mã xác nhận đến email
    private void sendVerificationCode(final String email) {
        // Hiển thị ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi mã xác nhận...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://10.0.2.2/schedule_api/send_verification_code.php");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Chuẩn bị dữ liệu
                    String data = "email=" + URLEncoder.encode(email, "UTF-8") +
                            "&type=" + URLEncoder.encode("reset", "UTF-8");

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Lấy mã phản hồi
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response code: " + responseCode);

                    // Đọc phản hồi
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    final String responseStr = response.toString();
                    Log.d(TAG, "Response: " + responseStr);

                    // Kiểm tra xem phản hồi có phải là JSON hợp lệ không
                    if (responseStr.trim().startsWith("{") && responseStr.trim().endsWith("}")) {
                        try {
                            // Phân tích phản hồi JSON
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            final boolean success = jsonResponse.getBoolean("status");
                            final String message = jsonResponse.getString("message");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }

                                    Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_LONG).show();

                                    if (success) {
                                        // Chuyển đến màn hình nhập mã xác nhận
                                        Intent intent = new Intent(ForgotPasswordActivity.this, VerificationActivity.class);
                                        intent.putExtra("EMAIL", email);
                                        startActivity(intent);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            final String jsonError = e.getMessage();
                            Log.e(TAG, "JSON parsing error: " + jsonError);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progressDialog != null && progressDialog.isShowing()) {
                                        progressDialog.dismiss();
                                    }
                                    Toast.makeText(ForgotPasswordActivity.this,
                                            "Lỗi phân tích JSON: " + jsonError, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        // Phản hồi không phải là JSON
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(ForgotPasswordActivity.this,
                                        "Phản hồi không phải là JSON hợp lệ: " + responseStr,
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    final String errorMessage = e.getMessage();
                    Log.e(TAG, "Error: " + errorMessage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (progressDialog != null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            Toast.makeText(ForgotPasswordActivity.this,
                                    "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}