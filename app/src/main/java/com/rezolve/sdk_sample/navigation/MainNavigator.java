package com.rezolve.sdk_sample.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;

public interface MainNavigator {
    void onContentResult(@NonNull ContentResult result);
    void navigateToProductListView(@NonNull Merchant merchant, @Nullable Category category);
    void navigateToProductDetails(@NonNull Product product);
}
