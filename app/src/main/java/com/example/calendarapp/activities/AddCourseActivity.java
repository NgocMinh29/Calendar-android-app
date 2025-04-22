package com.example.calendarapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;
import com.example.calendarapp.models.Course;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddCourseActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etCourseName;
    private EditText etRoom;
    private Spinner spinnerDay;
    private TextView tvStartTimeSelector;
    private TextView tvEndTimeSelector;
    private TextView tvStartDateSelector;
    private TextView tvEndDateSelector;
    private Spinner spinnerFrequency;
    private Switch switchNotification;
    private Spinner spinnerReminderTime;
    private Button btnCancel;
    private Button btnSave;

    private Calendar selectedStartDate = Calendar.getInstance();
    private Calendar selectedEndDate = Calendar.getInstance();
    private Calendar selectedStartTime = Calendar.getInstance();
    private Calendar selectedEndTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        initViews();
        setupSpinners();
        setupDateTimePickers();
        setupButtons();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        etCourseName = findViewById(R.id.et_course_name);
        etRoom = findViewById(R.id.et_room);
        spinnerDay = findViewById(R.id.spinner_day);
        tvStartTimeSelector = findViewById(R.id.tv_start_time_selector);
        tvEndTimeSelector = findViewById(R.id.tv_end_time_selector);
        tvStartDateSelector = findViewById(R.id.tv_start_date_selector);
        tvEndDateSelector = findViewById(R.id.tv_end_date_selector);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        switchNotification = findViewById(R.id.switch_notification);
        spinnerReminderTime = findViewById(R.id.spinner_reminder_time);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupSpinners() {
        // Day of week spinner
        String[] days = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(dayAdapter);

        // Frequency spinner
        String[] frequencies = {"1 tuần", "2 tuần", "3 tuần", "4 tuần"};
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, frequencies);
        spinnerFrequency.setAdapter(frequencyAdapter);

        // Reminder time spinner
        String[] reminderTimes = {"5 phút", "10 phút", "15 phút", "30 phút", "1 giờ", "2 giờ"};
        ArrayAdapter<String> reminderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reminderTimes);
        spinnerReminderTime.setAdapter(reminderAdapter);
        spinnerReminderTime.setSelection(2); // Default to 15 minutes
    }

    private void setupDateTimePickers() {
        // Setup start time picker
        tvStartTimeSelector.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedStartTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedStartTime.set(Calendar.MINUTE, minute);
                        updateStartTimeDisplay();
                    },
                    selectedStartTime.get(Calendar.HOUR_OF_DAY),
                    selectedStartTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Setup end time picker
        tvEndTimeSelector.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedEndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedEndTime.set(Calendar.MINUTE, minute);
                        updateEndTimeDisplay();
                    },
                    selectedEndTime.get(Calendar.HOUR_OF_DAY),
                    selectedEndTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Setup start date picker
        tvStartDateSelector.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedStartDate.set(Calendar.YEAR, year);
                        selectedStartDate.set(Calendar.MONTH, month);
                        selectedStartDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateStartDateDisplay();
                    },
                    selectedStartDate.get(Calendar.YEAR),
                    selectedStartDate.get(Calendar.MONTH),
                    selectedStartDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Setup end date picker
        tvEndDateSelector.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedEndDate.set(Calendar.YEAR, year);
                        selectedEndDate.set(Calendar.MONTH, month);
                        selectedEndDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateEndDateDisplay();
                    },
                    selectedEndDate.get(Calendar.YEAR),
                    selectedEndDate.get(Calendar.MONTH),
                    selectedEndDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });

        // Initialize displays
        updateStartTimeDisplay();
        updateEndTimeDisplay();
        updateStartDateDisplay();
        updateEndDateDisplay();
    }

    private void updateStartTimeDisplay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvStartTimeSelector.setText(timeFormat.format(selectedStartTime.getTime()));
    }

    private void updateEndTimeDisplay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvEndTimeSelector.setText(timeFormat.format(selectedEndTime.getTime()));
    }

    private void updateStartDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvStartDateSelector.setText(dateFormat.format(selectedStartDate.getTime()));
    }

    private void updateEndDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvEndDateSelector.setText(dateFormat.format(selectedEndDate.getTime()));
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveCourse();
            }
        });
    }

    private boolean validateInput() {
        if (etCourseName.getText().toString().trim().isEmpty()) {
            etCourseName.setError("Vui lòng nhập tên môn học");
            return false;
        }

        if (etRoom.getText().toString().trim().isEmpty()) {
            etRoom.setError("Vui lòng nhập phòng học");
            return false;
        }

        if (selectedEndDate.before(selectedStartDate)) {
            Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveCourse() {
        String name = etCourseName.getText().toString().trim();
        String room = etRoom.getText().toString().trim();
        String dayOfWeek = spinnerDay.getSelectedItem().toString();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String startTime = timeFormat.format(selectedStartTime.getTime());
        String endTime = timeFormat.format(selectedEndTime.getTime());

        Date startDate = selectedStartDate.getTime();
        Date endDate = selectedEndDate.getTime();

        // Get week frequency from spinner selection
        int weekFrequency = 1; // Default
        String frequencySelection = spinnerFrequency.getSelectedItem().toString();
        if (frequencySelection.contains("2")) {
            weekFrequency = 2;
        } else if (frequencySelection.contains("3")) {
            weekFrequency = 3;
        } else if (frequencySelection.contains("4")) {
            weekFrequency = 4;
        }

        boolean notification = switchNotification.isChecked();

        // Get reminder minutes from spinner selection
        int reminderMinutes = 15; // Default
        String reminderSelection = spinnerReminderTime.getSelectedItem().toString();
        if (reminderSelection.contains("5")) {
            reminderMinutes = 5;
        } else if (reminderSelection.contains("10")) {
            reminderMinutes = 10;
        } else if (reminderSelection.contains("15")) {
            reminderMinutes = 15;
        } else if (reminderSelection.contains("30")) {
            reminderMinutes = 30;
        } else if (reminderSelection.contains("1 giờ")) {
            reminderMinutes = 60;
        } else if (reminderSelection.contains("2 giờ")) {
            reminderMinutes = 120;
        }

        // Create new course
        Course course = new Course();
        course.setName(name);
        course.setRoom(room);
        course.setDayOfWeek(dayOfWeek);
        course.setStartTime(startTime);
        course.setEndTime(endTime);
        course.setStartDate(startDate);
        course.setEndDate(endDate);
        course.setWeekFrequency(weekFrequency);
        course.setNotification(notification);
        course.setReminderMinutes(reminderMinutes);

        // Save course to database
        // DatabaseHelper db = new DatabaseHelper(this);
        // db.addCourse(course);

        // Return result to calling activity
        setResult(RESULT_OK);
        finish();
    }
}
