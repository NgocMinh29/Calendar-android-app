package com.example.calendarapp.payment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.calendarapp.api.ApiClient;
import com.example.calendarapp.api.ApiService;
import com.example.calendarapp.models.ApiResponse;
import com.example.calendarapp.models.PremiumStatus;
import com.example.calendarapp.models.PurchaseRequest;
import com.example.calendarapp.models.Transaction;
import com.example.calendarapp.models.TransactionResponse;
import com.example.calendarapp.utils.SessionManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;

public class TransactionVerifier {
    private static final String TAG = "TransactionVerifier";
    private static final String SHEET_API_URL = "https://script.google.com/macros/s/AKfycbyr1PeTvviDEZNmpCfCpQtXRUabtWiFFMVD5jjtyszCZetsbQ2QriNvwWXyygB1I_B0Hw/exec";
    private static final double MINIMUM_AMOUNT = 99000.0;
    private static final String TARGET_ACCOUNT = "0772983376";
    private static final String PREMIUM_PREFIX = "PREMIUM";

    // Preferences để lưu trữ các giao dịch đã xử lý
    private static final String PREFS_NAME = "TransactionVerifierPrefs";
    private static final String PROCESSED_TRANSACTIONS_KEY = "processed_transactions";

    private final Context context;
    private final SessionManager sessionManager;
    private final ApiService apiService;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final OkHttpClient httpClient;
    private final Gson gson;
    private final SharedPreferences prefs;
    private Set<String> processedTransactions;

    public interface TransactionVerifierListener {
        void onVerificationStarted();
        void onTransactionFound(Transaction transaction);
        void onPremiumActivated();
        void onVerificationFailed(String error);
        void onNoValidTransactions();
    }

    public TransactionVerifier(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.httpClient = new OkHttpClient();
        this.gson = new Gson();

        // Khởi tạo SharedPreferences để lưu trữ giao dịch đã xử lý
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.processedTransactions = prefs.getStringSet(PROCESSED_TRANSACTIONS_KEY, new HashSet<>());
    }

    public void verifyTransaction(String referenceCode, TransactionVerifierListener listener) {
        // Kiểm tra nếu người dùng đã là premium, không cần xác thực nữa
        if (sessionManager.isPremium()) {
            Log.d(TAG, "User is already premium, skipping verification");
            if (listener != null) {
                mainHandler.post(() -> listener.onNoValidTransactions());
            }
            return;
        }

        if (listener != null) {
            listener.onVerificationStarted();
        }

        executorService.execute(() -> {
            try {
                // Fetch transactions from Google Sheet API
                Request request = new Request.Builder()
                        .url(SHEET_API_URL)
                        .build();

                Response response = httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response);
                }

                String responseBody = response.body().string();
                Log.d(TAG, "API Response: " + responseBody);

                TransactionResponse transactionResponse = gson.fromJson(responseBody, TransactionResponse.class);

                if (transactionResponse.isError()) {
                    throw new IOException("API returned error");
                }

                List<Transaction> transactions = transactionResponse.getTransactions();
                if (transactions == null || transactions.isEmpty()) {
                    notifyNoValidTransactions(listener);
                    return;
                }

                // Find valid transaction
                Transaction validTransaction = findValidTransaction(transactions, referenceCode);

                if (validTransaction != null) {
                    // Kiểm tra xem giao dịch này đã được xử lý chưa
                    String transactionKey = validTransaction.getTransactionId() + "_" + validTransaction.getAmount();
                    if (processedTransactions.contains(transactionKey)) {
                        Log.d(TAG, "Transaction already processed: " + transactionKey);
                        notifyNoValidTransactions(listener);
                        return;
                    }

                    // Đánh dấu giao dịch này đã được xử lý
                    Set<String> updatedSet = new HashSet<>(processedTransactions);
                    updatedSet.add(transactionKey);
                    prefs.edit().putStringSet(PROCESSED_TRANSACTIONS_KEY, updatedSet).apply();
                    processedTransactions = updatedSet;

                    notifyTransactionFound(listener, validTransaction);
                    activatePremium(listener, validTransaction);
                } else {
                    notifyNoValidTransactions(listener);
                }

            } catch (Exception e) {
                Log.e(TAG, "Error verifying transaction", e);
                notifyVerificationFailed(listener, e.getMessage());
            }
        });
    }

    private Transaction findValidTransaction(List<Transaction> transactions, String referenceCode) {
        // Tìm giao dịch hợp lệ: số tiền >= 99,000 VND và đúng tài khoản
        for (Transaction transaction : transactions) {
            Log.d(TAG, "Checking transaction: " + transaction);

            if (transaction.getAmount() >= MINIMUM_AMOUNT &&
                    TARGET_ACCOUNT.equals(transaction.getAccountNumber())) {

                // Nếu có mã tham chiếu, kiểm tra mô tả có chứa mã đó không
                if (referenceCode != null && !referenceCode.isEmpty()) {
                    if (transaction.getDescription() != null &&
                            transaction.getDescription().contains(referenceCode)) {
                        return transaction;
                    }
                } else {
                    // Nếu không có mã tham chiếu, chỉ cần kiểm tra số tiền và tài khoản
                    return transaction;
                }
            }
        }
        return null;
    }

    private void activatePremium(TransactionVerifierListener listener, Transaction transaction) {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            notifyVerificationFailed(listener, "Không tìm thấy thông tin người dùng");
            return;
        }

        // Tạo mã giao dịch từ thông tin giao dịch ngân hàng
        String transactionId = "BANK_" + transaction.getTransactionId() + "_" + System.currentTimeMillis();

        // Tạo request để kích hoạt premium
        PurchaseRequest purchaseRequest = new PurchaseRequest(
                userEmail,
                "premium_lifetime",
                "bank_transfer",
                transactionId,
                String.valueOf((int)transaction.getAmount())
        );

        // Gọi API để kích hoạt premium
        apiService.purchasePremium(purchaseRequest).enqueue(new Callback<ApiResponse<PremiumStatus>>() {
            @Override
            public void onResponse(Call<ApiResponse<PremiumStatus>> call, retrofit2.Response<ApiResponse<PremiumStatus>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    // Cập nhật session
                    sessionManager.updatePremiumStatus(true);

                    // Thông báo kích hoạt thành công
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onPremiumActivated();
                        }
                    });
                } else {
                    // Trong trường hợp API lỗi, vẫn kích hoạt premium ở local
                    sessionManager.updatePremiumStatus(true);

                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onPremiumActivated();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PremiumStatus>> call, Throwable t) {
                Log.e(TAG, "API call failed", t);

                // Trong trường hợp API lỗi, vẫn kích hoạt premium ở local
                sessionManager.updatePremiumStatus(true);

                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPremiumActivated();
                    }
                });
            }
        });
    }

    private void notifyTransactionFound(TransactionVerifierListener listener, Transaction transaction) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onTransactionFound(transaction);
            }
        });
    }

    private void notifyPremiumActivated(TransactionVerifierListener listener) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onPremiumActivated();
            }
        });
    }

    private void notifyVerificationFailed(TransactionVerifierListener listener, String error) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onVerificationFailed(error);
            }
        });
    }

    private void notifyNoValidTransactions(TransactionVerifierListener listener) {
        mainHandler.post(() -> {
            if (listener != null) {
                listener.onNoValidTransactions();
            }
        });
    }

    public static String generateReferenceCode() {
        // Tạo mã tham chiếu ngắn để người dùng dễ nhập vào nội dung chuyển khoản
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return PREMIUM_PREFIX + uuid;
    }

    // Xóa tất cả giao dịch đã xử lý (để test)
    public void clearProcessedTransactions() {
        prefs.edit().remove(PROCESSED_TRANSACTIONS_KEY).apply();
        processedTransactions = new HashSet<>();
    }
}
