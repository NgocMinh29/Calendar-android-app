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
import com.example.calendarapp.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private final List<Event> eventList;
    private final Context context;
    private final OnEventListener onEventListener;

    public interface OnEventListener {
        void onEventClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public EventAdapter(Context context, List<Event> eventList, OnEventListener onEventListener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventListener = onEventListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view, onEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//        String dateStr = dateFormat.format(event.getDate());
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = input.parse(event.getDate());
            String dateStr = output.format(date);
            holder.tvEventDateTime.setText(dateStr + " | " + event.getTime());
        } catch (Exception e) {
            holder.tvEventDateTime.setText(event.getDate() + " | " + event.getTime()); // fallback
        }


//        holder.tvEventDateTime.setText(dateStr + " | " + event.getTime());
        holder.tvEventLocation.setText(event.getLocation());

        if (event.isNotification()) {
            holder.tvEventReminder.setText("Nhắc trước " + event.getReminderMinutes() + " phút");
            holder.tvEventReminder.setVisibility(View.VISIBLE);
        } else {
            holder.tvEventReminder.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.btnMore);
            popup.inflate(R.menu.menu_event_options);
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    onEventListener.onEditClick(holder.getAdapterPosition());
                    return true;
                } else if (id == R.id.action_delete) {
                    onEventListener.onDeleteClick(holder.getAdapterPosition());
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvEventTitle, tvEventDateTime, tvEventLocation, tvEventReminder;
        ImageView ivEventIcon;
        ImageButton btnMore;
        OnEventListener onEventListener;

        public EventViewHolder(@NonNull View itemView, OnEventListener onEventListener) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDateTime = itemView.findViewById(R.id.tv_event_date_time);
            tvEventLocation = itemView.findViewById(R.id.tv_event_location);
            tvEventReminder = itemView.findViewById(R.id.tv_event_reminder);
            ivEventIcon = itemView.findViewById(R.id.iv_event_icon);
            btnMore = itemView.findViewById(R.id.btn_more);

            this.onEventListener = onEventListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onEventListener.onEventClick(getAdapterPosition());
        }
    }
}
