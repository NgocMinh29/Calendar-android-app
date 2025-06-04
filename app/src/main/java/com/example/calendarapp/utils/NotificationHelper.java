package com.example.calendarapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

// SỬA: Import từ package utils thay vì receivers
import com.example.calendarapp.utils.ReminderReceiver;

import java.util.Calendar;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    public static void scheduleReminder(Context context, int id, Calendar time, String title) {
        try {
            Log.d(TAG, "Scheduling reminder for: " + title);

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("event_title", title);

            // SỬA: Thêm FLAG_IMMUTABLE cho Android 12+
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, id, intent, flags
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // SỬA: Kiểm tra quyền đặt exact alarm trên Android S+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
                        Log.d(TAG, "Exact alarm scheduled successfully");
                    } else {
                        // Fallback to inexact alarm
                        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
                        Log.d(TAG, "Inexact alarm scheduled (no permission for exact)");
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
                    Log.d(TAG, "Exact alarm scheduled successfully");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling reminder: " + e.getMessage());
        }
    }

    public static void cancelReminder(Context context, int id) {
        try {
            Log.d(TAG, "Cancelling reminder with id: " + id);

            Intent intent = new Intent(context, ReminderReceiver.class);

            // SỬA: Thêm FLAG_IMMUTABLE cho Android 12+
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, id, intent, flags
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                Log.d(TAG, "Reminder cancelled successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cancelling reminder: " + e.getMessage());
        }
    }
}