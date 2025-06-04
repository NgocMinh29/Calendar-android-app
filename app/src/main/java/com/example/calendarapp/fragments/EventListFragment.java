package com.example.calendarapp.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.example.calendarapp.utils.ApiHelper;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class EventListFragment extends Fragment implements EventAdapter.OnEventListener {
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private FloatingActionButton fabAddEvent;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private static final int ADD_EVENT_REQUEST = 1;
    private static final int EDIT_EVENT_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        apiHelper = new ApiHelper(getContext());
        sessionManager = new SessionManager(getContext());

        recyclerView = view.findViewById(R.id.rv_events);
        fabAddEvent = view.findViewById(R.id.fab_add_event);

        setupRecyclerView();
        loadEvents();

        fabAddEvent.setOnClickListener(v -> {
            if (sessionManager.canPerformApiOperations()) {
                Intent intent = new Intent(getActivity(), AddEventActivity.class);
                startActivityForResult(intent, ADD_EVENT_REQUEST);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm sự kiện", Toast.LENGTH_SHORT).show();
            }
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
        if (!sessionManager.canPerformApiOperations()) {
            // Hiển thị thông báo cho người dùng khách
            if (sessionManager.isGuest()) {
                Toast.makeText(getContext(), "Chế độ khách - không có dữ liệu", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        showProgressDialog("Đang tải sự kiện...");

        apiHelper.getAllEvents(new ApiHelper.ApiCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> data) {
                hideProgressDialog();
                eventList.clear();
                eventList.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Lỗi tải sự kiện: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEventClick(int position) {
        // Xử lý khi nhấn vào sự kiện
        Event event = eventList.get(position);
        Intent intent = new Intent(getActivity(), EditEventActivity.class);
        intent.putExtra("EVENT", event);
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    @Override
    public void onEditClick(int position) {
        if (!sessionManager.canPerformApiOperations()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = eventList.get(position);
        Intent intent = new Intent(getActivity(), EditEventActivity.class);
        intent.putExtra("EVENT", event);
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    @Override
    public void onDeleteClick(int position) {
        if (!sessionManager.canPerformApiOperations()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = eventList.get(position);

        // Hiển thị dialog xác nhận xóa
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa sự kiện này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteEvent(event, position);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteEvent(Event event, int position) {
        showProgressDialog("Đang xóa sự kiện...");

        apiHelper.deleteEvent(event.getId(), new ApiHelper.ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                hideProgressDialog();
                eventList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa sự kiện", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Lỗi xóa sự kiện: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            // Cập nhật lại danh sách sau khi thêm/sửa
            loadEvents();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại fragment
        loadEvents();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}
