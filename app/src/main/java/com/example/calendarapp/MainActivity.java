package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.fragments.CalendarFragment;
import com.example.calendarapp.fragments.CourseListFragment;
import com.example.calendarapp.fragments.EventListFragment;
import com.example.calendarapp.fragments.SettingsFragment;
import com.example.calendarapp.login.LoginActivity;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.DatabaseHelper;
import com.example.calendarapp.models.Event;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Kiểm tra trạng thái đăng nhập
        if (!sessionManager.isLoggedIn() && !sessionManager.isGuest()) {
            // Nếu chưa đăng nhập và không phải khách, chuyển đến màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Thêm dữ liệu mẫu nếu cần
        addSampleData();

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        }
    }

    private void addSampleData() {
        // Kiểm tra xem đã có dữ liệu chưa
        if (databaseHelper.getAllEvents().size() == 0 && databaseHelper.getAllCourses().size() == 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                // Thêm sự kiện mẫu
                Event event = new Event();
                event.setTitle("Thi giữa kỳ CSDL");
                event.setNote("Mang máy tính + máy in");
                event.setDate(dateFormat.parse("15/04/2025"));
                event.setTime("13:00");
                event.setLocation("Phòng máy tính");
                event.setNotification(true);
                event.setReminderMinutes(60);
                databaseHelper.addEvent(event);

                // Thêm môn học mẫu
                Course course1 = new Course();
                course1.setName("Toán rời rạc");
                course1.setRoom("P.201");
                course1.setDayOfWeek("Thứ Ba");
                course1.setStartTime("8:00");
                course1.setEndTime("9:30");
                course1.setStartDate(dateFormat.parse("25/05/2024"));
                course1.setEndDate(dateFormat.parse("20/10/2025"));
                course1.setWeekFrequency(1);
                course1.setNotification(true);
                course1.setReminderMinutes(15);
                databaseHelper.addCourse(course1);

                Course course2 = new Course();
                course2.setName("Nhập môn AI");
                course2.setRoom("P.312");
                course2.setDayOfWeek("Thứ Tư");
                course2.setStartTime("13:00");
                course2.setEndTime("17:00");
                course2.setStartDate(dateFormat.parse("25/05/2024"));
                course2.setEndDate(dateFormat.parse("20/10/2025"));
                course2.setWeekFrequency(1);
                course2.setNotification(true);
                course2.setReminderMinutes(15);
                databaseHelper.addCourse(course2);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        String title = "";

        int itemId = item.getItemId();
        if (itemId == R.id.nav_calendar) {
            selectedFragment = new CalendarFragment();
            title = "Thời khóa biểu";
        } else if (itemId == R.id.nav_courses) {
            selectedFragment = new CourseListFragment();
            title = "Danh sách môn học";
        } else if (itemId == R.id.nav_events) {
            selectedFragment = new EventListFragment();
            title = "Danh sách sự kiện";
        } else if (itemId == R.id.nav_settings) {
            selectedFragment = new SettingsFragment();
            title = "Cài đặt";
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            // Set title in toolbar
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }

            return true;
        }

        return false;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}