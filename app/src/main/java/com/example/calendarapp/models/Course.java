package com.example.calendarapp.models;

import java.io.Serializable;
import java.util.Date;

public class Course implements Serializable {
    private long id;
    private String name;
    private String room;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private Date startDate;
    private Date endDate;
    private int weekFrequency;
    private boolean notification;
    private int reminderMinutes;

    public Course() {
        // Default constructor
    }

    public Course(long id, String name, String room, String dayOfWeek, String startTime, String endTime,
                  Date startDate, Date endDate, int weekFrequency, boolean notification, int reminderMinutes) {
        this.id = id;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
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
