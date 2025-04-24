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

import com.example.calendarapp.MainActivity;
import com.example.calendarapp.R;
import com.example.calendarapp.activities.AddEventActivity;
import com.example.calendarapp.activities.EditEventActivity;
import com.example.calendarapp.adapters.EventAdapter;
import com.example.calendarapp.models.DatabaseHelper;
import com.example.calendarapp.models.Event;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment implements EventAdapter.OnEventListener {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FloatingActionButton fabAddEvent;
    private DatabaseHelper databaseHelper;

    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        databaseHelper = ((MainActivity) getActivity()).getDatabaseHelper();

        recyclerView = view.findViewById(R.id.rv_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        setupRecyclerView();
        loadEvents();

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

    private void loadEvents() {
        eventList.clear();
        eventList.addAll(databaseHelper.getAllEvents());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEventClick(int position) {
        // Xử lý khi nhấn vào sự kiện
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
        Event event = eventList.get(position);

        // Hiển thị dialog xác nhận xóa
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa sự kiện này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            databaseHelper.deleteEvent(event);
            eventList.remove(position);
            adapter.notifyItemRemoved(position);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Cập nhật lại danh sách sau khi thêm/sửa
        loadEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại fragment
        loadEvents();
    }
}
