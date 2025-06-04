package com.example.calendarapp.premium;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;
import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.billing.MockPaymentManager;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.PremiumStatus;
import com.example.calendarapp.models.PurchaseRequest;
import com.example.calendarapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PremiumPurchaseActivity extends AppCompatActivity implements MockPaymentManager.PaymentListener {
    private static final String TAG = "PremiumPurchaseActivity";

    private MockPaymentManager paymentManager;
    private SessionManager sessionManager;
    private ApiService apiService;

    private Button btnPurchase;
    private ImageButton btnBack;
    private TextView tvPrice;
    private ProgressBar progressLoading;
    private TextView tvDemoMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_purchase);

        Log.d(TAG, "PremiumPurchaseActivity created");

        initViews();
        initServices();
        setupClickListeners();

        // Hiển thị thông báo demo mode
    }

    private void initViews() {
        btnPurchase = findViewById(R.id.btn_purchase);
        btnBack = findViewById(R.id.btn_back);
        tvPrice = findViewById(R.id.tv_price);
        progressLoading = findViewById(R.id.progress_loading);
        tvDemoMode = findViewById(R.id.tv_test_mode);
    }

    private void initServices() {
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        paymentManager = new MockPaymentManager(this, this);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPurchase.setOnClickListener(v -> {
            Log.d(TAG, "Purchase button clicked");

            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Vui lòng đăng nhập để mua Premium", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hiển thị các phương thức thanh toán
            Log.d(TAG, "Showing payment options");
            try {
                paymentManager.showPaymentOptions(this, "Premium Lifetime", "99.000đ");
            } catch (Exception e) {
                Log.e(TAG, "Error showing payment options", e);
                // Fallback to backup method
                paymentManager.showPaymentOptionsBackup(this, "Premium Lifetime", "99.000đ");
            }
        });
    }

    private void showLoading(boolean show) {
        progressLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        btnPurchase.setEnabled(!show);
    }

    @Override
    public void onPaymentSuccess(String transactionId, String paymentMethod) {
        Log.d(TAG, "Payment completed: " + transactionId);

        showLoading(true);

        // Gửi thông tin purchase lên server
        PurchaseRequest purchaseRequest = new PurchaseRequest(
                sessionManager.getUserEmail(),
                "premium_lifetime",
                "mock_token_" + transactionId,
                transactionId,
                "99000"
        );

        apiService.purchasePremium(purchaseRequest).enqueue(new Callback<ApiResponse<PremiumStatus>>() {
            @Override
            public void onResponse(Call<ApiResponse<PremiumStatus>> call, Response<ApiResponse<PremiumStatus>> response) {
                runOnUiThread(() -> {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        // Thành công
                        Toast.makeText(PremiumPurchaseActivity.this, "Nâng cấp Premium thành công!", Toast.LENGTH_SHORT).show();

                        // Cập nhật session
                        sessionManager.updatePremiumStatus(true);

                        // Chuyển đến màn hình thành công
                        Intent intent = new Intent(PremiumPurchaseActivity.this, PurchaseSuccessActivity.class);
                        intent.putExtra("TRANSACTION_ID", transactionId);
                        intent.putExtra("PAYMENT_METHOD", paymentMethod);
                        startActivity(intent);
                        finish();
                    } else {
                        String errorMsg = response.body() != null ? response.body().getMessage() : "Lỗi không xác định";
                        Toast.makeText(PremiumPurchaseActivity.this, "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<PremiumStatus>> call, Throwable t) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "API call failed", t);

                    // Trong demo mode, vẫn cho phép thành công
                    Toast.makeText(PremiumPurchaseActivity.this, "Nâng cấp Premium thành công! (Demo Mode)", Toast.LENGTH_SHORT).show();
                    sessionManager.updatePremiumStatus(true);

                    Intent intent = new Intent(PremiumPurchaseActivity.this, PurchaseSuccessActivity.class);
                    intent.putExtra("TRANSACTION_ID", transactionId);
                    intent.putExtra("PAYMENT_METHOD", paymentMethod);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

    @Override
    public void onPaymentFailed(String error) {
        Log.d(TAG, "Payment failed: " + error);
        Toast.makeText(this, "Thanh toán thất bại: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentCanceled() {
        Log.d(TAG, "Payment canceled");
        Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();
    }
}
