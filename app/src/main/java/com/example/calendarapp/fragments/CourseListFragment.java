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
import com.example.calendarapp.activities.AddCourseActivity;
import com.example.calendarapp.activities.EditCourseActivity;
import com.example.calendarapp.adapters.CourseAdapter;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.utils.ApiHelper;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CourseListFragment extends Fragment implements CourseAdapter.OnCourseListener {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;
    private ApiHelper apiHelper;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;

    private static final int ADD_COURSE_REQUEST = 1;
    private static final int EDIT_COURSE_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);

        apiHelper = new ApiHelper(getContext());
        sessionManager = new SessionManager(getContext());

        recyclerView = view.findViewById(R.id.rv_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);

        setupRecyclerView();
        loadCourses();

        fabAddCourse.setOnClickListener(v -> {
            if (sessionManager.canPerformApiOperations()) {
                Intent intent = new Intent(getActivity(), AddCourseActivity.class);
                startActivityForResult(intent, ADD_COURSE_REQUEST);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm môn học", Toast.LENGTH_SHORT).show();
            }
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
        if (!sessionManager.canPerformApiOperations()) {
            // Hiển thị thông báo cho người dùng khách
            if (sessionManager.isGuest()) {
                Toast.makeText(getContext(), "Chế độ khách - không có dữ liệu", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        showProgressDialog("Đang tải môn học...");

        apiHelper.getAllCourses(new ApiHelper.ApiCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                hideProgressDialog();
                courseList.clear();
                courseList.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Lỗi tải môn học: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCourseClick(int position) {
        // Xử lý khi nhấn vào môn học
        Course course = courseList.get(position);
        Intent intent = new Intent(getActivity(), EditCourseActivity.class);
        intent.putExtra("COURSE", course);
        startActivityForResult(intent, EDIT_COURSE_REQUEST);
    }

    @Override
    public void onEditClick(int position) {
        if (!sessionManager.canPerformApiOperations()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        Course course = courseList.get(position);
        Intent intent = new Intent(getActivity(), EditCourseActivity.class);
        intent.putExtra("COURSE", course);
        startActivityForResult(intent, EDIT_COURSE_REQUEST);
    }

    @Override
    public void onDeleteClick(int position) {
        if (!sessionManager.canPerformApiOperations()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        Course course = courseList.get(position);

        // Hiển thị dialog xác nhận xóa
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa môn học này?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            deleteCourse(course, position);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void deleteCourse(Course course, int position) {
        showProgressDialog("Đang xóa môn học...");

        apiHelper.deleteCourse(course.getId(), new ApiHelper.ApiCallback<String>() {
            @Override
            public void onSuccess(String data) {
                hideProgressDialog();
                courseList.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(getContext(), "Đã xóa môn học", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                hideProgressDialog();
                Toast.makeText(getContext(), "Lỗi xóa môn học: " + error, Toast.LENGTH_SHORT).show();
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
            loadCourses();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại danh sách khi quay lại fragment
        loadCourses();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
    }
}
