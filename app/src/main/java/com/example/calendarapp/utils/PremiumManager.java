package com.example.calendarapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.PremiumStatus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremiumManager {
    private static final String TAG = "PremiumManager";
    private static final String PREF_NAME = "PremiumPrefs";
    private static final String KEY_IS_PREMIUM = "is_premium";
    private static final String KEY_LAST_CHECK = "last_check";
    private static final long CHECK_INTERVAL = 24 * 60 * 60 * 1000; // 24 hours

    private Context context;
    private SharedPreferences preferences;
    private ApiService apiService;

    public interface PremiumStatusListener {
        void onPremiumStatusChecked(boolean isPremium);
        void onError(String error);
    }

    public PremiumManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public boolean isPremium() {
        return preferences.getBoolean(KEY_IS_PREMIUM, false);
    }

    public void setPremium(boolean isPremium) {
        preferences.edit()
                .putBoolean(KEY_IS_PREMIUM, isPremium)
                .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
                .apply();
    }

    public void checkPremiumStatus(String userEmail, PremiumStatusListener listener) {
        // Kiểm tra cache trước
        long lastCheck = preferences.getLong(KEY_LAST_CHECK, 0);
        long now = System.currentTimeMillis();

        if (now - lastCheck < CHECK_INTERVAL && isPremium()) {
            // Cache còn hiệu lực và user là premium
            listener.onPremiumStatusChecked(true);
            return;
        }

        // Gọi API để kiểm tra
        apiService.getPremiumStatus(userEmail).enqueue(new Callback<ApiResponse<PremiumStatus>>() {
            @Override
            public void onResponse(Call<ApiResponse<PremiumStatus>> call, Response<ApiResponse<PremiumStatus>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    PremiumStatus status = response.body().getData();
                    boolean isPremium = status != null && status.isPremium();

                    setPremium(isPremium);
                    listener.onPremiumStatusChecked(isPremium);
                } else {
                    setPremium(false);
                    listener.onPremiumStatusChecked(false);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PremiumStatus>> call, Throwable t) {
                Log.e(TAG, "Failed to check premium status", t);
                listener.onError(t.getMessage());
            }
        });
    }

    public void clearPremiumStatus() {
        preferences.edit().clear().apply();
    }
}
