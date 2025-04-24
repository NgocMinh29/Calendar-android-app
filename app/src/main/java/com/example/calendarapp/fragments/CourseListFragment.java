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
import com.example.calendarapp.activities.AddCourseActivity;
import com.example.calendarapp.activities.EditCourseActivity;
import com.example.calendarapp.adapters.CourseAdapter;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseListFragment extends Fragment implements CourseAdapter.OnCourseListener {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;
    private DatabaseHelper databaseHelper;

    private static final int ADD_COURSE_REQUEST = 1;
    private static final int EDIT_COURSE_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);

        databaseHelper = ((MainActivity) getActivity()).getDatabaseHelper();

        recyclerView = view.findViewById(R.id.rv_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);

        setupRecyclerView();
        loadCourses();

        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddCourseActivity.class);
            startActivityForResult(intent, ADD_COURSE_REQUEST);
        });

        return view;
    }

    private void setupRecyclerView() {
        courseList = new ArrayList<>();
        adapter = new CourseAdapter(getContext(), courseList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadCourses() {
        courseList.clear();
        courseList.addAll(databaseHelper.getAllCourses());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCourseClick(int position) {
        // Xử lý khi nhấn vào môn học
    }

    @Override
    public void onEditClick(int position) {
        Course course = courseList.get(position);
        Intent intent = new Intent(getActivity(), EditCourseActivity.class);
        intent.putExtra("COURSE", course);
        startActivityForResult(intent, EDIT_COURSE_REQUEST);
    }

    @Override
    public void onDeleteClick(int position) {
        Course course = courseList.get(position);

        // Hiển thị dialog xác nhận xóa
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa môn học này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            databaseHelper.deleteCourse(course);
            courseList.remove(position);
            adapter.notifyItemRemoved(position);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Cập nhật lại danh sách sau khi thêm/sửa
        loadCourses();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại fragment
        loadCourses();
    }
}
