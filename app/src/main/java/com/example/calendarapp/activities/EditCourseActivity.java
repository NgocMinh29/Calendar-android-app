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

public class EditCourseActivity extends AppCompatActivity {
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

    private Course course;
    private ApiHelper apiHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        // Get course from intent
        course = (Course) getIntent().getSerializableExtra("COURSE");
        if (course == null) {
            finish();
            return;
        }

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
        loadCourseData();
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
    }

    private void loadCourseData() {
        etCourseName.setText(course.getName());
        etRoom.setText(course.getRoom());

        // Set day of week spinner
        String dayOfWeek = course.getDayOfWeek();
        String[] days = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        for (int i = 0; i < days.length; i++) {
            if (days[i].equals(dayOfWeek)) {
                spinnerDay.setSelection(i);
                break;
            }
        }

        // Set time
        String[] startTimeParts = course.getStartTime().split(":");
        if (startTimeParts.length == 2) {
            try {
                int hour = Integer.parseInt(startTimeParts[0]);
                int minute = Integer.parseInt(startTimeParts[1]);
                selectedStartTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedStartTime.set(Calendar.MINUTE, minute);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        String[] endTimeParts = course.getEndTime().split(":");
        if (endTimeParts.length == 2) {
            try {
                int hour = Integer.parseInt(endTimeParts[0]);
                int minute = Integer.parseInt(endTimeParts[1]);
                selectedEndTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedEndTime.set(Calendar.MINUTE, minute);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Set dates from string format yyyy-MM-dd
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDate = apiDateFormat.parse(course.getStartDate());
            selectedStartDate.setTime(startDate);

            Date endDate = apiDateFormat.parse(course.getEndDate());
            selectedEndDate.setTime(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set week frequency spinner
        int weekFrequency = course.getWeekFrequency();
        int spinnerPosition = 0; // Default to weekly
        if (weekFrequency == 2) {
            spinnerPosition = 1; // Every 2 weeks
        } else if (weekFrequency == 3) {
            spinnerPosition = 2; // Every 3 weeks
        } else if (weekFrequency == 4) {
            spinnerPosition = 3; // Every 4 weeks
        } else if (weekFrequency == 5) {
            spinnerPosition = 4; // Every 5 weeks
        }
        spinnerFrequency.setSelection(spinnerPosition);

        // Set notification switch
        switchNotification.setChecked(course.isNotification());

        // Set reminder spinner
        int reminderMinutes = course.getReminderMinutes();
        int reminderPosition = 2; // Default to 15 minutes
        if (reminderMinutes == 5) {
            reminderPosition = 0;
        } else if (reminderMinutes == 10) {
            reminderPosition = 1;
        } else if (reminderMinutes == 15) {
            reminderPosition = 2;
        } else if (reminderMinutes == 30) {
            reminderPosition = 3;
        } else if (reminderMinutes == 60) {
            reminderPosition = 4;
        } else if (reminderMinutes == 120) {
            reminderPosition = 5;
        }
        spinnerReminderTime.setSelection(reminderPosition);

        updateStartTimeDisplay();
        updateEndTimeDisplay();
        updateStartDateDisplay();
        updateEndDateDisplay();
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
                updateCourse();
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

    private void updateCourse() {
        course.setName(etCourseName.getText().toString().trim());
        course.setRoom(etRoom.getText().toString().trim());
        course.setDayOfWeek(spinnerDay.getSelectedItem().toString());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        course.setStartTime(timeFormat.format(selectedStartTime.getTime()));
        course.setEndTime(timeFormat.format(selectedEndTime.getTime()));
        course.setStartDate(apiDateFormat.format(selectedStartDate.getTime()));
        course.setEndDate(apiDateFormat.format(selectedEndDate.getTime()));

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
        course.setWeekFrequency(weekFrequency);

        course.setNotification(switchNotification.isChecked());

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
        course.setReminderMinutes(reminderMinutes);

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật môn học...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (course.isNotification()) {
            Calendar cal = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date = sdf.parse(course.getStartDate() + " " + course.getStartTime());
                cal.setTime(date);
                cal.add(Calendar.MINUTE, -course.getReminderMinutes());
                NotificationHelper.cancelReminder(EditCourseActivity.this, 2000 + (int) course.getId());
                NotificationHelper.scheduleReminder(EditCourseActivity.this, 2000 + (int) course.getId(), cal, course.getName());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        // Update on server
        apiHelper.updateCourse(course, new ApiHelper.ApiCallback<Course>() {
            @Override
            public void onSuccess(Course data) {
                progressDialog.dismiss();
                Toast.makeText(EditCourseActivity.this, "Đã cập nhật môn học", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(EditCourseActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
