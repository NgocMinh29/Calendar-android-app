package com.example.calendarapp.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.calendarapp.MainActivity;
import com.example.calendarapp.MainActivity_Login;
import com.example.calendarapp.R;
import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.forgot_password.ForgotPasswordActivity;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.LoginRequest;
import com.example.calendarapp.models.SocialLoginRequest;
import com.example.calendarapp.models.User;
import com.example.calendarapp.sign_up.SignupActivity;
import com.example.calendarapp.utils.SessionManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private AppCompatButton loginButton;
    private AppCompatButton forgotPasswordButton;
    private AppCompatButton continueWithoutAccountButton;
    private AppCompatButton googleButton;
    private AppCompatButton facebookButton;
    private TextView signUpTextView;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;

    // Facebook Login
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        // Ánh xạ các thành phần giao diện
        initViews();

        // Kiểm tra xem người dùng đã lưu thông tin đăng nhập chưa
        checkSavedCredentials();

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Cấu hình Google Sign In
        configureGoogleSignIn();

        // Cấu hình Facebook Login
        configureFacebookLogin();
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

    private void configureGoogleSignIn() {
        // Cấu hình Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();

        // Xây dựng GoogleSignInClient với các tùy chọn đã chỉ định
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configureFacebookLogin() {
        // Khởi tạo CallbackManager
        callbackManager = CallbackManager.Factory.create();

        // Đăng ký callback cho LoginManager
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "Facebook login success");
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "Facebook login canceled");
                        Toast.makeText(LoginActivity.this, "Đăng nhập Facebook đã bị hủy", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull FacebookException exception) {
                        Log.e(TAG, "Facebook login error", exception);
                        Toast.makeText(LoginActivity.this, "Lỗi đăng nhập Facebook: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
                Toast.makeText(LoginActivity.this, "Tiếp tục mà không cần tài khoản", Toast.LENGTH_SHORT).show();
                navigateToMainActivity(null); // null đại diện cho người dùng khách
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Google
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập bằng Facebook
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithFacebook();
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

    // Thêm phương thức attemptLogin() sau phương thức setupClickListeners()
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
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Khởi tạo API service
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Tạo request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Gọi API
        Call<ApiResponse<User>> call = apiService.login(loginRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        // Đăng nhập thành công
                        User user = apiResponse.getData();
                        loginSuccess(email);
                    } else {
                        // Đăng nhập thất bại
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Fallback: Thử với mật khẩu mặc định cho demo
                        if (password.equals("123456")) {
                            loginSuccess(email);
                        }
                    }
                } else {
                    // Lỗi từ server
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();

                        // Fallback: Thử với mật khẩu mặc định cho demo
                        if (password.equals("123456")) {
                            loginSuccess(email);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                // Lỗi kết nối
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Fallback: Thử với mật khẩu mặc định cho demo
                if (password.equals("123456")) {
                    loginSuccess(email);
                }
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithFacebook() {
        // Đăng xuất trước để đảm bảo người dùng có thể chọn tài khoản khác
        LoginManager.getInstance().logOut();

        // Yêu cầu quyền truy cập email và thông tin công khai
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Chuyển kết quả đến Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Kết quả trả về từ Intent đăng nhập Google
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Đăng nhập Google thành công
            String email = account.getEmail();
            String displayName = account.getDisplayName();
            String id = account.getId();

            Log.d(TAG, "Google Sign In success: " + email);
            Toast.makeText(this, "Đăng nhập Google thành công: " + email, Toast.LENGTH_SHORT).show();

            // Gửi thông tin đến server để đăng nhập/đăng ký
            performSocialLogin("google", id, email, displayName);
        } catch (ApiException e) {
            // Đăng nhập Google thất bại
            Log.w(TAG, "Google sign in failed", e);
            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        // Lấy thông tin người dùng từ Graph API
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                (object, response) -> {
                    try {
                        if (object != null) {
                            String id = object.getString("id");
                            String email = object.has("email") ? object.getString("email") : id + "@facebook.com";
                            String name = object.getString("name");

                            Log.d(TAG, "Facebook user info: " + object.toString());

                            // Gửi thông tin đến server để đăng nhập/đăng ký
                            performSocialLogin("facebook", id, email, name);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON error", e);
                        Toast.makeText(LoginActivity.this, "Lỗi xử lý thông tin Facebook", Toast.LENGTH_SHORT).show();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void performSocialLogin(String provider, String socialId, String email, String name) {
        // Hiển thị ProgressDialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý đăng nhập...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Khởi tạo API service
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Tạo request
        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(provider, socialId, email, name);

        // Gọi API
        Call<ApiResponse<User>> call = apiService.socialLogin(socialLoginRequest);
        call.enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        // Đăng nhập thành công
                        User user = apiResponse.getData();
                        Toast.makeText(LoginActivity.this, "Đăng nhập " + provider + " thành công", Toast.LENGTH_SHORT).show();
                        // Chuyển đến màn hình chính
                        navigateToMainActivity(email);
                    } else {
                        // Đăng nhập thất bại
                        Toast.makeText(LoginActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Lỗi từ server
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Toast.makeText(LoginActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                // Ẩn ProgressDialog
                progressDialog.dismiss();

                // Lỗi kết nối
                Toast.makeText(LoginActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                // Fallback: Đăng nhập thành công mà không cần xác thực server (chỉ để demo)
                navigateToMainActivity(email);
            }
        });
    }

    private void loginSuccess(String email) {
        // Lưu thông tin đăng nhập nếu người dùng chọn "Remember me"
        if (rememberMeCheckBox.isChecked()) {
            saveLoginCredentials(email, passwordEditText.getText().toString());
        } else {
            clearSavedCredentials();
        }

        // Lưu phiên đăng nhập
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.createLoginSession(email, null);

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

    // Thay đổi phương thức navigateToMainActivity trong LoginActivity.java
    private void navigateToMainActivity(String email) {
        SessionManager sessionManager = new SessionManager(this);

        if (email != null) {
            // Lưu phiên đăng nhập nếu không phải là khách
            sessionManager.createLoginSession(email, null);
        } else {
            // Tạo phiên đăng nhập khách
            sessionManager.createGuestSession();
        }

        // Chuyển đến màn hình chính
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Đóng màn hình đăng nhập
    }
}
