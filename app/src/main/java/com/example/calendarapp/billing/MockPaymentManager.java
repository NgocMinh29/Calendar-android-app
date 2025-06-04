package com.example.calendarapp.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calendarapp.R;
import com.example.calendarapp.payment.BankTransferActivity;

import java.util.Random;

/**
 * Mock Payment Manager cho đồ án - mô phỏng thanh toán thực tế
 * Không sử dụng tiền thật, chỉ để demo
 */
public class MockPaymentManager {
    private static final String TAG = "MockPaymentManager";

    public interface PaymentListener {
        void onPaymentSuccess(String transactionId, String paymentMethod);
        void onPaymentFailed(String error);
        void onPaymentCanceled();
    }

    private Context context;
    private PaymentListener listener;

    public MockPaymentManager(Context context, PaymentListener listener) {
        this.context = context;
        this.listener = listener;
    }

    /**
     * Hiển thị dialog chọn phương thức thanh toán
     */
    public void showPaymentOptions(Activity activity, String productName, String price) {
        Log.d(TAG, "Showing payment options dialog");

        String[] paymentMethods = {
                "💳 Thẻ tín dụng/ghi nợ",
                "🏦 Chuyển khoản ngân hàng",
                "📱 Ví điện tử (MoMo, ZaloPay)",
                "💰 Thẻ cào điện thoại"
        };

        // Tạo custom dialog với ListView
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        // Tạo ListView
        ListView listView = new ListView(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity,
                android.R.layout.simple_list_item_1,
                paymentMethods
        );
        listView.setAdapter(adapter);

        builder.setTitle("Chọn phương thức thanh toán")
                .setMessage("Sản phẩm: " + productName + " - " + price + "\n\nVui lòng chọn phương thức thanh toán:")
                .setView(listView)
                .setNegativeButton("Hủy", (dialog, which) -> {
                    Log.d(TAG, "Payment canceled by user");
                    listener.onPaymentCanceled();
                });

        AlertDialog dialog = builder.create();

        // Xử lý click trên ListView
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMethod = paymentMethods[position];
            Log.d(TAG, "Selected payment method: " + selectedMethod);
            dialog.dismiss();

            // Xử lý theo phương thức được chọn
            switch (position) {
                case 0: // Thẻ tín dụng
                    processCardPayment(activity, selectedMethod, price);
                    break;
                case 1: // Chuyển khoản ngân hàng
                    processBankTransfer(activity, selectedMethod, price);
                    break;
                case 2: // Ví điện tử
                    processEWalletPayment(activity, selectedMethod, price);
                    break;
                case 3: // Thẻ cào
                    processCardPayment(activity, selectedMethod, price);
                    break;
                default:
                    processCardPayment(activity, selectedMethod, price);
                    break;
            }
        });

        dialog.show();
        Log.d(TAG, "Payment options dialog shown");
    }

    /**
     * Xử lý chuyển khoản ngân hàng - chuyển đến BankTransferActivity
     */
    private void processBankTransfer(Activity activity, String paymentMethod, String price) {
        Log.d(TAG, "Processing bank transfer");

        Intent intent = new Intent(activity, BankTransferActivity.class);
        intent.putExtra("PAYMENT_METHOD", paymentMethod);
        intent.putExtra("PRICE", price);
        activity.startActivity(intent);

        // Đóng activity hiện tại nếu cần
        if (activity.getClass().getSimpleName().equals("PremiumPurchaseActivity")) {
            activity.finish();
        }
    }

    /**
     * Xử lý thanh toán thẻ tín dụng (mock)
     */
    private void processCardPayment(Activity activity, String paymentMethod, String price) {
        processPayment(activity, paymentMethod, price);
    }

    /**
     * Xử lý thanh toán ví điện tử (mock)
     */
    private void processEWalletPayment(Activity activity, String paymentMethod, String price) {
        processPayment(activity, paymentMethod, price);
    }

    /**
     * Xử lý thanh toán với phương thức đã chọn (cho các phương thức mock)
     */
    private void processPayment(Activity activity, String paymentMethod, String price) {
        Log.d(TAG, "Processing payment with method: " + paymentMethod);

        // Hiển thị dialog loading
        AlertDialog loadingDialog = new AlertDialog.Builder(activity)
                .setTitle("Đang xử lý thanh toán...")
                .setMessage("Phương thức: " + paymentMethod + "\nVui lòng đợi trong giây lát")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Mô phỏng thời gian xử lý thanh toán (2-5 giây)
        int processingTime = 2000 + new Random().nextInt(3000);

        new Handler().postDelayed(() -> {
            loadingDialog.dismiss();

            // Mô phỏng tỷ lệ thành công 90%
            boolean isSuccess = new Random().nextInt(100) < 90;

            if (isSuccess) {
                showPaymentSuccess(activity, paymentMethod, price);
            } else {
                showPaymentFailed(activity, "Thanh toán thất bại. Vui lòng thử lại.");
            }
        }, processingTime);
    }

    /**
     * Phương thức backup - sử dụng multiple buttons
     */
    public void showPaymentOptionsBackup(Activity activity, String productName, String price) {
        Log.d(TAG, "Showing backup payment options");

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Chọn phương thức thanh toán")
                .setMessage("Sản phẩm: " + productName + " - " + price + "\n\nVui lòng chọn phương thức thanh toán:")
                .setPositiveButton("💳 Thẻ tín dụng", (dialog, which) -> {
                    processPayment(activity, "💳 Thẻ tín dụng/ghi nợ", price);
                })
                .setNeutralButton("🏦 Ngân hàng", (dialog, which) -> {
                    processBankTransfer(activity, "🏦 Chuyển khoản ngân hàng", price);
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    listener.onPaymentCanceled();
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Hiển thị dialog thanh toán thành công
     */
    private void showPaymentSuccess(Activity activity, String paymentMethod, String price) {
        String transactionId = generateTransactionId();

        Log.d(TAG, "Payment successful - Transaction ID: " + transactionId);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("✅ Thanh toán thành công!")
                .setMessage(
                        "Phương thức: " + paymentMethod + "\n" +
                                "Số tiền: " + price + "\n" +
                                "Mã giao dịch: " + transactionId + "\n\n" +
                                "Cảm ơn bạn đã nâng cấp Premium!"
                )
                .setPositiveButton("OK", (dialog, which) -> {
                    listener.onPaymentSuccess(transactionId, paymentMethod);
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Hiển thị dialog thanh toán thất bại
     */
    private void showPaymentFailed(Activity activity, String error) {
        Log.d(TAG, "Payment failed: " + error);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("❌ Thanh toán thất bại")
                .setMessage(error + "\n\nVui lòng kiểm tra lại thông tin và thử lại.")
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    // Có thể thử lại
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    listener.onPaymentFailed(error);
                })
                .show();
    }

    /**
     * Tạo mã giao dịch giả
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * Mô phỏng kiểm tra trạng thái thanh toán
     */
    public void checkPaymentStatus(String transactionId, PaymentStatusCallback callback) {
        new Handler().postDelayed(() -> {
            // Mô phỏng kiểm tra từ server
            boolean isVerified = new Random().nextBoolean();
            callback.onStatusChecked(isVerified, transactionId);
        }, 1000);
    }

    public interface PaymentStatusCallback {
        void onStatusChecked(boolean isVerified, String transactionId);
    }
}
