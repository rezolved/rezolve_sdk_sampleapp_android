package com.rezolve.shared.services.callbacks;

import com.rezolve.sdk.model.cart.Order;

public interface CheckoutCallback {
    void onCheckoutSuccess(Order order);

    void onCheckoutFailure(String message);
}
