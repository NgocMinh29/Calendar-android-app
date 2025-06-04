package com.example.calendarapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.models.Course;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private final List<Course> courseList;
    private final Context context;
    private final OnCourseListener onCourseListener;

    public interface OnCourseListener {
        void onCourseClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public CourseAdapter(Context context, List<Course> courseList, OnCourseListener onCourseListener) {
        this.context = context;
        this.courseList = courseList;
        this.onCourseListener = onCourseListener;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view, onCourseListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        holder.tvCourseName.setText(course.getName());
        holder.tvCourseRoom.setText(course.getRoom());

        String timeInfo = course.getDayOfWeek() + " | " + course.getStartTime() + " - " + course.getEndTime();
        holder.tvCourseTime.setText(timeInfo);

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        String dateRange = "Cách " + course.getWeekFrequency() + " tuần | " +
//                dateFormat.format(course.getStartDate()) + " - " +
//                dateFormat.format(course.getEndDate());
//        holder.tvCourseDateRange.setText(dateRange);
//        String dateRange = "Cách " + course.getWeekFrequency() + " tuần | " +
//                course.getStartDate() + " - " + course.getEndDate();
//        holder.tvCourseDateRange.setText(dateRange);

        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            Date startDate = input.parse(course.getStartDate());
            Date endDate = input.parse(course.getEndDate());

            String dateRange = "Cách " + course.getWeekFrequency() + " tuần | " +
                    output.format(startDate) + " - " + output.format(endDate);

            holder.tvCourseDateRange.setText(dateRange);
        } catch (Exception e) {
            holder.tvCourseDateRange.setText("Ngày học không hợp lệ");
        }



        if (course.isNotification()) {
            holder.tvCourseReminder.setText("Nhắc trước " + course.getReminderMinutes() + " phút");
            holder.tvCourseReminder.setVisibility(View.VISIBLE);
        } else {
            holder.tvCourseReminder.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            popup.inflate(R.menu.menu_course_options);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    onCourseListener.onEditClick(holder.getAdapterPosition());
                    return true;
                } else if (id == R.id.action_delete) {
                    onCourseListener.onDeleteClick(holder.getAdapterPosition());
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvCourseName, tvCourseRoom, tvCourseTime, tvCourseDateRange, tvCourseReminder;
        ImageView ivCourseIcon;
        ImageButton btnMore;
        OnCourseListener onCourseListener;

        public CourseViewHolder(@NonNull View itemView, OnCourseListener onCourseListener) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvCourseRoom = itemView.findViewById(R.id.tv_course_room);
            tvCourseTime = itemView.findViewById(R.id.tv_course_time);
            tvCourseDateRange = itemView.findViewById(R.id.tv_course_date_range);
            tvCourseReminder = itemView.findViewById(R.id.tv_course_reminder);
            ivCourseIcon = itemView.findViewById(R.id.iv_course_icon);
            btnMore = itemView.findViewById(R.id.btn_more);

            this.onCourseListener = onCourseListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCourseListener.onCourseClick(getAdapterPosition());
        }
    }
}
