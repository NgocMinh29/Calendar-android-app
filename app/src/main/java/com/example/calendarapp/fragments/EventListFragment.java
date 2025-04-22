package com.example.calendarapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.activities.AddEventActivity;
import com.example.calendarapp.activities.EditEventActivity;
import com.example.calendarapp.adapters.EventAdapter;
import com.example.calendarapp.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventListFragment extends Fragment implements EventAdapter.OnEventListener {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FloatingActionButton fabAddEvent;

    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.rv_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        setupRecyclerView();
        loadSampleEvents();

        fabAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddEventActivity.class);
            startActivityForResult(intent, ADD_EVENT_REQUEST);
        });

        return view;
    }

    private void setupRecyclerView() {
        eventList = new ArrayList<>();
        adapter = new EventAdapter(getContext(), eventList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadSampleEvents() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            // Sample event like in the screenshot
            Event event = new Event(
                    1,
                    "Thi giữa kỳ CSDL",
                    "Mang máy tính + máy in",
                    dateFormat.parse("15/04/2025"),
                    "13:00",
                    true,
                    60, // 1 hour reminder
                    "Phòng máy tính"
            );
            eventList.add(event);
            adapter.notifyDataSetChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEventClick(int position) {
        // Handle event click
    }

    @Override
    public void onEditClick(int position) {
        Event event = eventList.get(position);
        Intent intent = new Intent(getActivity(), EditEventActivity.class);
        intent.putExtra("EVENT", event);
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    @Override
    public void onDeleteClick(int position) {
        eventList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle results from add/edit activities
    }
}
