package com.rezolve.sdk_sample.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.rezolve.sdk.core.interfaces.MerchantByIdInterface;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.resolver.result.CategoryResult;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;
import com.rezolve.sdk.ssp.resolver.result.ProductResult;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk.ssp.resolver.result.SspCategoryResult;
import com.rezolve.sdk.ssp.resolver.result.SspProductResult;
import com.rezolve.sdk_sample.ProductListActivity;
import com.rezolve.sdk_sample.R;
import com.rezolve.shared.ProductDetailsActivity;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.ProductUtils;
import com.rezolve.shared.utils.sdk.MerchantManagerUtils;
import com.rezolve.shared.utils.sdk.RezolveSdkUtils;

import java.util.List;

public class Navigator implements MainNavigator {

    public static final String TAG = Navigator.class.getSimpleName();
    
    private final Activity activity;
    private final NavigatorEvents navigatorEvents;
    
    public Navigator(Activity activity, NavigatorEvents navigatorEvents) {
        this.activity = activity;
        this.navigatorEvents = navigatorEvents;
    }
    
    @Override
    public void onContentResult(@NonNull ContentResult result) {
        if (result instanceof ProductResult productResult) {
            onProductResult(productResult.getProduct(), productResult.getCategoryId());
        } else if (result instanceof CategoryResult categoryResult) {
            onCategoryResult(categoryResult.getCategory(), categoryResult.getMerchantId());
        } else if (result instanceof SspActResult act) {
            if (act.sspAct.getPageBuildingBlocks() != null && !act.sspAct.getPageBuildingBlocks().isEmpty()) {
                onSspActResult(act);
            }
        } else if (result instanceof SspProductResult sspProductResult) {
            navigatorEvents.onToastMessage(sspProductResult.getSspProduct().getEngagementName() + " - " + sspProductResult.getSspProduct().getRezolveTrigger());
        } else if (result instanceof SspCategoryResult sspCategoryResult) {
            navigatorEvents.onToastMessage(sspCategoryResult.getSspCategory().getEngagementName() + " - " + sspCategoryResult.getSspCategory().getRezolveTrigger());
        }
    }

    public void onProductResult(Product product, String categoryId) {
        navigateToProductDetails(product);
    }

    public void onCategoryResult(Category category, String merchantId) {
        navigateToProductListView(merchantId, category);
    }

    private void onSspActResult(SspActResult act) {
        navigateToSspActView(act);
    }

    @Override
    public void navigateToProductDetails(@NonNull Product product) {
        Intent intent = new Intent(activity, ProductDetailsActivity.class);
        Bundle bundle = ProductUtils.toBundle(product);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private void navigateToSspActView(SspActResult act) {
        Intent intent = new Intent(activity, SspActActivity.class);
        Bundle bundle = ProductUtils.toBundle(act.getSspAct());
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private void navigateToProductListView(String merchantId, Category category) {
        if (merchantId == null) {
            navigatorEvents.onSnackbarMessage(R.string.missing_merchants);
            return;
        }
        if (category == null) {
            navigatorEvents.onSnackbarMessage(R.string.missing_category);
            return;
        }
        navigatorEvents.showLoadingIndicator();
        RezolveSdkUtils.getMerchantManager().getMerchantById(
                merchantId,
                new MerchantByIdInterface() {
                    @Override
                    public void onError(@NonNull RezolveError error) {
                        Log.d(TAG, "onRezolveError: "+error.getMessage());
                        navigatorEvents.hideLoadingIndicator();
                        navigatorEvents.onToastMessage(error.getMessage());
                    }

                    @Override
                    public void onGetMerchantsDetailsSuccess(Merchant merchantDetails) {
                        navigatorEvents.hideLoadingIndicator();
                        if (merchantDetails != null) {
                            navigateToProductListView(merchantDetails, category);
                        } else {
                            navigatorEvents.onSnackbarMessage(R.string.missing_merchants);
                        }
                    }
                }
        );
    }

    @Override
    public void navigateToProductListView(@NonNull Merchant merchant, @Nullable Category category) {
        Intent intent = new Intent(activity, ProductListActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(ProductListActivity.PARAM_MERCHANT_JSON_KEY, merchant.entityToJson().toString());
        if (category != null) {
            bundle.putString(ProductListActivity.PARAM_CATEGORY_JSON_KEY, category.entityToJson().toString());
        }
        intent.putExtras(bundle);

        activity.startActivity(intent);
    }

    public interface NavigatorEvents {
        void onSnackbarMessage(@StringRes int message);
        void onToastMessage(String text);
        void showLoadingIndicator();
        void hideLoadingIndicator();
    }
}
