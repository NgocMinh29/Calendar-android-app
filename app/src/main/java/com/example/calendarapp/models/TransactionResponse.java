package com.example.calendarapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TransactionResponse {
    @SerializedName("data")
    private List<Transaction> transactions;

    @SerializedName("error")
    private boolean error;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public boolean isError() {
        return error;
    }
}
