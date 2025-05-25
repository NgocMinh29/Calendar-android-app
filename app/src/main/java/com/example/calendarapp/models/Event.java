package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Event implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("title")
    private String title;

    @SerializedName("note")
    private String note;

    @SerializedName("date")
    private String date; // Format: yyyy-MM-dd

    @SerializedName("time")
    private String time; // Format: HH:mm

    @SerializedName("notification")
    private boolean notification;

    @SerializedName("reminder_minutes")
    private int reminderMinutes;

    @SerializedName("location")
    private String location;

    public Event() {
        // Default constructor
    }

    public Event(long id, int userId, String title, String note, String date, String time,
                 boolean notification, int reminderMinutes, String location) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.note = note;
        this.date = date;
        this.time = time;
        this.notification = notification;
        this.reminderMinutes = reminderMinutes;
        this.location = location;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public int getReminderMinutes() {
        return reminderMinutes;
    }

    public void setReminderMinutes(int reminderMinutes) {
        this.reminderMinutes = reminderMinutes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
