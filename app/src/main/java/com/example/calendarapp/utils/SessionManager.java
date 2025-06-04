package com.example.calendarapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    // Tên SharedPreferences
    private static final String PREF_NAME = "CalendarAppSession";

    // Các khóa cho SharedPreferences (giữ nguyên các khóa cũ)
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_IS_GUEST = "isGuest";
    private static final String KEY_USER_ID = "userId";

    // Thêm các khóa mới cho premium
    private static final String KEY_IS_PREMIUM = "isPremium";
    private static final String KEY_PREMIUM_PURCHASE_DATE = "premiumPurchaseDate";
    private static final String KEY_PREMIUM_EXPIRES_AT = "premiumExpiresAt";
    private static final String KEY_PREMIUM_LAST_CHECK = "premiumLastCheck";

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
     * Lưu thông tin đăng nhập (method cũ - giữ nguyên)
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
     * Lưu thông tin đăng nhập với userId (method cũ - giữ nguyên)
     */
    public void createLoginSession(String email, String name, int userId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null ? name : "");
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    /**
     * Lưu thông tin đăng nhập với premium status (method mới)
     */
    public void createLoginSession(String email, String name, int userId, boolean isPremium) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_IS_GUEST, false);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name != null ? name : "");
        editor.putInt(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_PREMIUM, isPremium);
        if (isPremium) {
            editor.putLong(KEY_PREMIUM_LAST_CHECK, System.currentTimeMillis());
        }
        editor.commit();
    }

    /**
     * Tạo phiên đăng nhập khách (method cũ - giữ nguyên)
     */
    public void createGuestSession() {
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_GUEST, true);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_NAME);
        editor.putInt(KEY_USER_ID, -1);
        // Xóa thông tin premium khi là guest
        editor.putBoolean(KEY_IS_PREMIUM, false);
        editor.remove(KEY_PREMIUM_PURCHASE_DATE);
        editor.remove(KEY_PREMIUM_EXPIRES_AT);
        editor.commit();
    }

    /**
     * Kiểm tra trạng thái đăng nhập (method cũ - giữ nguyên)
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Kiểm tra xem có phải người dùng khách không (method cũ - giữ nguyên)
     */
    public boolean isGuest() {
        return pref.getBoolean(KEY_IS_GUEST, false);
    }

    /**
     * Lấy thông tin người dùng đã đăng nhập (methods cũ - giữ nguyên)
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
     * Đăng xuất người dùng (method cũ - giữ nguyên)
     */
    public void logout() {
        editor.clear();
        editor.commit();
    }

    /**
     * Kiểm tra xem có thể thực hiện thao tác API không (method cũ - giữ nguyên)
     */
    public boolean canPerformApiOperations() {
        return isLoggedIn() && getUserId() != -1;
    }

    private int extractUserIdFromEmail(String email) {
        // This is a simple implementation
        // In a real app, you should get the user ID from the login response
        return Math.abs(email.hashCode() % 10000);
    }

    // ==================== PREMIUM METHODS (MỚI) ====================

    /**
     * Cập nhật trạng thái premium
     */
    public void updatePremiumStatus(boolean isPremium) {
        editor.putBoolean(KEY_IS_PREMIUM, isPremium);
        editor.putLong(KEY_PREMIUM_LAST_CHECK, System.currentTimeMillis());
        if (isPremium) {
            editor.putString(KEY_PREMIUM_PURCHASE_DATE, String.valueOf(System.currentTimeMillis()));
        }
        editor.commit();
    }

    /**
     * Cập nhật thông tin premium chi tiết
     */
    public void updatePremiumInfo(boolean isPremium, String purchaseDate, String expiresAt) {
        editor.putBoolean(KEY_IS_PREMIUM, isPremium);
        editor.putLong(KEY_PREMIUM_LAST_CHECK, System.currentTimeMillis());
        if (purchaseDate != null) {
            editor.putString(KEY_PREMIUM_PURCHASE_DATE, purchaseDate);
        }
        if (expiresAt != null) {
            editor.putString(KEY_PREMIUM_EXPIRES_AT, expiresAt);
        }
        editor.commit();
    }

    /**
     * Kiểm tra trạng thái premium
     */
    public boolean isPremium() {
        return pref.getBoolean(KEY_IS_PREMIUM, false);
    }

    /**
     * Lấy ngày mua premium
     */
    public String getPremiumPurchaseDate() {
        return pref.getString(KEY_PREMIUM_PURCHASE_DATE, null);
    }

    /**
     * Lấy ngày hết hạn premium
     */
    public String getPremiumExpiresAt() {
        return pref.getString(KEY_PREMIUM_EXPIRES_AT, null);
    }

    /**
     * Lấy thời gian kiểm tra premium lần cuối
     */
    public long getPremiumLastCheck() {
        return pref.getLong(KEY_PREMIUM_LAST_CHECK, 0);
    }

    /**
     * Kiểm tra xem có cần refresh premium status không (24h)
     */
    public boolean shouldRefreshPremiumStatus() {
        long lastCheck = getPremiumLastCheck();
        long now = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000; // 24 hours
        return (now - lastCheck) > dayInMillis;
    }

    /**
     * Xóa thông tin premium
     */
    public void clearPremiumInfo() {
        editor.putBoolean(KEY_IS_PREMIUM, false);
        editor.remove(KEY_PREMIUM_PURCHASE_DATE);
        editor.remove(KEY_PREMIUM_EXPIRES_AT);
        editor.remove(KEY_PREMIUM_LAST_CHECK);
        editor.commit();
    }

    /**
     * Kiểm tra xem user có thể sử dụng tính năng premium không
     */
    public boolean canUsePremiumFeatures() {
        return isLoggedIn() && isPremium();
    }

    /**
     * Lấy tất cả thông tin premium
     */
    public PremiumInfo getPremiumInfo() {
        return new PremiumInfo(
                isPremium(),
                getPremiumPurchaseDate(),
                getPremiumExpiresAt(),
                getPremiumLastCheck()
        );
    }

    /**
     * Class helper để lưu thông tin premium
     */
    public static class PremiumInfo {
        private boolean isPremium;
        private String purchaseDate;
        private String expiresAt;
        private long lastCheck;

        public PremiumInfo(boolean isPremium, String purchaseDate, String expiresAt, long lastCheck) {
            this.isPremium = isPremium;
            this.purchaseDate = purchaseDate;
            this.expiresAt = expiresAt;
            this.lastCheck = lastCheck;
        }

        // Getters
        public boolean isPremium() { return isPremium; }
        public String getPurchaseDate() { return purchaseDate; }
        public String getExpiresAt() { return expiresAt; }
        public long getLastCheck() { return lastCheck; }
    }
}
