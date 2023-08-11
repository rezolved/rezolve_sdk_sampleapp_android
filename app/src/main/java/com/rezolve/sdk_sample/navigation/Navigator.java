package com.rezolve.sdk_sample.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk_sample.category.CategoryViewActivity;
import com.rezolve.shared.ProductDetailsActivity;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.ProductUtils;

public class Navigator {

    public static void navigateToProductDetails(@NonNull Product product, @NonNull Activity activity) {
        Intent intent = new Intent(activity, ProductDetailsActivity.class);
        Bundle bundle = ProductUtils.toBundle(product);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void navigateToSspActView(@NonNull SspActResult act, @NonNull Activity activity) {
        Intent intent = new Intent(activity, SspActActivity.class);
        Bundle bundle = ProductUtils.toBundle(act.getSspAct());
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void navigateToProductListView(
            @NonNull Merchant merchant,
            @Nullable Category category,
            @NonNull Activity activity
    ) {
        Intent intent = new Intent(activity, CategoryViewActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(CategoryViewActivity.PARAM_MERCHANT_JSON_KEY, merchant.entityToJson().toString());
        if (category != null) {
            bundle.putString(CategoryViewActivity.PARAM_CATEGORY_JSON_KEY, category.entityToJson().toString());
        }
        intent.putExtras(bundle);

        activity.startActivity(intent);
    }
}
