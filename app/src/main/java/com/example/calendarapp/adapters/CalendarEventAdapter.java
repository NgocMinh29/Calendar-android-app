package com.example.calendarapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.models.Course;
import com.example.calendarapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CalendarEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EVENT = 0;
    private static final int TYPE_COURSE = 1;

    private final List<Object> items;
    private final Context context;
    private final OnCalendarItemListener listener;

    public interface OnCalendarItemListener {
        void onCourseClick(Course course);
        void onEventClick(Event event);
    }

    public CalendarEventAdapter(Context context, List<Object> items, OnCalendarItemListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Event) {
            return TYPE_EVENT;
        } else {
            return TYPE_COURSE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_EVENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
            return new EventViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_course, parent, false);
            return new CourseViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_EVENT) {
            EventViewHolder eventHolder = (EventViewHolder) holder;
            Event event = (Event) items.get(position);

            eventHolder.tvTitle.setText(event.getTitle());
            eventHolder.tvTime.setText(event.getTime());
            eventHolder.tvLocation.setText(event.getLocation());

            if (event.isNotification()) {
                eventHolder.tvReminder.setText("Nhắc trước " + event.getReminderMinutes() + " phút");
                eventHolder.tvReminder.setVisibility(View.VISIBLE);
            } else {
                eventHolder.tvReminder.setVisibility(View.GONE);
            }

            eventHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEventClick(event);
                }
            });
        } else {
            CourseViewHolder courseHolder = (CourseViewHolder) holder;
            Course course = (Course) items.get(position);

            courseHolder.tvTitle.setText(course.getName());
            courseHolder.tvTime.setText(course.getStartTime() + " - " + course.getEndTime());
            courseHolder.tvRoom.setText(course.getRoom());

            if (course.isNotification()) {
                courseHolder.tvReminder.setText("Nhắc trước " + course.getReminderMinutes() + " phút");
                courseHolder.tvReminder.setVisibility(View.VISIBLE);
            } else {
                courseHolder.tvReminder.setVisibility(View.GONE);
            }

            courseHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCourseClick(course);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvLocation, tvReminder;
        ImageView ivIcon;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_event_title);
            tvTime = itemView.findViewById(R.id.tv_event_time);
            tvLocation = itemView.findViewById(R.id.tv_event_location);
            tvReminder = itemView.findViewById(R.id.tv_event_reminder);
            ivIcon = itemView.findViewById(R.id.iv_event_icon);
        }
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTime, tvRoom, tvReminder;
        ImageView ivIcon;

        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_course_title);
            tvTime = itemView.findViewById(R.id.tv_course_time);
            tvRoom = itemView.findViewById(R.id.tv_course_room);
            tvReminder = itemView.findViewById(R.id.tv_course_reminder);
            ivIcon = itemView.findViewById(R.id.iv_course_icon);
        }
    }
}
