package com.example.calendarapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;
    private static final int VIEW_TYPE_TYPING = 3;

    private Context context;
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;

    public ChatMessageAdapter(Context context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if (message.isTyping()) {
            return VIEW_TYPE_TYPING;
        } else if (message.isFromUser()) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_AI;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case VIEW_TYPE_USER:
                View userView = inflater.inflate(R.layout.item_chat_user, parent, false);
                return new UserMessageViewHolder(userView);
            case VIEW_TYPE_AI:
                View aiView = inflater.inflate(R.layout.item_chat_ai, parent, false);
                return new AIMessageViewHolder(aiView);
            case VIEW_TYPE_TYPING:
                View typingView = inflater.inflate(R.layout.item_chat_typing, parent, false);
                return new TypingViewHolder(typingView);
            default:
                View defaultView = inflater.inflate(R.layout.item_chat_ai, parent, false);
                return new AIMessageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AIMessageViewHolder) {
            ((AIMessageViewHolder) holder).bind(message);
        } else if (holder instanceof TypingViewHolder) {
            ((TypingViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void removeTypingMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).isTyping()) {
                messages.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void updateLastMessage(String newText) {
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            lastMessage.setMessage(newText);
            notifyItemChanged(messages.size() - 1);
        }
    }

    class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    class AIMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        AIMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
            tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        }
    }

    class TypingViewHolder extends RecyclerView.ViewHolder {
        TypingViewHolder(View itemView) {
            super(itemView);
        }

        void bind(ChatMessage message) {
            // Animation sẽ được handle trong layout
        }
    }
}
