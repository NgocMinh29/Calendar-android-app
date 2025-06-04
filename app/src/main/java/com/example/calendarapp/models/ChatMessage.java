package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;
    public static final int TYPE_SYSTEM = 3;

    @SerializedName("id")
    private long id;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private int type; // 1: user, 2: AI, 3: system

    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("is_typing")
    private boolean isTyping;

    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("conversation_title")
    private String conversationTitle;

    private long timestamp;

    public ChatMessage() {
        this.isTyping = false;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
        this.isTyping = false;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String message, int type, boolean isTyping) {
        this(message, type);
        this.isTyping = isTyping;
    }

    public ChatMessage(String message, int type, String conversationId) {
        this(message, type);
        this.conversationId = conversationId;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isTyping() {
        return isTyping;
    }

    public void setTyping(boolean typing) {
        isTyping = typing;
    }

    public boolean isFromUser() {
        return type == TYPE_USER;
    }

    public boolean isFromAI() {
        return type == TYPE_AI;
    }

    public boolean isSystemMessage() {
        return type == TYPE_SYSTEM;
    }

    public String getFormattedTime() {
        // Chuyển đổi createdAt thành định dạng giờ phút
        try {
            if (createdAt != null && createdAt.length() > 16) {
                return createdAt.substring(11, 16); // Lấy phần giờ:phút
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Thêm getter và setter cho timestamp
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Thêm getter và setter cho userEmail
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // Thêm getter và setter cho conversationTitle
    public String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }
}
