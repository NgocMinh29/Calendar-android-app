package com.example.calendarapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {
    private TextView tvMonth;
    private RecyclerView rvCalendarItems;
    private FloatingActionButton fabAddCalendarItem;
    private List<Object> calendarItems; // Can contain both Course and Event objects

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        tvMonth = view.findViewById(R.id.tv_month);
        rvCalendarItems = view.findViewById(R.id.rv_calendar_items);
        fabAddCalendarItem = view.findViewById(R.id.fab_add_calendar_item);

        setupCalendarView();
        loadCalendarItems();

        return view;
    }

    private void setupCalendarView() {
        // Set current month
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", new Locale("vi", "VN"));
        tvMonth.setText("Tháng " + (calendar.get(Calendar.MONTH) + 1));

        // Setup RecyclerView for calendar items
        calendarItems = new ArrayList<>();
        rvCalendarItems.setLayoutManager(new LinearLayoutManager(getContext()));

        // Custom adapter would be needed here to handle both Course and Event types
    }

    private void loadCalendarItems() {
        // Load courses and events for the selected date/week
        // This would typically come from a database or shared data source
    }
}
