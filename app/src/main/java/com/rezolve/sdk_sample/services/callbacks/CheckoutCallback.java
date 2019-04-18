package com.rezolve.sdk_sample.services.callbacks;

import com.rezolve.sdk.model.cart.Order;

public interface CheckoutCallback {
    void onCheckoutSuccess(Order order);
    void onCheckoutFailure(String message);
}
