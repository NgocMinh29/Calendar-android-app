package com.example.calendarapp.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class để tạo QR Code theo chuẩn VietQR EMVCo với autofill đầy đủ
 */
public class QRCodeGenerator {
    private static final String TAG = "QRCodeGenerator";

    /**
     * Tạo QR Code VietQR với autofill đầy đủ thông tin
     */
    public static Bitmap generateBankTransferQR(String bankCode, String accountNumber,
                                                String accountName, String amount,
                                                String transferContent, String transactionId) {
        try {
            // Tạo QR content theo chuẩn VietQR EMVCo với đầy đủ thông tin
            String qrContent = createCompleteVietQRContent(bankCode, accountNumber, accountName,
                    amount, transferContent);

            Log.d(TAG, "Complete VietQR Content: " + qrContent);

            // Tạo QR Code
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 512, 512);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            Log.d(TAG, "Complete VietQR Code generated successfully");
            return bitmap;

        } catch (WriterException e) {
            Log.e(TAG, "Error generating complete VietQR code", e);
            return null;
        }
    }

    /**
     * Tạo VietQR content hoàn chỉnh theo chuẩn EMVCo với autofill
     */
    private static String createCompleteVietQRContent(String bankCode, String accountNumber,
                                                      String accountName, String amount,
                                                      String transferContent) {

        // Lấy bank BIN code
        String bankBIN = getBankBIN(bankCode);

        // Tạo QR theo chuẩn EMVCo đầy đủ
        StringBuilder qrBuilder = new StringBuilder();

        // 1. Payload Format Indicator (ID "00") - Bắt buộc
        qrBuilder.append("000201");

        // 2. Point of Initiation Method (ID "01") - Static QR
        qrBuilder.append("010212");

        // 3. Merchant Account Information (ID "38" - VietQR) - Thông tin ngân hàng
        String merchantInfo = buildMerchantAccountInfo(bankBIN, accountNumber);
        qrBuilder.append("38").append(String.format("%02d", merchantInfo.length())).append(merchantInfo);

        // 4. Merchant Category Code (ID "52") - Chuyển khoản cá nhân
        qrBuilder.append("520400000");

        // 5. Transaction Currency (ID "53") - VND = 704
        qrBuilder.append("5303704");

        // 6. Transaction Amount (ID "54") - Số tiền autofill
        if (amount != null && !amount.isEmpty()) {
            String cleanAmount = amount.replace(",", "").replace(".", "").replace(" ", "");
            // Đảm bảo amount là số nguyên (VND không có decimal)
            try {
                int amountInt = Integer.parseInt(cleanAmount);
                String amountStr = String.valueOf(amountInt);
                qrBuilder.append("54").append(String.format("%02d", amountStr.length())).append(amountStr);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Invalid amount format: " + amount);
            }
        }

        // 7. Country Code (ID "58") - Vietnam
        qrBuilder.append("5802VN");

        // 8. Merchant Name (ID "59") - Tên người nhận autofill
        String merchantName = accountName != null ? accountName : "NGUYEN VAN A";
        // Giới hạn 25 ký tự và chuyển thành uppercase
        merchantName = merchantName.toUpperCase();
        if (merchantName.length() > 25) {
            merchantName = merchantName.substring(0, 25);
        }
        qrBuilder.append("59").append(String.format("%02d", merchantName.length())).append(merchantName);

        // 9. Merchant City (ID "60") - Thành phố
        String merchantCity = "HO CHI MINH";
        qrBuilder.append("60").append(String.format("%02d", merchantCity.length())).append(merchantCity);

        // 10. Additional Data Field Template (ID "62") - Nội dung chuyển khoản autofill
        String additionalData = buildAdditionalData(transferContent);
        if (!additionalData.isEmpty()) {
            qrBuilder.append("62").append(String.format("%02d", additionalData.length())).append(additionalData);
        }

        // 11. CRC (ID "63") - Checksum cuối cùng
        String qrWithoutCRC = qrBuilder.toString() + "6304";
        String crc = calculateCRC16(qrWithoutCRC);
        qrBuilder.append("63").append("04").append(crc);

        return qrBuilder.toString();
    }

    /**
     * Xây dựng Merchant Account Information theo chuẩn VietQR
     */
    private static String buildMerchantAccountInfo(String bankBIN, String accountNumber) {
        StringBuilder builder = new StringBuilder();

        // Globally Unique Identifier (ID "00") - VietQR GUID
        String guid = "A000000727";
        builder.append("00").append(String.format("%02d", guid.length())).append(guid);

        // Payment network specific (ID "01") - Bank BIN + Account Number
        String bankAccount = bankBIN + accountNumber;
        builder.append("01").append(String.format("%02d", bankAccount.length())).append(bankAccount);

        // Service Code (ID "02") - QRIBFTTA (QR Instant Bank Fund Transfer to Account)
        String serviceCode = "QRIBFTTA";
        builder.append("02").append(String.format("%02d", serviceCode.length())).append(serviceCode);

        return builder.toString();
    }

    /**
     * Xây dựng Additional Data Field với nội dung chuyển khoản
     */
    private static String buildAdditionalData(String transferContent) {
        if (transferContent == null || transferContent.trim().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        // Bill Number (ID "01") - Nội dung chuyển khoản
        String content = transferContent.trim();
        // Giới hạn 25 ký tự cho nội dung
        if (content.length() > 25) {
            content = content.substring(0, 25);
        }

        builder.append("01").append(String.format("%02d", content.length())).append(content);

        // Purpose of Transaction (ID "08") - Mục đích giao dịch
        String purpose = "PREMIUM";
        builder.append("08").append(String.format("%02d", purpose.length())).append(purpose);

        return builder.toString();
    }

    /**
     * Lấy Bank BIN code chính thức theo VietQR
     */
    private static String getBankBIN(String bankCode) {
        switch (bankCode.toUpperCase()) {
            case "MB":
            case "MBBANK":
                return "970422"; // MB Bank
            case "VCB":
            case "VIETCOMBANK":
                return "970436"; // Vietcombank
            case "TCB":
            case "TECHCOMBANK":
                return "970407"; // Techcombank
            case "ACB":
                return "970416"; // ACB
            case "VTB":
            case "VIETINBANK":
                return "970415"; // VietinBank
            case "BIDV":
                return "970418"; // BIDV
            case "VPB":
            case "VPBANK":
                return "970432"; // VPBank
            case "TPB":
            case "TPBANK":
                return "970423"; // TPBank
            case "STB":
            case "SACOMBANK":
                return "970403"; // Sacombank
            case "HDB":
            case "HDBANK":
                return "970437"; // HDBank
            case "MSB":
            case "MSBANK":
                return "970426"; // MSB
            case "OCB":
                return "970448"; // OCB
            case "SHB":
                return "970443"; // SHB
            case "EIB":
                return "970431"; // EIB
            case "VAB":
            case "VIETABANK":
                return "970427"; // VietABank
            case "NAB":
            case "NAMABANK":
                return "970428"; // NamABank
            case "PGB":
            case "PGBANK":
                return "970430"; // PGBank
            case "VIET":
            case "VIETBANK":
                return "970433"; // VietBank
            case "BVB":
            case "BAOVIETBANK":
                return "970438"; // BaoVietBank
            case "SEAB":
            case "SEABANK":
                return "970440"; // SeABank
            case "COOPBANK":
                return "970446"; // CoopBank
            case "LPB":
            case "LPBANK":
                return "970449"; // LPBank
            case "KLB":
            case "KIENLONGBANK":
                return "970452"; // KienLongBank
            default:
                return "970422"; // Default to MB Bank
        }
    }

    /**
     * Tính CRC16 theo chuẩn ISO/IEC 13239 cho VietQR
     */
    private static String calculateCRC16(String data) {
        int crc = 0xFFFF;
        byte[] bytes = data.getBytes();

        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }

        return String.format("%04X", crc);
    }

    /**
     * Tạo QR Code đơn giản với text (fallback)
     */
    public static Bitmap generateSimpleQR(String text, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;

        } catch (WriterException e) {
            Log.e(TAG, "Error generating simple QR code", e);
            return null;
        }
    }
}
