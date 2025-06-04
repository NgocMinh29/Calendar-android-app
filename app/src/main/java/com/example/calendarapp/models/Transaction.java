package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("Mã GD")
    private String transactionId;

    @SerializedName("Mô tả")
    private String description;

    @SerializedName("Giá trị")
    private double amount;

    @SerializedName("Ngày diễn ra")
    private String transactionDate;

    @SerializedName("Số tài khoản")
    private String accountNumber;

    public String getTransactionId() {
        return transactionId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", transactionDate='" + transactionDate + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
