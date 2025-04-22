package com.example.calendarapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.calendarapp.fragments.CalendarFragment;
import com.example.calendarapp.fragments.CourseListFragment;
import com.example.calendarapp.fragments.EventListFragment;
import com.example.calendarapp.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
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
}
