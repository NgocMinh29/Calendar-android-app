package com.example.calendarapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.MainActivity;
import com.example.calendarapp.R;
import com.example.calendarapp.login.LoginActivity;
import com.example.calendarapp.utils.SessionManager;

public class SettingsFragment extends Fragment {
    private Switch switchDarkMode;
    private Spinner spinnerWeekStart;
    private Switch switchNotifications;
    private Spinner spinnerReminderTime;
    private TextView tvUserInfo;
    private Button btnLogout;
    private Button btnLogin;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Khởi tạo SessionManager
        sessionManager = ((MainActivity) requireActivity()).getSessionManager();

        // Ánh xạ các thành phần giao diện
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        spinnerWeekStart = view.findViewById(R.id.spinner_week_start);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);
        tvUserInfo = view.findViewById(R.id.tv_user_info);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogin = view.findViewById(R.id.btn_login);

        setupSpinners();
        loadSettings();
        setupAuthUI();

        return view;
    }

    private void setupSpinners() {
        // Week start day options
        String[] weekDays = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, weekDays);
        spinnerWeekStart.setAdapter(weekAdapter);

        // Reminder time options
        String[] reminderTimes = {"5 phút", "10 phút", "15 phút", "30 phút", "1 giờ", "2 giờ"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, reminderTimes);
        spinnerReminderTime.setAdapter(reminderAdapter);
    }

    private void loadSettings() {
        // Load saved settings
        switchDarkMode.setChecked(false);
        spinnerWeekStart.setSelection(0); // Monday
        switchNotifications.setChecked(true);
        spinnerReminderTime.setSelection(2); // 15 minutes
    }

    // Thay đổi phương thức setupAuthUI trong SettingsFragment.java
    private void setupAuthUI() {
        // Kiểm tra trạng thái đăng nhập
        if (sessionManager.isLoggedIn()) {
            // Người dùng đã đăng nhập
            String userEmail = sessionManager.getUserEmail();
            tvUserInfo.setText("Đang đăng nhập với: " + userEmail);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);

            // Xử lý sự kiện đăng xuất
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Đăng xuất
                    sessionManager.logout();

                    // Chuyển đến màn hình đăng nhập
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        } else if (sessionManager.isGuest()) {
            // Người dùng khách
            tvUserInfo.setText("Bạn đang sử dụng với tư cách khách");
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);

            // Xử lý sự kiện đăng nhập
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chuyển đến màn hình đăng nhập
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
        } else {
            // Trường hợp không xác định - chuyển về màn hình đăng nhập
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }
}