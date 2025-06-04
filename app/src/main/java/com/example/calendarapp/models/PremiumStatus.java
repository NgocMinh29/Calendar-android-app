package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

public class PremiumStatus {
    @SerializedName("user_id")
    private int userId;

    @SerializedName("is_premium")
    private boolean isPremium;

    @SerializedName("purchase_date")
    private String purchaseDate;

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("expires_at")
    private String expiresAt;

    public PremiumStatus() {}

    public PremiumStatus(int userId, boolean isPremium, String purchaseDate, String transactionId, String productId) {
        this.userId = userId;
        this.isPremium = isPremium;
        this.purchaseDate = purchaseDate;
        this.transactionId = transactionId;
        this.productId = productId;
    }

    // Getters and Setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isPremium() {
        return isPremium;
    }

    public void setPremium(boolean premium) {
        isPremium = premium;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}
