package com.rezolve.sdk_sample;

import androidx.annotation.NonNull;

import com.rezolve.sdk.model.shop.Product;

public interface MainNavigator {
    void navigateToProductDetails(@NonNull Product product);
}
