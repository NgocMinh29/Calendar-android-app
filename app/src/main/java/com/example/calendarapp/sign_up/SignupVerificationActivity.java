package com.example.calendarapp.sign_up;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;
import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.RegisterRequest;
import com.example.calendarapp.models.User;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupVerificationActivity extends AppCompatActivity {

    private static final String TAG = "SignupVerification";
    private EditText digit1, digit2, digit3, digit4, digit5, digit6;
    private Button verifyButton;
    private ImageButton backButton;
    private TextView resendCodeTextView, emailTextView;
    private String email, username, password, fullName;
    private CountDownTimer resendTimer;
    private boolean canResend = true;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_verification);

        // Lấy thông tin từ Intent
        email = getIntent().getStringExtra("EMAIL");
        username = getIntent().getStringExtra("USERNAME");
        password = getIntent().getStringExtra("PASSWORD");
        fullName = getIntent().getStringExtra("FULL_NAME");

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
                validateVerificationCode();
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
                    requestVerificationCode();
                }
            }
        });

        // Tự động focus vào ô đầu tiên
        digit1.requestFocus();

        // Yêu cầu mã xác nhận khi màn hình được tạo
        requestVerificationCode();
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
    private void validateVerificationCode() {
        String enteredCode = digit1.getText().toString() +
                digit2.getText().toString() +
                digit3.getText().toString() +
                digit4.getText().toString() +
                digit5.getText().toString() +
                digit6.getText().toString();

        if (enteredCode.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã xác nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi mã xác nhận đến server để kiểm tra
        verifyCode(enteredCode);
    }

    // Yêu cầu mã xác nhận từ server
    // Update the requestVerificationCode method to use the new handler
    private void requestVerificationCode() {
        // Hiển thị dialog tiến trình
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
                            "&type=" + URLEncoder.encode("signup", "UTF-8");

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
                        handleVerificationCodeResponse(responseStr);
                    } else {
                        // Phản hồi không phải là JSON
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                Toast.makeText(SignupVerificationActivity.this,
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

                            Toast.makeText(SignupVerificationActivity.this,
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

    // Xác minh mã với server và đăng ký người dùng nếu thành công
    // Update the verifyCode method to handle errors better
    // Update the verifyCode method to better handle the verification process
    // Update the verifyCode method to better handle the verification process
    private void verifyCode(final String enteredCode) {
        // Hiển thị dialog tiến trình
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xác minh mã...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://10.0.2.2/schedule_api/verify_code.php");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Chuẩn bị dữ liệu
                    String data = "email=" + URLEncoder.encode(email, "UTF-8") +
                            "&code=" + URLEncoder.encode(enteredCode, "UTF-8") +
                            "&type=" + URLEncoder.encode("signup", "UTF-8");

                    // Log the data being sent
                    Log.d(TAG, "Sending data: " + data);

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Lấy mã phản hồi
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Response code: " + responseCode);

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

                                    Toast.makeText(SignupVerificationActivity.this, message, Toast.LENGTH_LONG).show();

                                    if (success) {
                                        // Mã xác nhận hợp lệ, tiến hành đăng ký người dùng
                                        registerUser();
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

                                    Toast.makeText(SignupVerificationActivity.this,
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

                                Toast.makeText(SignupVerificationActivity.this,
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

                            Toast.makeText(SignupVerificationActivity.this,
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

    // Đăng ký người dùng sau khi xác minh thành công
    private void registerUser() {
        // Khởi tạo API service
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Tạo request
        RegisterRequest registerRequest = new RegisterRequest(username, email, password, fullName != null ? fullName : "");

        // Gọi API
        Call<ApiResponse<User>> call = apiService.register(registerRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isStatus()) {
                        // Đăng ký thành công
                        User user = apiResponse.getData();

                        Toast.makeText(SignupVerificationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                        // Lưu thông tin tài khoản vào SharedPreferences
                        SharedPreferences userPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
                        userPrefs.edit()
                                .putString(email + "_password", password)
                                .apply();

                        // Chuyển đến màn hình thành công
                        Intent intent = new Intent(SignupVerificationActivity.this, SignupSuccessActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Đăng ký thất bại
                        Toast.makeText(SignupVerificationActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Lỗi từ server
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(SignupVerificationActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(SignupVerificationActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                // Lỗi kết nối
                Toast.makeText(SignupVerificationActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Fallback: Thử đăng ký bằng HttpURLConnection
                registerUserWithHttpURLConnection();
            }
        });
    }

    // Phương thức dự phòng để đăng ký người dùng bằng HttpURLConnection
    private void registerUserWithHttpURLConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL("http://10.0.2.2/schedule_api/register.php");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);

                    // Chuẩn bị dữ liệu
                    String data = "username=" + URLEncoder.encode(username, "UTF-8") +
                            "&email=" + URLEncoder.encode(email, "UTF-8") +
                            "&password=" + URLEncoder.encode(password, "UTF-8") +
                            "&full_name=" + URLEncoder.encode(fullName != null ? fullName : "", "UTF-8");

                    // Gửi dữ liệu
                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.close();

                    // Lấy mã phản hồi
                    int responseCode = conn.getResponseCode();
                    Log.d(TAG, "Register response code: " + responseCode);

                    // Đọc phản hồi
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();

                    final String responseStr = response.toString();
                    Log.d(TAG, "Register response: " + responseStr);

                    // Kiểm tra xem phản hồi có phải là JSON hợp lệ không
                    if (responseStr.trim().startsWith("{") && responseStr.trim().endsWith("}")) {
                        try {
                            // Phân tích phản hồi JSON
                            JSONObject jsonResponse = new JSONObject(responseStr);
                            final boolean success = jsonResponse.has("status") ?
                                    jsonResponse.getBoolean("status") :
                                    responseCode >= 200 && responseCode < 300;
                            final String message = jsonResponse.has("message") ?
                                    jsonResponse.getString("message") :
                                    "Đăng ký thành công";

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (success) {
                                        // Đăng ký thành công
                                        Toast.makeText(SignupVerificationActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                                        // Lưu thông tin tài khoản vào SharedPreferences
                                        SharedPreferences userPrefs = getSharedPreferences("UserData", MODE_PRIVATE);
                                        userPrefs.edit()
                                                .putString(email + "_password", password)
                                                .apply();

                                        // Chuyển đến màn hình thành công
                                        Intent intent = new Intent(SignupVerificationActivity.this, SignupSuccessActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // Đăng ký thất bại
                                        Toast.makeText(SignupVerificationActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            final String jsonError = e.getMessage();
                            Log.e(TAG, "JSON parsing error: " + jsonError);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(SignupVerificationActivity.this,
                                            "Lỗi phân tích JSON: " + jsonError, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        // Phản hồi không phải là JSON
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SignupVerificationActivity.this,
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
                            Toast.makeText(SignupVerificationActivity.this,
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
    // Add this method to display the verification code from the response
    // Add this method to display the verification code from the response
    private void handleVerificationCodeResponse(String responseStr) {
        try {
            JSONObject jsonResponse = new JSONObject(responseStr);
            final boolean success = jsonResponse.getBoolean("status");
            final String message = jsonResponse.getString("message");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }

                    Toast.makeText(SignupVerificationActivity.this, message, Toast.LENGTH_LONG).show();

                    if (success) {
                        // Bắt đầu đếm ngược thời gian gửi lại mã
                        startResendTimer();
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

                    Toast.makeText(SignupVerificationActivity.this,
                            "Lỗi phân tích JSON: " + jsonError, Toast.LENGTH_LONG).show();
                }
            });
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}