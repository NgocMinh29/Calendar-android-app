package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Course implements Serializable {
    @SerializedName("id")
    private long id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("name")
    private String name;

    @SerializedName("room")
    private String room;

    @SerializedName("day_of_week")
    private String dayOfWeek;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("start_date")
    private String startDate; // Format: yyyy-MM-dd

    @SerializedName("end_date")
    private String endDate; // Format: yyyy-MM-dd

    @SerializedName("week_frequency")
    private int weekFrequency;

    @SerializedName("notification")
    private boolean notification;

    @SerializedName("reminder_minutes")
    private int reminderMinutes;

    public Course() {
        // Default constructor
    }

    public Course(long id, int userId, String name, String room, String dayOfWeek,
                  String startTime, String endTime, String startDate, String endDate,
                  int weekFrequency, boolean notification, int reminderMinutes) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.room = room;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weekFrequency = weekFrequency;
        this.notification = notification;
        this.reminderMinutes = reminderMinutes;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getWeekFrequency() {
        return weekFrequency;
    }

    public void setWeekFrequency(int weekFrequency) {
        this.weekFrequency = weekFrequency;
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
}
