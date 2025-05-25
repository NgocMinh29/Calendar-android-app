package com.example.calendarapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Tên SharedPreferences
    private static final String PREF_NAME = "CalendarAppSession";

    // Các khóa cho SharedPreferences
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_IS_GUEST = "isGuest";

    private static final String KEY_USER_ID = "userId";
    // SharedPreferences và Editor
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    // Constructor
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Lưu thông tin đăng nhập
     */
    public void createLoginSession(String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null ? name : "");
        int userId = extractUserIdFromEmail(email);
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    /**
     * Tạo phiên đăng nhập khách
     */
    public void createGuestSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_GUEST, true);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.putInt(KEY_USER_ID, -1);
        editor.commit();
    }
    /**
     * Kiểm tra trạng thái đăng nhập
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Kiểm tra xem có phải người dùng khách không
     */
    public boolean isGuest() {
        return pref.getBoolean(KEY_IS_GUEST, false);
    }

    /**
     * Lấy thông tin người dùng đã đăng nhập
     */
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, null);
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
    /**
     * Đăng xuất người dùng
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }

    private int extractUserIdFromEmail(String email) {
        // This is a simple implementation
        // In a real app, you should get the user ID from the login response
        return Math.abs(email.hashCode() % 10000);
    }

    public void createLoginSession(String email, String name, int userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null ? name : "");
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    /**
     * Lấy user ID
     */


    /**
     * Kiểm tra xem có thể thực hiện thao tác API không
     */
    public boolean canPerformApiOperations() {
        return isLoggedIn() && getUserId() != -1;
    }

}