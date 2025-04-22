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

import java.util.List;

public class CalendarEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_COURSE = 0;
    private static final int TYPE_EVENT = 1;

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
        if (items.get(position) instanceof Course) {
            return TYPE_COURSE;
        } else {
            return TYPE_EVENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_COURSE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_course, parent, false);
            return new CourseViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_COURSE) {
            Course course = (Course) items.get(position);
            CourseViewHolder courseHolder = (CourseViewHolder) holder;

            courseHolder.tvCourseName.setText(course.getName());
            courseHolder.tvCourseTime.setText(course.getStartTime() + " - " + course.getEndTime());
            courseHolder.tvCourseRoom.setText(course.getRoom());

            courseHolder.itemView.setOnClickListener(v -> listener.onCourseClick(course));
        } else {
            Event event = (Event) items.get(position);
            EventViewHolder eventHolder = (EventViewHolder) holder;

            eventHolder.tvEventTitle.setText(event.getTitle());
            eventHolder.tvEventTime.setText(event.getTime());
            eventHolder.tvEventLocation.setText(event.getLocation());

            eventHolder.itemView.setOnClickListener(v -> listener.onEventClick(event));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourseName, tvCourseTime, tvCourseRoom;
        ImageView ivCourseIcon;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourseName = itemView.findViewById(R.id.tv_course_name);
            tvCourseTime = itemView.findViewById(R.id.tv_course_time);
            tvCourseRoom = itemView.findViewById(R.id.tv_course_room);
            ivCourseIcon = itemView.findViewById(R.id.iv_course_icon);
        }
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvEventTime, tvEventLocation;
        ImageView ivEventIcon;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventTime = itemView.findViewById(R.id.tv_event_time);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            ivEventIcon = itemView.findViewById(R.id.iv_event_icon);
        }
    }
}
