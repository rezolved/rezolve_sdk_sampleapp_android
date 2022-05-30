package com.rezolve.shared.services.callbacks;

import com.rezolve.sdk.model.shop.OrderSummary;

public interface PaymentCallback {
    void onPurchaseSuccess(OrderSummary orderSummary);

    void onPurchaseFailure(String message);
}
