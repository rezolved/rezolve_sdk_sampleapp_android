package com.rezolve.sdk_sample.scan;

import androidx.annotation.StringRes;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;

import java.util.List;

public interface ScanCallback {
    void showLoadingIndicator();
    void hideLoadingIndicator();
    void onToastMessage(String msg);
    void onSnackbarMessage(@StringRes int msg);

    void showProductDetails(Product product);

    void showSspActView(SspActResult act);


    void showProductListView(Merchant merchantDetails, Category category);

    void showMerchantSelector(List<Merchant> merchants);
}
