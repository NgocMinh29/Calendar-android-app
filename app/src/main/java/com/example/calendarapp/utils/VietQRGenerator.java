package com.example.calendarapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VietQRGenerator {
    private static final String TAG = "VietQRGenerator";
    private static final String VIETQR_API_URL = "https://img.vietqr.io/image/%s-%s-%s.png";

    /**
     * Tạo URL VietQR để tải QR code
     *
     * @param bankBin Mã BIN của ngân hàng (970422 cho MB Bank)
     * @param accountNumber Số tài khoản
     * @param amount Số tiền (không có dấu phẩy)
     * @param description Nội dung chuyển khoản
     * @param accountName Tên chủ tài khoản
     * @return URL để tải QR code
     */
    public static String generateVietQRUrl(String bankBin, String accountNumber, String amount,
                                           String description, String accountName) throws IOException {
        String baseUrl = String.format(VIETQR_API_URL, bankBin, accountNumber, "compact2");

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        builder.appendQueryParameter("amount", amount);
        builder.appendQueryParameter("addInfo", description);

        if (accountName != null && !accountName.isEmpty()) {
            builder.appendQueryParameter("accountName", accountName);
        }

        return builder.build().toString();
    }

    /**
     * Tải QR code từ URL
     *
     * @param url URL của QR code
     * @return Bitmap của QR code
     */
    public static Bitmap downloadQRImage(String url) throws IOException {
        Log.d(TAG, "Downloading QR from: " + url);

        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + connection.getResponseCode());
            }

            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
