package com.example.calendarapp.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
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
import com.example.calendarapp.utils.ApiHelper;
import com.example.calendarapp.utils.NotificationHelper;

import java.text.ParseException;
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

    private Calendar selectedStartTime = Calendar.getInstance();
    private Calendar selectedEndTime = Calendar.getInstance();
    private Calendar selectedStartDate = Calendar.getInstance();
    private Calendar selectedEndDate = Calendar.getInstance();

    private ApiHelper apiHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        apiHelper = new ApiHelper(this);

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

        setupSpinners();
        setupDateTimePickers();
        setupButtons();
    }

    private void setupSpinners() {
        // Day of week spinner
        String[] days = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(dayAdapter);

        // Week frequency spinner
        String[] frequencies = {"Hàng tuần", "Cách 1 tuần", "Cách 2 tuần", "Cách 3 tuần", "Cách 4 tuần"};
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
        selectedStartTime.set(Calendar.HOUR_OF_DAY, 7);
        selectedStartTime.set(Calendar.MINUTE, 30);
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
        selectedEndTime.set(Calendar.HOUR_OF_DAY, 9);
        selectedEndTime.set(Calendar.MINUTE, 0);
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
        // Set default end date to 4 months from now
        selectedEndDate.add(Calendar.MONTH, 4);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        String startTime = timeFormat.format(selectedStartTime.getTime());
        String endTime = timeFormat.format(selectedEndTime.getTime());
        String startDate = dateFormat.format(selectedStartDate.getTime());
        String endDate = dateFormat.format(selectedEndDate.getTime());

        // Get week frequency from spinner selection
        int weekFrequency = 1; // Default to weekly
        String frequencySelection = spinnerFrequency.getSelectedItem().toString();
        if (frequencySelection.contains("1")) {
            weekFrequency = 2; // Every 2 weeks
        } else if (frequencySelection.contains("2")) {
            weekFrequency = 3; // Every 3 weeks
        } else if (frequencySelection.contains("3")) {
            weekFrequency = 4; // Every 4 weeks
        } else if (frequencySelection.contains("4")) {
            weekFrequency = 5; // Every 5 weeks
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

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang thêm môn học...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // 🔔 Đặt lịch thông báo sau khi Course được tạo hoặc cập nhật
        if (course.isNotification()) {
            Calendar cal = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = sdf.parse(course.getStartDate() + " " + course.getStartTime());
                cal.setTime(date);
                cal.add(Calendar.MINUTE, -course.getReminderMinutes());
                NotificationHelper.cancelReminder(AddCourseActivity.this, 2000 + (int) course.getId());
                NotificationHelper.scheduleReminder(AddCourseActivity.this, 2000 + (int) course.getId(), cal, course.getName());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        // Save to server
        apiHelper.createCourse(course, new ApiHelper.ApiCallback<Course>() {
            @Override
            public void onSuccess(Course data) {
                progressDialog.dismiss();
                Toast.makeText(AddCourseActivity.this, "Đã thêm môn học", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(AddCourseActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
