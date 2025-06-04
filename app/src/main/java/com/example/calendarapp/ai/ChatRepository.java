package com.example.calendarapp.ai;

import android.content.Context;
import android.util.Log;

import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.ChatConversation;
import com.example.calendarapp.models.ChatMessage;
import com.example.calendarapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {
    private static final String TAG = "ChatRepository";

    private ApiService apiService;
    private SessionManager sessionManager;
    private Context context;

    // Cache cho tin nhắn
    private Map<String, List<ChatMessage>> messageCache;

    // Cache cho cuộc trò chuyện
    private List<ChatConversation> conversationsCache;

    // Callback interfaces
    public interface ChatMessagesCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String error);
    }

    public interface ConversationsCallback {
        void onSuccess(List<ChatConversation> conversations);
        void onError(String error);
    }

    public interface SaveMessageCallback {
        void onSuccess(ChatMessage message, String conversationId);
        void onError(String error);
    }

    public ChatRepository(Context context) {
        this.context = context;
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.sessionManager = new SessionManager(context);
        this.messageCache = new HashMap<>();
        this.conversationsCache = new ArrayList<>();
    }

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng
     */
    public void getConversations(ConversationsCallback callback) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        // Nếu có cache, trả về cache trước
        if (conversationsCache != null && !conversationsCache.isEmpty()) {
            callback.onSuccess(conversationsCache);
        }

        // Gọi API để lấy dữ liệu mới nhất
        apiService.getConversations(userEmail).enqueue(new Callback<ApiResponse<List<ChatConversation>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatConversation>>> call, Response<ApiResponse<List<ChatConversation>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChatConversation>> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        // Cập nhật cache
                        conversationsCache = apiResponse.getData();
                        callback.onSuccess(conversationsCache);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Lỗi khi lấy danh sách cuộc trò chuyện");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatConversation>>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi lấy danh sách cuộc trò chuyện", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy tin nhắn của một cuộc trò chuyện
     */
    public void getChatMessages(String conversationId, ChatMessagesCallback callback) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        // Nếu có cache, trả về cache trước
        if (messageCache.containsKey(conversationId)) {
            callback.onSuccess(messageCache.get(conversationId));
        }

        // Gọi API để lấy dữ liệu mới nhất
        apiService.getChatMessages(userEmail, conversationId).enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChatMessage>>> call, Response<ApiResponse<List<ChatMessage>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChatMessage>> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        // Cập nhật cache
                        messageCache.put(conversationId, apiResponse.getData());
                        callback.onSuccess(apiResponse.getData());
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Lỗi khi lấy tin nhắn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChatMessage>>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi lấy tin nhắn", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Lưu tin nhắn mới
     */
    public void saveMessage(ChatMessage message, String conversationTitle, SaveMessageCallback callback) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            callback.onError("Người dùng chưa đăng nhập");
            return;
        }

        // Tạo một bản sao của tin nhắn để thêm thông tin user_email
        ChatMessage messageToSend = new ChatMessage();
        messageToSend.setId(message.getId());
        messageToSend.setMessage(message.getMessage());
        messageToSend.setType(message.getType());
        messageToSend.setConversationId(message.getConversationId());
        messageToSend.setTimestamp(message.getTimestamp());
        messageToSend.setTyping(message.isTyping());

        // Thêm user_email vào tin nhắn - ĐÂY LÀ TRƯỜNG BẮT BUỘC
        messageToSend.setUserEmail(userEmail);

        // Thêm conversation_title nếu có
        if (conversationTitle != null) {
            messageToSend.setConversationTitle(conversationTitle);
        }

        // Gọi API để lưu tin nhắn
        apiService.saveChatMessage(messageToSend).enqueue(new Callback<ApiResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChatMessage> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        // Cập nhật cache
                        String conversationId = apiResponse.getData().getConversationId();
                        if (!messageCache.containsKey(conversationId)) {
                            messageCache.put(conversationId, new ArrayList<>());
                        }
                        messageCache.get(conversationId).add(apiResponse.getData());

                        // Cập nhật cache cuộc trò chuyện
                        updateConversationCache(conversationId, conversationTitle);

                        callback.onSuccess(apiResponse.getData(), conversationId);
                    } else {
                        callback.onError(apiResponse.getMessage());
                    }
                } else {
                    callback.onError("Lỗi khi lưu tin nhắn");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {
                Log.e(TAG, "Lỗi khi lưu tin nhắn", t);
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Xóa lịch sử chat
     */
    public void deleteChatHistory(String conversationId, Callback<ApiResponse<String>> callback) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null) {
            return;
        }

        // THAY ĐỔI: Sử dụng phương thức POST thay vì DELETE và gửi dữ liệu trong body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("user_email", userEmail);
        if (conversationId != null) {
            requestBody.put("conversation_id", conversationId);
        }

        apiService.deleteChatHistoryWithBody(requestBody).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Xóa cache
                    if (conversationId != null) {
                        messageCache.remove(conversationId);
                        // Xóa khỏi danh sách cuộc trò chuyện
                        if (conversationsCache != null) {
                            for (int i = 0; i < conversationsCache.size(); i++) {
                                if (conversationsCache.get(i).getId().equals(conversationId)) {
                                    conversationsCache.remove(i);
                                    break;
                                }
                            }
                        }
                    } else {
                        messageCache.clear();
                        conversationsCache.clear();
                    }
                }
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                callback.onFailure(call, t);
            }
        });
    }

    /**
     * Cập nhật cache cuộc trò chuyện
     */
    private void updateConversationCache(String conversationId, String title) {
        if (conversationsCache == null) {
            conversationsCache = new ArrayList<>();
        }

        // Kiểm tra xem cuộc trò chuyện đã tồn tại trong cache chưa
        boolean found = false;
        for (ChatConversation conversation : conversationsCache) {
            if (conversation.getId().equals(conversationId)) {
                // Cập nhật thời gian
                conversation.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
                found = true;
                break;
            }
        }

        // Nếu chưa tồn tại, thêm mới
        if (!found) {
            ChatConversation newConversation = new ChatConversation(conversationId, title);
            newConversation.setUserId(sessionManager.getUserId());
            conversationsCache.add(0, newConversation); // Thêm vào đầu danh sách
        }
    }

    /**
     * Xóa cache
     */
    public void clearCache() {
        messageCache.clear();
        conversationsCache.clear();
    }
}
