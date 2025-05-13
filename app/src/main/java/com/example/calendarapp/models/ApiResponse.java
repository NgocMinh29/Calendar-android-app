package com.example.calendarapp.models;

public class ApiResponse<T> {
    private boolean status;
    private String message;
    private T data; // Thay đổi từ 'user' thành 'data' để phù hợp với API

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}