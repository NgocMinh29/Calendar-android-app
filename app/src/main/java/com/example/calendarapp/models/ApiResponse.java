package com.example.calendarapp.models;
//
//public class ApiResponse<T> {
//    private boolean status;
//    private String message;
//    private T data; // Thay đổi từ 'user' thành 'data' để phù hợp với API
//
//    public boolean isStatus() {
//        return status;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public T getData() {
//        return data;
//    }
//}


import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("error")
    private String error;

    public ApiResponse() {}

    public ApiResponse(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status;
    }

    public void setSuccess(boolean success) {
        this.status = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
