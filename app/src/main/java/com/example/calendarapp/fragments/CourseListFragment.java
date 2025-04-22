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
import com.example.calendarapp.activities.AddCourseActivity;
import com.example.calendarapp.activities.EditCourseActivity;
import com.example.calendarapp.adapters.CourseAdapter;
import com.example.calendarapp.models.Course;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CourseListFragment extends Fragment implements CourseAdapter.OnCourseListener {
    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;
    private FloatingActionButton fabAddCourse;

    private static final int ADD_COURSE_REQUEST = 1;
    private static final int EDIT_COURSE_REQUEST = 2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_list, container, false);

        recyclerView = view.findViewById(R.id.rv_courses);
        fabAddCourse = view.findViewById(R.id.fab_add_course);

        setupRecyclerView();
        loadSampleCourses();

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

    private void loadSampleCourses() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            // Sample courses like in the screenshot
            Course course1 = new Course(
                    1,
                    "Toán rời rạc",
                    "P.201",
                    "Thứ Ba",
                    "8:00",
                    "9:30",
                    dateFormat.parse("25/05/2024"),
                    dateFormat.parse("20/10/2025"),
                    1, // weekly
                    true,
                    15 // 15 minutes reminder
            );

            Course course2 = new Course(
                    2,
                    "Nhập môn AI",
                    "P.201",
                    "Thứ Ba",
                    "8:00",
                    "9:30",
                    dateFormat.parse("25/05/2024"),
                    dateFormat.parse("20/10/2025"),
                    1, // weekly
                    true,
                    15 // 15 minutes reminder
            );

            courseList.add(course1);
            courseList.add(course2);
            adapter.notifyDataSetChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCourseClick(int position) {
        // Handle course click
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
        courseList.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle results from add/edit activities
    }
}
