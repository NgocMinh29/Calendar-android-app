package com.example.calendarapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.R;

public class SettingsFragment extends Fragment {
    private Switch switchDarkMode;
    private Spinner spinnerWeekStart;
    private Switch switchNotifications;
    private Spinner spinnerReminderTime;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        spinnerWeekStart = view.findViewById(R.id.spinner_week_start);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        spinnerReminderTime = view.findViewById(R.id.spinner_reminder_time);

        setupSpinners();
        loadSettings();

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
}
