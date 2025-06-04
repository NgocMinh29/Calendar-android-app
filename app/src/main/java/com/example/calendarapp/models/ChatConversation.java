package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatConversation {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    private List<ChatMessage> messages;

    public ChatConversation() {
        this.messages = new ArrayList<>();
    }

    public ChatConversation(String id, String title) {
        this.id = id;
        this.title = title;
        this.messages = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
    }

    public String getFormattedDate() {
        // Chuyển đổi createdAt thành định dạng ngày tháng đẹp hơn
        try {
            // Đơn giản hóa, chỉ lấy ngày tháng năm
            if (createdAt != null && createdAt.length() > 10) {
                return createdAt.substring(0, 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdAt;
    }
}
