package com.example.calendarapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.login.LoginActivity;
import com.example.calendarapp.fragments.CalendarFragment;
import com.example.calendarapp.fragments.CourseListFragment;
import com.example.calendarapp.fragments.EventListFragment;
import com.example.calendarapp.fragments.SettingsFragment;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo SessionManager
        sessionManager = new SessionManager(this);

        // Kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn()) {
            // Chuyển đến màn hình đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        }

        // Hiển thị thông báo chào mừng
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            Toast.makeText(this, "Chào mừng " + userName + "!", Toast.LENGTH_SHORT).show();
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

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Kiểm tra lại session khi quay lại activity
        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
