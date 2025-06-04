package com.example.calendarapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.models.ChatConversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {

    private Context context;
    private List<ChatConversation> conversations;
    private OnConversationClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnConversationClickListener {
        void onConversationClick(ChatConversation conversation);
        void onConversationDelete(ChatConversation conversation);
    }

    public ConversationsAdapter(Context context, List<ChatConversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversations = conversations;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        ChatConversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDate, tvPreview;
        ImageButton btnDelete;

        ConversationViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_conversation_title);
            tvDate = itemView.findViewById(R.id.tv_conversation_date);
            tvPreview = itemView.findViewById(R.id.tv_conversation_preview);
            btnDelete = itemView.findViewById(R.id.btn_delete_conversation);
        }

        void bind(ChatConversation conversation) {
            tvTitle.setText(conversation.getTitle());

            // Hiển thị ngày tạo
            try {
                if (conversation.getUpdatedAt() != null) {
                    // Giả sử updatedAt có format "yyyy-MM-dd HH:mm:ss"
                    String dateStr = conversation.getUpdatedAt();
                    if (dateStr.length() > 16) {
                        dateStr = dateStr.substring(0, 16).replace(" ", " ");
                    }
                    tvDate.setText(dateStr);
                } else {
                    tvDate.setText("Vừa xong");
                }
            } catch (Exception e) {
                tvDate.setText("Vừa xong");
            }

            // Hiển thị preview (có thể là tin nhắn đầu tiên)
            String preview = "Cuộc trò chuyện với AI";
            if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
                String firstMessage = conversation.getMessages().get(0).getMessage();
                if (firstMessage.length() > 50) {
                    preview = firstMessage.substring(0, 50) + "...";
                } else {
                    preview = firstMessage;
                }
            }
            tvPreview.setText(preview);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationClick(conversation);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onConversationDelete(conversation);
                }
            });
        }
    }
}
