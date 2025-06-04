package com.example.calendarapp.ai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.adapters.ConversationsAdapter;
import com.example.calendarapp.models.ChatConversation;
import com.example.calendarapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ConversationsActivity extends AppCompatActivity implements ConversationsAdapter.OnConversationClickListener {
    private static final String TAG = "ConversationsActivity";

    private RecyclerView rvConversations;
    private LinearLayout tvEmptyState;
    private FloatingActionButton fabNewChat;

    private ConversationsAdapter adapter;
    private List<ChatConversation> conversations;
    private ChatRepository chatRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversations);

        sessionManager = new SessionManager(this);
        chatRepository = new ChatRepository(this);

        // Kiểm tra quyền Premium
        if (!sessionManager.isPremium()) {
            Toast.makeText(this, "Tính năng này chỉ dành cho người dùng Premium", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadConversations();
    }

    private void initViews() {
        rvConversations = findViewById(R.id.rv_conversations);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        fabNewChat = findViewById(R.id.fab_new_chat);

        fabNewChat.setOnClickListener(v -> startNewChat());

        // Thêm back button listener
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        conversations = new ArrayList<>();
        adapter = new ConversationsAdapter(this, conversations, this);
        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        rvConversations.setAdapter(adapter);
    }

    private void loadConversations() {
        chatRepository.getConversations(new ChatRepository.ConversationsCallback() {
            @Override
            public void onSuccess(List<ChatConversation> conversationList) {
                runOnUiThread(() -> {
                    conversations.clear();
                    if (conversationList != null && !conversationList.isEmpty()) {
                        conversations.addAll(conversationList);
                        rvConversations.setVisibility(View.VISIBLE);
                        tvEmptyState.setVisibility(View.GONE);
                    } else {
                        rvConversations.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ConversationsActivity.this, "Lỗi khi tải lịch sử: " + error, Toast.LENGTH_SHORT).show();
                    rvConversations.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void startNewChat() {
        Intent intent = new Intent(this, AIChatActivity.class);
        intent.putExtra("new_chat", true); // THÊM FLAG MỚI
        intent.putExtra("from_conversations", true);
        startActivity(intent);
    }

    @Override
    public void onConversationClick(ChatConversation conversation) {
        Intent intent = new Intent(this, AIChatActivity.class);
        intent.putExtra("conversation_id", conversation.getId());
        intent.putExtra("conversation_title", conversation.getTitle());
        intent.putExtra("from_conversations", true);
        startActivity(intent);
    }

    @Override
    public void onConversationDelete(ChatConversation conversation) {
        // Xóa cuộc trò chuyện
        chatRepository.deleteChatHistory(conversation.getId(), new retrofit2.Callback<com.example.calendarapp.models.ApiResponse<String>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.calendarapp.models.ApiResponse<String>> call, retrofit2.Response<com.example.calendarapp.models.ApiResponse<String>> response) {
                runOnUiThread(() -> {
                    // Xóa khỏi danh sách
                    conversations.remove(conversation);
                    adapter.notifyDataSetChanged();

                    // Kiểm tra xem còn cuộc trò chuyện nào không
                    if (conversations.isEmpty()) {
                        rvConversations.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                    }

                    Toast.makeText(ConversationsActivity.this, "Đã xóa cuộc trò chuyện", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.calendarapp.models.ApiResponse<String>> call, Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(ConversationsActivity.this, "Lỗi khi xóa: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại danh sách khi quay lại activity
        loadConversations();
    }
}
