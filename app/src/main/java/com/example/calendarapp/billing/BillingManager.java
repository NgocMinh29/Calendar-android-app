package com.example.calendarapp.billing;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class BillingManager implements PurchasesUpdatedListener {
    private static final String TAG = "BillingManager";
    private static final String PREMIUM_PRODUCT_ID = "premium_upgrade";

    private BillingClient billingClient;
    private Context context;
    private BillingListener billingListener;
    private ProductDetails premiumProductDetails;

    public interface BillingListener {
        void onBillingSetupFinished(boolean success);
        void onProductDetailsLoaded(ProductDetails productDetails);
        void onPurchaseCompleted(Purchase purchase);
        void onPurchaseError(String error);
        void onPremiumStatusChecked(boolean isPremium);
    }

    public BillingManager(Context context, BillingListener listener) {
        this.context = context;
        this.billingListener = listener;

        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();
    }

    public void startConnection() {
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup successful");
                    billingListener.onBillingSetupFinished(true);
                    queryProductDetails();
                    queryPurchases();
                } else {
                    Log.e(TAG, "Billing setup failed: " + billingResult.getDebugMessage());
                    billingListener.onBillingSetupFinished(false);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected");
            }
        });
    }

    private void queryProductDetails() {
        QueryProductDetailsParams.Product product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(ImmutableList.of(product))
                .build();

        billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> productDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (!productDetailsList.isEmpty()) {
                        premiumProductDetails = productDetailsList.get(0);
                        billingListener.onProductDetailsLoaded(premiumProductDetails);
                        Log.d(TAG, "Product details loaded: " + premiumProductDetails.getName());
                    }
                } else {
                    Log.e(TAG, "Failed to query product details: " + billingResult.getDebugMessage());
                }
            }
        });
    }

    public void launchPurchaseFlow(Activity activity) {
        if (premiumProductDetails == null) {
            billingListener.onPurchaseError("Product details not loaded");
            return;
        }

        BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(premiumProductDetails)
                .build();

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(ImmutableList.of(productDetailsParams))
                .build();

        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlowParams);

        if (billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            Log.e(TAG, "Failed to launch billing flow: " + billingResult.getDebugMessage());
            billingListener.onPurchaseError("Failed to launch purchase: " + billingResult.getDebugMessage());
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled purchase");
            billingListener.onPurchaseError("Purchase canceled");
        } else {
            Log.e(TAG, "Purchase failed: " + billingResult.getDebugMessage());
            billingListener.onPurchaseError("Purchase failed: " + billingResult.getDebugMessage());
        }
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            Log.d(TAG, "Purchase successful: " + purchase.getOrderId());
            billingListener.onPurchaseCompleted(purchase);
        } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING) {
            Log.d(TAG, "Purchase pending: " + purchase.getOrderId());
            billingListener.onPurchaseError("Purchase is pending");
        }
    }

    public void queryPurchases() {
        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build();

        billingClient.queryPurchasesAsync(params, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> purchases) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    boolean hasPremium = false;
                    for (Purchase purchase : purchases) {
                        if (purchase.getProducts().contains(PREMIUM_PRODUCT_ID) &&
                                purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            hasPremium = true;
                            break;
                        }
                    }
                    billingListener.onPremiumStatusChecked(hasPremium);
                } else {
                    Log.e(TAG, "Failed to query purchases: " + billingResult.getDebugMessage());
                    billingListener.onPremiumStatusChecked(false);
                }
            }
        });
    }

    public void endConnection() {
        if (billingClient != null) {
            billingClient.endConnection();
        }
    }

    public String getPremiumProductId() {
        return PREMIUM_PRODUCT_ID;
    }
}
