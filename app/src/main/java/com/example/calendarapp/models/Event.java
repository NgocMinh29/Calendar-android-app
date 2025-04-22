package com.example.calendarapp.models;

import java.io.Serializable;
import java.util.Date;

public class Event implements Serializable {
    private long id;
    private String title;
    private String note;
    private Date date;
    private String time;
    private boolean notification;
    private int reminderMinutes;
    private String location;

    public Event() {
        // Default constructor
    }

    public Event(long id, String title, String note, Date date, String time, boolean notification, int reminderMinutes, String location) {
        this.id = id;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
