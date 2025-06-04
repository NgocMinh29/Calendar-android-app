package com.example.calendarapp.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.example.calendarapp.MainActivity;
import com.example.calendarapp.R;
import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.billing.BillingManager;
import com.example.calendarapp.login.LoginActivity;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.PremiumStatus;
import com.example.calendarapp.premium.PremiumPurchaseActivity;
import com.example.calendarapp.utils.ReminderReceiver;
import com.example.calendarapp.utils.SessionManager;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment implements BillingManager.BillingListener {
    private static final String TAG = "SettingsFragment";

    private Switch switchDarkMode;
    private Spinner spinnerWeekStart;
    private Switch switchNotifications;
    private TextView tvUserInfo;
    private Button btnLogout;
    private Button btnLogin;
    private Button btnPremium;
    private TextView tvPremiumDescription;
    private TextView tvAppVersion;

    private SessionManager sessionManager;
    private ApiService apiService;
    private BillingManager billingManager;
    private boolean isPremiumUser = false;
    private boolean isCheckingPremiumStatus = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo SessionManager
        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        apiService = ApiClient.getClient().create(ApiService.class);

        // Ánh xạ các thành phần giao diện
        initViews(view);
        setupSpinners();
        loadSettings();
        setupDarkModeSwitch();
        setupNotificationsSwitch();
        setupAuthUI();

        // Khởi tạo billing manager
        billingManager = new BillingManager(requireContext(), this);
        billingManager.startConnection();

        // Kiểm tra trạng thái premium
        checkPremiumStatus();

        return view;
    }

    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        spinnerWeekStart = view.findViewById(R.id.spinner_week_start);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        tvUserInfo = view.findViewById(R.id.tv_user_info);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogin = view.findViewById(R.id.btn_login);
        btnPremium = view.findViewById(R.id.btn_premium);
        tvPremiumDescription = view.findViewById(R.id.tv_premium_description);
        tvAppVersion = view.findViewById(R.id.tv_app_version);

        // Thiết lập phiên bản ứng dụng
        try {
            String versionName = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            tvAppVersion.setText("Phiên bản " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Phiên bản 1.0.0");
        }
    }

    private void setupSpinners() {
        // Week start day options
        String[] weekDays = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        int[] calendarDays = {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
        };
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, weekDays);
        spinnerWeekStart.setAdapter(weekAdapter);

        // Thiết lập listener cho spinner tuần bắt đầu từ
        spinnerWeekStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedDay = calendarDays[position];
                SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
                prefs.edit().putInt("week_start_day", selectedDay).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadSettings() {
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Load dark mode setting
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        // Load week start day
        int weekStartDay = prefs.getInt("week_start_day", Calendar.MONDAY);
        int weekStartPosition = 0; // Default to Monday
        switch (weekStartDay) {
            case Calendar.MONDAY: weekStartPosition = 0; break;
            case Calendar.TUESDAY: weekStartPosition = 1; break;
            case Calendar.WEDNESDAY: weekStartPosition = 2; break;
            case Calendar.THURSDAY: weekStartPosition = 3; break;
            case Calendar.FRIDAY: weekStartPosition = 4; break;
            case Calendar.SATURDAY: weekStartPosition = 5; break;
            case Calendar.SUNDAY: weekStartPosition = 6; break;
        }
        spinnerWeekStart.setSelection(weekStartPosition);

        // Load notifications setting
        boolean notificationsEnabled = prefs.getBoolean("notificationEnabled", true);
        switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupDarkModeSwitch() {
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupNotificationsSwitch() {
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("notificationEnabled", isChecked);
            editor.apply();

            if (!isChecked) {
                // Hủy tất cả các thông báo nếu tắt
                cancelAllAlarms();
                Toast.makeText(getContext(), "Đã tắt tất cả thông báo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelAllAlarms() {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

        // Ví dụ: giả sử bạn đặt mỗi nhắc nhở có requestCode riêng → bạn phải biết hoặc lưu lại các ID này.
        // Đơn giản: nếu bạn chỉ dùng 1 PendingIntent chung cho các nhắc nhở:
        Intent intent = new Intent(requireContext(), ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    private void setupAuthUI() {
        // Kiểm tra trạng thái đăng nhập
        if (sessionManager.isLoggedIn()) {
            // Người dùng đã đăng nhập
            String userEmail = sessionManager.getUserEmail();
            tvUserInfo.setText("Đang đăng nhập với: " + userEmail);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

            // Xử lý sự kiện đăng xuất
            btnLogout.setOnClickListener(v -> {
                // Đăng xuất
                sessionManager.logout();

                // Chuyển đến màn hình đăng nhập
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            });
        } else if (sessionManager.isGuest()) {
            // Người dùng khách
            tvUserInfo.setText("Bạn đang sử dụng với tư cách khách");
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);

            // Xử lý sự kiện đăng nhập
            btnLogin.setOnClickListener(v -> {
                // Chuyển đến màn hình đăng nhập
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            });
        } else {
            // Trường hợp không xác định - chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

        // Setup premium button
        btnPremium.setOnClickListener(v -> {
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để nâng cấp Premium", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isPremiumUser) {
                Toast.makeText(getContext(), "Bạn đã là thành viên Premium", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển đến màn hình mua premium
            Intent intent = new Intent(getActivity(), PremiumPurchaseActivity.class);
            startActivity(intent);
        });
    }

    private void checkPremiumStatus() {
        // Tránh multiple API calls cùng lúc
        if (isCheckingPremiumStatus) {
            return;
        }

        // Luôn hiển thị trạng thái từ SessionManager trước
        isPremiumUser = sessionManager.isPremium();
        updatePremiumUI(isPremiumUser);

        // Nếu không đăng nhập hoặc đã là premium, không cần kiểm tra từ server
        if (!sessionManager.isLoggedIn()) {
            updatePremiumUI(false);
            return;
        }

        // Nếu đã là premium theo SessionManager, không cần gọi API
        if (isPremiumUser) {
            Log.d(TAG, "User is already premium according to SessionManager");
            return;
        }

        isCheckingPremiumStatus = true;

        // Kiểm tra từ server chỉ khi chưa premium
        String userEmail = sessionManager.getUserEmail();
        apiService.getPremiumStatus(userEmail).enqueue(new Callback<ApiResponse<PremiumStatus>>() {
            @Override
            public void onResponse(Call<ApiResponse<PremiumStatus>> call, Response<ApiResponse<PremiumStatus>> response) {
                isCheckingPremiumStatus = false;
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            PremiumStatus status = response.body().getData();
                            boolean serverPremiumStatus = status != null && status.isPremium();

                            Log.d(TAG, "Server premium status: " + serverPremiumStatus);
                            Log.d(TAG, "SessionManager premium status: " + sessionManager.isPremium());

                            // Chỉ cập nhật nếu có thay đổi
                            if (serverPremiumStatus != sessionManager.isPremium()) {
                                sessionManager.updatePremiumStatus(serverPremiumStatus);
                                isPremiumUser = serverPremiumStatus;
                                updatePremiumUI(serverPremiumStatus);
                                Log.d(TAG, "Updated premium status to: " + serverPremiumStatus);
                            }
                        } else {
                            Log.w(TAG, "API response unsuccessful, keeping current status");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing premium status response", e);
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<PremiumStatus>> call, Throwable t) {
                isCheckingPremiumStatus = false;
                if (getActivity() == null) return;

                Log.e(TAG, "Failed to check premium status", t);
                // Giữ nguyên trạng thái hiện tại khi API lỗi
            }
        });
    }

    private void updatePremiumUI(boolean isPremium) {
        Log.d(TAG, "Updating premium UI with status: " + isPremium);

        if (isPremium) {
            btnPremium.setText("✓ Đã nâng cấp Premium");
            btnPremium.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
            btnPremium.setEnabled(false);
            tvPremiumDescription.setText("Bạn đang sử dụng phiên bản Premium");
        } else {
            btnPremium.setText("Nâng cấp Premium - 99.000đ");
            btnPremium.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
            btnPremium.setEnabled(true);
            tvPremiumDescription.setText("Nâng cấp để trải nghiệm đầy đủ tính năng");
        }
        isPremiumUser = isPremium;
    }

    public void refreshPremiumStatus() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                isPremiumUser = sessionManager.isPremium();
                updatePremiumUI(isPremiumUser);
                Log.d(TAG, "Premium status refreshed: " + isPremiumUser);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "SettingsFragment onResume - checking premium status");
        // Kiểm tra lại trạng thái premium khi quay lại fragment
        checkPremiumStatus();
    }

    // BillingManager.BillingListener implementations
    @Override
    public void onBillingSetupFinished(boolean success) {
        if (success) {
            // Chỉ query purchases nếu chưa premium theo SessionManager
            if (!sessionManager.isPremium()) {
                billingManager.queryPurchases();
            } else {
                Log.d(TAG, "User already premium, skipping purchase query");
            }
        }
    }

    @Override
    public void onProductDetailsLoaded(ProductDetails productDetails) {
        // Không cần xử lý ở đây
    }

    @Override
    public void onPurchaseCompleted(Purchase purchase) {
        // Không cần xử lý ở đây vì sẽ được xử lý trong PremiumPurchaseActivity
    }

    @Override
    public void onPurchaseError(String error) {
        // Không cần xử lý ở đây
    }

    @Override
    public void onPremiumStatusChecked(boolean isPremium) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                // Chỉ cập nhật nếu SessionManager chưa có premium hoặc BillingManager confirm premium
                boolean currentSessionPremium = sessionManager.isPremium();

                // Nếu SessionManager đã premium, chỉ cập nhật khi BillingManager cũng confirm premium
                // Nếu SessionManager chưa premium, cập nhật theo BillingManager
                if (!currentSessionPremium || isPremium) {
                    Log.d(TAG, "BillingManager premium status: " + isPremium + ", SessionManager: " + currentSessionPremium);
                    updatePremiumUI(isPremium);
                } else {
                    Log.d(TAG, "Keeping SessionManager premium status, ignoring BillingManager: " + currentSessionPremium);
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (billingManager != null) {
            billingManager.endConnection();
        }
    }
}