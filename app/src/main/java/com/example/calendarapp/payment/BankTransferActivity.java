package com.example.calendarapp.payment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapp.R;
import com.example.calendarapp.models.Transaction;
import com.example.calendarapp.premium.PurchaseSuccessActivity;
import com.example.calendarapp.utils.SessionManager;
import com.example.calendarapp.utils.VietQRGenerator;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BankTransferActivity extends AppCompatActivity implements TransactionVerifier.TransactionVerifierListener {
    private static final String TAG = "BankTransferActivity";
    private static final long COUNTDOWN_TIME = 15 * 60 * 1000; // 15 phút
    private static final long CHECK_INTERVAL = 30 * 1000; // 30 giây

    // UI Components - khớp với layout XML
    private ImageView ivQRCode;
    private TextView tvBankName;
    private TextView tvAccountNumber;
    private TextView tvAccountName;
    private TextView tvAmount;
    private TextView tvTransferContent;
    private TextView tvCountdown;
    private TextView tvStatus;
    private Button btnCopyAccount;
    private Button btnCopyAmount;
    private Button btnCopyContent;
    private Button btnManualConfirm;
    private Button btnCancel;
    private ImageView btnBack;
    private ProgressBar progressLoading;
    private ProgressBar progressChecking;
    private Button btnCheckTransaction;

    // Data
    private String bankName = "MB Bank";
    private String accountNumber = "0772983376";
    private String accountName = "NGUYEN QUANG THANG";
    private String amount = "99000";
    private String referenceCode;

    private CountDownTimer countDownTimer;
    private CountDownTimer autoCheckTimer;
    private TransactionVerifier transactionVerifier;
    private SessionManager sessionManager;
    private boolean isVerifying = false;
    private boolean transactionFound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        initViews();
        initData();
        setupClickListeners();
        startCountdown();
        generateQRCode();

        // Tự động kiểm tra giao dịch mỗi 30 giây
        startAutoCheck();
    }

    private void initViews() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvBankName = findViewById(R.id.tvBankName);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAmount = findViewById(R.id.tvAmount);
        tvTransferContent = findViewById(R.id.tvTransferContent);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvStatus = findViewById(R.id.tvStatus);
        btnCopyAccount = findViewById(R.id.btnCopyAccount);
        btnCopyAmount = findViewById(R.id.btnCopyAmount);
        btnCopyContent = findViewById(R.id.btnCopyContent);
        btnManualConfirm = findViewById(R.id.btnManualConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        btnCheckTransaction = findViewById(R.id.btnCheckTransaction);

        // Thêm ProgressBar vào layout nếu chưa có
        progressLoading = findViewById(R.id.progressLoading);
        if (progressLoading == null) {
            progressLoading = new ProgressBar(this);
            progressLoading.setVisibility(View.GONE);
        }

        progressChecking = findViewById(R.id.progressChecking);
        if (progressChecking == null) {
            progressChecking = new ProgressBar(this);
            progressChecking.setVisibility(View.GONE);
        }
    }

    private void initData() {
        sessionManager = new SessionManager(this);
        transactionVerifier = new TransactionVerifier(this);

        // Tạo mã tham chiếu duy nhất cho giao dịch này
        referenceCode = TransactionVerifier.generateReferenceCode();

        // Hiển thị thông tin ngân hàng
        tvBankName.setText(bankName);
        tvAccountNumber.setText(accountNumber);
        tvAccountName.setText(accountName);

        // Format số tiền
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedAmount = formatter.format(Double.parseDouble(amount)) + " VND";
        tvAmount.setText(formattedAmount);

        // Hiển thị mã tham chiếu
        tvTransferContent.setText(referenceCode);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        btnCancel.setOnClickListener(v -> onBackPressed());

        btnCopyAccount.setOnClickListener(v -> copyToClipboard("Số tài khoản", accountNumber));
        btnCopyAmount.setOnClickListener(v -> copyToClipboard("Số tiền", amount));
        btnCopyContent.setOnClickListener(v -> copyToClipboard("Nội dung chuyển khoản", referenceCode));

        btnManualConfirm.setOnClickListener(v -> showManualConfirmDialog());
        btnCheckTransaction.setOnClickListener(v -> verifyTransaction());
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã sao chép " + label, Toast.LENGTH_SHORT).show();
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes);
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00");
                showTimeoutDialog();
            }
        }.start();
    }

    private void showTimeoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hết thời gian")
                .setMessage("Thời gian chờ thanh toán đã hết. Bạn có muốn tiếp tục chờ không?")
                .setPositiveButton("Tiếp tục chờ", (dialog, which) -> {
                    startCountdown();
                })
                .setNegativeButton("Quay lại", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void showManualConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận thanh toán")
                .setMessage("Bạn đã hoàn thành chuyển khoản và muốn xác nhận thủ công?")
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    verifyTransaction();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void generateQRCode() {
        if (progressLoading != null) {
            progressLoading.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            try {
                // Sử dụng VietQR API để tạo QR code
                String vietQrUrl = String.format(
                        "https://img.vietqr.io/image/970422-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                        accountNumber,
                        amount,
                        referenceCode,
                        accountName.replace(" ", "%20")
                );

                Log.d(TAG, "VietQR URL: " + vietQrUrl);

                // Tải QR code từ URL
                Bitmap qrBitmap = VietQRGenerator.downloadQRImage(vietQrUrl);

                runOnUiThread(() -> {
                    if (progressLoading != null) {
                        progressLoading.setVisibility(View.GONE);
                    }

                    if (qrBitmap != null) {
                        ivQRCode.setImageBitmap(qrBitmap);
                        tvStatus.setText("✅ QR VietQR sẵn sàng - Quét để chuyển khoản!");
                    } else {
                        tvStatus.setText("⚠️ Không thể tạo QR code - Vui lòng chuyển khoản thủ công");
                        Toast.makeText(this, "Không thể tạo mã QR", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error generating QR code", e);
                runOnUiThread(() -> {
                    if (progressLoading != null) {
                        progressLoading.setVisibility(View.GONE);
                    }
                    tvStatus.setText("⚠️ Lỗi tạo QR code - Vui lòng chuyển khoản thủ công");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void verifyTransaction() {
        if (isVerifying) return;

        isVerifying = true;
        if (progressChecking != null) {
            progressChecking.setVisibility(View.VISIBLE);
        }

        transactionVerifier.verifyTransaction(referenceCode, this);
    }

    private void startAutoCheck() {
        autoCheckTimer = new CountDownTimer(COUNTDOWN_TIME, CHECK_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Chỉ kiểm tra nếu chưa tìm thấy giao dịch và không đang trong quá trình kiểm tra
                if (!transactionFound && !isVerifying && !isFinishing()) {
                    Log.d(TAG, "Auto checking transaction...");
                    verifyTransaction();
                }
            }

            @Override
            public void onFinish() {
                // Không làm gì khi hết thời gian
            }
        }.start();
    }

    @Override
    public void onVerificationStarted() {
        runOnUiThread(() -> {
            if (progressChecking != null) {
                progressChecking.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onTransactionFound(Transaction transaction) {
        runOnUiThread(() -> {
            Toast.makeText(this, "Đã tìm thấy giao dịch: " + transaction.getAmount() + " VND", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPremiumActivated() {
        // Đánh dấu đã tìm thấy giao dịch để dừng auto check
        transactionFound = true;

        runOnUiThread(() -> {
            isVerifying = false;
            if (progressChecking != null) {
                progressChecking.setVisibility(View.GONE);
            }

            // Dừng countdown
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Dừng auto check
            if (autoCheckTimer != null) {
                autoCheckTimer.cancel();
            }

            // Cập nhật session
            sessionManager.updatePremiumStatus(true);

            // Hiển thị thông báo thành công
            tvStatus.setText("✅ Đã kích hoạt Premium thành công!");
            Toast.makeText(this, "Đã kích hoạt Premium thành công!", Toast.LENGTH_LONG).show();

            // Chuyển đến màn hình thành công
            Intent intent = new Intent(this, PurchaseSuccessActivity.class);
            intent.putExtra("TRANSACTION_ID", "BANK_TRANSFER");
            intent.putExtra("PAYMENT_METHOD", "Chuyển khoản ngân hàng");
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onVerificationFailed(String error) {
        runOnUiThread(() -> {
            isVerifying = false;
            if (progressChecking != null) {
                progressChecking.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onNoValidTransactions() {
        runOnUiThread(() -> {
            isVerifying = false;
            if (progressChecking != null) {
                progressChecking.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (autoCheckTimer != null) {
            autoCheckTimer.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc muốn hủy quá trình thanh toán?")
                .setPositiveButton("Có", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}
