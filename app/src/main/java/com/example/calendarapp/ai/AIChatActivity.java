package com.example.calendarapp.ai;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapp.R;
import com.example.calendarapp.adapters.ChatMessageAdapter;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.ChatConversation;
import com.example.calendarapp.models.ChatMessage;
import com.example.calendarapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity implements GeminiAIManager.AIResponseCallback {
    private static final String TAG = "AIChatActivity";

    private RecyclerView rvChatMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private TextView tvTitle;

    private ChatMessageAdapter adapter;
    private List<ChatMessage> messages;
    private GeminiAIManager aiManager;
    private SessionManager sessionManager;
    private ChatRepository chatRepository;

    private String currentConversationId;
    private String conversationTitle;
    private boolean isLoadingHistory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        sessionManager = new SessionManager(this);
        chatRepository = new ChatRepository(this);

        // Kiểm tra quyền Premium
        if (!sessionManager.isPremium()) {
            Toast.makeText(this, "Tính năng này chỉ dành cho người dùng Premium", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupChat();

        // Kiểm tra xem có conversation_id được truyền vào không
        Intent intent = getIntent();
        boolean isNewChat = intent.getBooleanExtra("new_chat", false);

        if (intent != null && intent.hasExtra("conversation_id") && !isNewChat) {
            currentConversationId = intent.getStringExtra("conversation_id");
            conversationTitle = intent.getStringExtra("conversation_title");
            if (conversationTitle != null) {
                tvTitle.setText(conversationTitle);
            }
            loadChatHistory();
        } else if (isNewChat) {
            // THAY ĐỔI: Nếu là cuộc trò chuyện mới, bắt đầu từ đầu
            startNewConversation();
        } else {
            // THAY ĐỔI: Tự động tải cuộc trò chuyện gần nhất thay vì tạo mới
            loadLatestConversation();
        }
    }

    private void initViews() {
        rvChatMessages = findViewById(R.id.rv_chat_messages);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        tvTitle = findViewById(R.id.tv_title);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());

        // Thiết lập title
        tvTitle.setText("🤖 Trợ lý AI Học tập");
    }

    private void setupChat() {
        messages = new ArrayList<>();
        adapter = new ChatMessageAdapter(this, messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);
        rvChatMessages.setAdapter(adapter);

        aiManager = new GeminiAIManager(this);
    }

    // THÊM MỚI: Tải cuộc trò chuyện gần nhất
    private void loadLatestConversation() {
        chatRepository.getConversations(new ChatRepository.ConversationsCallback() {
            @Override
            public void onSuccess(List<ChatConversation> conversations) {
                runOnUiThread(() -> {
                    if (conversations != null && !conversations.isEmpty()) {
                        // Lấy cuộc trò chuyện gần nhất (đầu tiên trong danh sách)
                        ChatConversation latestConversation = conversations.get(0);
                        currentConversationId = latestConversation.getId();
                        conversationTitle = latestConversation.getTitle();
                        tvTitle.setText(conversationTitle);

                        // Tải tin nhắn của cuộc trò chuyện này
                        loadChatHistory();
                    } else {
                        // Không có cuộc trò chuyện nào, hiển thị tin nhắn chào mừng
                        showWelcomeMessage();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Nếu có lỗi, hiển thị tin nhắn chào mừng
                    showWelcomeMessage();
                });
            }
        });
    }

    private void loadChatHistory() {
        if (currentConversationId == null) {
            return;
        }

        isLoadingHistory = true;

        // Hiển thị loading message
        ChatMessage loadingMsg = new ChatMessage("Đang tải lịch sử trò chuyện...", ChatMessage.TYPE_SYSTEM);
        adapter.addMessage(loadingMsg);

        chatRepository.getChatMessages(currentConversationId, new ChatRepository.ChatMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> chatMessages) {
                runOnUiThread(() -> {
                    // Xóa loading message
                    messages.clear();

                    // Thêm tin nhắn từ lịch sử
                    if (chatMessages != null && !chatMessages.isEmpty()) {
                        messages.addAll(chatMessages);
                        adapter.notifyDataSetChanged();
                        scrollToBottom();
                    } else {
                        // Nếu không có lịch sử, hiển thị tin nhắn chào mừng
                        showWelcomeMessage();
                    }

                    isLoadingHistory = false;
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Xóa loading message
                    messages.clear();

                    // Hiển thị tin nhắn chào mừng
                    showWelcomeMessage();

                    isLoadingHistory = false;
                });
            }
        });
    }

    private void showWelcomeMessage() {
        String welcomeMessage = "Xin chào! Tôi là trợ lý AI học tập của bạn. 📚\n\n" +
                "Tôi có thể giúp bạn:\n" +
                "• Giải thích các khái niệm học thuật\n" +
                "• Hướng dẫn giải bài tập\n" +
                "• Tư vấn phương pháp học tập\n" +
                "• Trả lời câu hỏi về các môn học\n\n" +
                "Hãy đặt câu hỏi để bắt đầu! 🚀";

        ChatMessage welcomeMsg = new ChatMessage(welcomeMessage, ChatMessage.TYPE_AI);
        adapter.addMessage(welcomeMsg);
        scrollToBottom();

        // CHỈ lưu tin nhắn chào mừng khi KHÔNG đang tải lịch sử và là cuộc trò chuyện mới
        if (!isLoadingHistory && currentConversationId == null) {
            saveMessageToDatabase(welcomeMsg, "Cuộc trò chuyện mới");
        }
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        // Thêm tin nhắn của user
        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.TYPE_USER);
        if (currentConversationId != null) {
            userMessage.setConversationId(currentConversationId);
        }
        adapter.addMessage(userMessage);
        scrollToBottom();

        // Xóa text trong EditText
        etMessage.setText("");

        // Lưu tin nhắn của user vào CSDL
        saveMessageToDatabase(userMessage, messageText);

        // Gửi tin nhắn đến AI
        aiManager.sendMessage(messageText, this);
    }

    private void saveMessageToDatabase(ChatMessage message, String potentialTitle) {
        if (sessionManager.getUserEmail() == null) {
            return;
        }

        // Nếu là tin nhắn đầu tiên và chưa có conversation_id, sử dụng potentialTitle làm tiêu đề
        String title = (currentConversationId == null && conversationTitle == null) ? potentialTitle : conversationTitle;

        chatRepository.saveMessage(message, title, new ChatRepository.SaveMessageCallback() {
            @Override
            public void onSuccess(ChatMessage savedMessage, String conversationId) {
                // Cập nhật conversation_id nếu chưa có
                if (currentConversationId == null) {
                    currentConversationId = conversationId;
                    // Cập nhật conversation_id cho tất cả tin nhắn trong adapter
                    for (ChatMessage msg : messages) {
                        msg.setConversationId(conversationId);
                    }
                }
            }

            @Override
            public void onError(String error) {
                // Log lỗi nhưng không hiển thị cho user để không làm gián đoạn trò chuyện
                android.util.Log.e(TAG, "Lỗi khi lưu tin nhắn: " + error);
            }
        });
    }

    @Override
    public void onTypingStart() {
        runOnUiThread(() -> {
            // Thêm typing indicator
            ChatMessage typingMessage = new ChatMessage("AI đang soạn tin...", ChatMessage.TYPE_AI, true);
            adapter.addMessage(typingMessage);
            scrollToBottom();
        });
    }

    @Override
    public void onSuccess(String response) {
        runOnUiThread(() -> {
            // Xóa typing indicator
            adapter.removeTypingMessage();

            // Thêm phản hồi từ AI
            ChatMessage aiMessage = new ChatMessage(response, ChatMessage.TYPE_AI);
            if (currentConversationId != null) {
                aiMessage.setConversationId(currentConversationId);
            }
            adapter.addMessage(aiMessage);
            scrollToBottom();

            // Lưu phản hồi của AI vào CSDL
            saveMessageToDatabase(aiMessage, null);
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            // Xóa typing indicator
            adapter.removeTypingMessage();

            // Hiển thị thông báo lỗi
            String errorMessage = "Xin lỗi, tôi gặp sự cố khi xử lý câu hỏi của bạn. Vui lòng thử lại sau.\n\nLỗi: " + error;
            ChatMessage errorMsg = new ChatMessage(errorMessage, ChatMessage.TYPE_AI);
            adapter.addMessage(errorMsg);
            scrollToBottom();

            Toast.makeText(this, "Lỗi AI: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            rvChatMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ai_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_clear_chat) {
            showClearChatDialog();
            return true;
        } else if (id == R.id.action_chat_history) {
            openChatHistory();
            return true;
        } else if (id == R.id.action_new_chat) {
            startNewChat();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showClearChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xóa lịch sử chat");
        builder.setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử trò chuyện này không?");

        builder.setPositiveButton("Xóa", (dialog, which) -> {
            clearCurrentChat();
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void clearCurrentChat() {
        if (currentConversationId != null) {
            chatRepository.deleteChatHistory(currentConversationId, new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    runOnUiThread(() -> {
                        // Xóa tin nhắn trên giao diện
                        messages.clear();
                        adapter.notifyDataSetChanged();

                        // Reset conversation
                        currentConversationId = null;
                        conversationTitle = null;

                        // Hiển thị tin nhắn chào mừng
                        showWelcomeMessage();

                        Toast.makeText(AIChatActivity.this, "Đã xóa lịch sử trò chuyện", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    runOnUiThread(() -> {
                        Toast.makeText(AIChatActivity.this, "Lỗi khi xóa lịch sử: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            // Nếu chưa có conversation_id, chỉ xóa trên giao diện
            messages.clear();
            adapter.notifyDataSetChanged();
            showWelcomeMessage();
        }
    }

    private void openChatHistory() {
        Intent intent = new Intent(this, ConversationsActivity.class);
        startActivity(intent);
    }

    // THAY ĐỔI: Bắt đầu cuộc trò chuyện mới
    private void startNewChat() {
        startNewConversation();
        Toast.makeText(this, "Đã bắt đầu cuộc trò chuyện mới", Toast.LENGTH_SHORT).show();
    }

    // THÊM MỚI: Bắt đầu cuộc trò chuyện hoàn toàn mới
    private void startNewConversation() {
        // Reset tất cả thông tin cuộc trò chuyện
        currentConversationId = null;
        conversationTitle = null;

        // Xóa tin nhắn trên giao diện
        messages.clear();
        adapter.notifyDataSetChanged();

        // Hiển thị tin nhắn chào mừng
        showWelcomeMessage();

        // Reset title
        tvTitle.setText("🤖 Trợ lý AI Học tập");

        android.util.Log.d(TAG, "Started new conversation");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aiManager != null) {
            aiManager.cleanup();
        }
    }
}
