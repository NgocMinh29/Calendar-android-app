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
import com.example.calendarapp.models.Event;
import com.example.calendarapp.utils.ApiHelper;
import com.example.calendarapp.utils.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditEventActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText etEventTitle;
    private EditText etEventNote;
    private TextView tvTimeSelector;
    private TextView tvDateSelector;
    private Switch switchNotification;
    private Spinner spinnerReminderTime;
    private Button btnCancel;
    private Button btnSave;

    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private Event event;
    private ApiHelper apiHelper;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Get event from intent
        event = (Event) getIntent().getSerializableExtra("EVENT");
        if (event == null) {
            finish();
            return;
        }

        apiHelper = new ApiHelper(this);

        btnBack = findViewById(R.id.btn_back);
        etEventTitle = findViewById(R.id.et_event_title);
        etEventNote = findViewById(R.id.et_event_note);
        tvTimeSelector = findViewById(R.id.tv_time_selector);
        tvDateSelector = findViewById(R.id.tv_date_selector);
        switchNotification = findViewById(R.id.switch_notification);
        spinnerReminderTime = findViewById(R.id.spinner_reminder_time);
        btnCancel = findViewById(R.id.btn_cancel);
        btnSave = findViewById(R.id.btn_save);

        setupReminderSpinner();
        loadEventData();
        setupDateTimePickers();
        setupButtons();
    }

    private void setupReminderSpinner() {
        String[] reminderTimes = {"5 phút", "10 phút", "15 phút", "30 phút", "1 giờ", "2 giờ"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reminderTimes);
        spinnerReminderTime.setAdapter(adapter);
    }

    private void loadEventData() {
        etEventTitle.setText(event.getTitle());
        etEventNote.setText(event.getNote());

        // Set date from string format yyyy-MM-dd
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date eventDate = apiDateFormat.parse(event.getDate());
            selectedDate.setTime(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set time
        String[] timeParts = event.getTime().split(":");
        if (timeParts.length == 2) {
            try {
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Set notification switch
        switchNotification.setChecked(event.isNotification());

        // Set reminder spinner
        int reminderMinutes = event.getReminderMinutes();
        int spinnerPosition = 2; // Default to 15 minutes
        if (reminderMinutes == 5) {
            spinnerPosition = 0;
        } else if (reminderMinutes == 10) {
            spinnerPosition = 1;
        } else if (reminderMinutes == 15) {
            spinnerPosition = 2;
        } else if (reminderMinutes == 30) {
            spinnerPosition = 3;
        } else if (reminderMinutes == 60) {
            spinnerPosition = 4;
        } else if (reminderMinutes == 120) {
            spinnerPosition = 5;
        }
        spinnerReminderTime.setSelection(spinnerPosition);

        updateTimeDisplay();
        updateDateDisplay();
    }

    private void setupDateTimePickers() {
        // Setup time picker
        tvTimeSelector.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view, hourOfDay, minute) -> {
                        selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedTime.set(Calendar.MINUTE, minute);
                        updateTimeDisplay();
                    },
                    selectedTime.get(Calendar.HOUR_OF_DAY),
                    selectedTime.get(Calendar.MINUTE),
                    true
            );
            timePickerDialog.show();
        });

        // Setup date picker
        tvDateSelector.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(Calendar.YEAR, year);
                        selectedDate.set(Calendar.MONTH, month);
                        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void updateTimeDisplay() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTimeSelector.setText(timeFormat.format(selectedTime.getTime()));
    }

    private void updateDateDisplay() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvDateSelector.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                updateEvent();
            }
        });
    }

    private boolean validateInput() {
        if (etEventTitle.getText().toString().trim().isEmpty()) {
            etEventTitle.setError("Vui lòng nhập tên sự kiện");
            return false;
        }
        return true;
    }

    private void updateEvent() {
        event.setTitle(etEventTitle.getText().toString().trim());
        event.setNote(etEventNote.getText().toString().trim());
//        event.setUserEmail(currentUserEmail); // currentUserEmail lấy từ session, SharedPreferences

        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        event.setDate(apiDateFormat.format(selectedDate.getTime()));
        event.setTime(timeFormat.format(selectedTime.getTime()));
        event.setNotification(switchNotification.isChecked());

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
        event.setReminderMinutes(reminderMinutes);

        // Show progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật sự kiện...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (event.isNotification()) {
            Calendar cal = Calendar.getInstance();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                Date date1 = sdf.parse(event.getDate() + " " + event.getTime());
                cal.setTime(date1);
                cal.add(Calendar.MINUTE, -event.getReminderMinutes());
                NotificationHelper.cancelReminder(this, 1000 + (int) event.getId());
                NotificationHelper.scheduleReminder(this, 1000 + (int) event.getId(), cal, event.getTitle());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Update on server
        apiHelper.updateEvent(event, new ApiHelper.ApiCallback<Event>() {
            @Override
            public void onSuccess(Event data) {
                progressDialog.dismiss();
                Toast.makeText(EditEventActivity.this, "Đã cập nhật sự kiện", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(EditEventActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
