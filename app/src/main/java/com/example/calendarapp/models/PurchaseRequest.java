package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

public class PurchaseRequest {
    @SerializedName("user_email")
    private String userEmail;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("purchase_token")
    private String purchaseToken;

    @SerializedName("transaction_id")
    private String transactionId;

    @SerializedName("price")
    private String price;

    public PurchaseRequest() {}

    public PurchaseRequest(String userEmail, String productId, String purchaseToken, String transactionId, String price) {
        this.userEmail = userEmail;
        this.productId = productId;
        this.purchaseToken = purchaseToken;
        this.transactionId = transactionId;
        this.price = price;
    }

    // Getters and Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
