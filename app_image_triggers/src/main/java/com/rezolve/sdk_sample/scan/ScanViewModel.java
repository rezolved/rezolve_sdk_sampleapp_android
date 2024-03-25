package com.rezolve.sdk_sample.scan;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.callbacks.MerchantCallback;
import com.rezolve.sdk.core.callbacks.TriggerCallback;
import com.rezolve.sdk.core.interfaces.MerchantByIdInterface;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.ScannedData;
import com.rezolve.sdk.ssp.resolver.result.CategoryResult;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;
import com.rezolve.sdk.ssp.resolver.result.ProductResult;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk.ssp.resolver.result.SspCategoryResult;
import com.rezolve.sdk.ssp.resolver.result.SspProductResult;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.navigation.Navigator;
import com.rezolve.shared.utils.DialogUtils;

import java.util.List;

public class ScanViewModel {

    private final static String TAG = ScanViewModel.class.getSimpleName();

    private final ScanCallback scanCallback;

    public ScanViewModel(ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    public void onContentResult(@NonNull ContentResult result) {
        if (result instanceof ProductResult productResult) {
            onProductResult(productResult.getProduct());
        } else if (result instanceof CategoryResult categoryResult) {
            onCategoryResult(categoryResult.getCategory(), categoryResult.getMerchantId());
        } else if (result instanceof SspActResult act) {
            if (act.sspAct.getPageBuildingBlocks() != null && !act.sspAct.getPageBuildingBlocks().isEmpty()) {
                onSspActResult(act);
            }
        } else if (result instanceof SspProductResult sspProductResult) {
            RezolveSDK.peekInstance().getRezolveSession().getTriggerManager().resolveTrigger(
                    sspProductResult.getSspProduct().getRezolveTrigger(),
                    new TriggerCallback() {
                        @Override
                        public void onProductResult(Product product, String categoryId) {
                            Log.d(TAG, "onProductResult: "+product.entityToJson());
                            scanCallback.showProductDetails(product);
                        }

                        @Override
                        public void onBadTrigger() {
                            Log.e(TAG, "onContentResult.SspCategoryResult.onBadTrigger");
                        }

                        @Override
                        public void onScanError(@NonNull RezolveError error, @Nullable ScannedData scannedData) {
                            Log.e(TAG, "onContentResult.SspCategoryResult.error: "+error.getMessage() + ", scannedData: "+scannedData);
                        }
                    }
            );
        } else if (result instanceof SspCategoryResult sspCategoryResult) {
            // Sample payload:
            //SspCategoryResult{
            // sspCategory=SspCategory{
            //  id='a17faf22-ea72-4632-8d6d-8a9aae7b3ae5',
            //  merchantId='79',
            //  title='Default Category Target Page',
            //  subtitle='Open your Store Page',
            //  description='Open your Store Page',
            //  rezolveTrigger='http://rzlv.co/instant?path=2/252/2',
            //  serviceId='c94dd852-64b2-44f9-aa47-0a2bfac9685d',
            //  isActive=true',
            //  type=CATEGORY'
            // }
            //}
            RezolveSDK.peekInstance().getRezolveSession().getTriggerManager().resolveTrigger(
                    sspCategoryResult.getSspCategory().getRezolveTrigger(),
                    new TriggerCallback() {
                        @Override
                        public void onCategoryResult(Category category, String merchantId) {
                            Log.d(TAG, "onCategoryResult: "+category.entityToJson());
                            getMerchantDetailsAndRedirectToProductListView(merchantId, category);
                        }

                        @Override
                        public void onBadTrigger() {
                            Log.e(TAG, "onContentResult.SspCategoryResult.onBadTrigger");
                        }

                        @Override
                        public void onScanError(@NonNull RezolveError error, @Nullable ScannedData scannedData) {
                            Log.e(TAG, "onContentResult.SspCategoryResult.error: "+error.getMessage() + ", scannedData: "+scannedData);
                        }
                    }
            );
        }
    }

    private void onProductResult(Product product) {
        scanCallback.showProductDetails(product);
    }

    private void onCategoryResult(Category category, String merchantId) {
        getMerchantDetailsAndRedirectToProductListView(merchantId, category);
    }

    private void onSspActResult(SspActResult act) {
        scanCallback.showSspActView(act);
    }

    // depending on your UX flow, you can show the category or fetch merchant details first
    private void getMerchantDetailsAndRedirectToProductListView(String merchantId, Category category) {
        if (merchantId == null) {
            scanCallback.onSnackbarMessage(R.string.missing_merchants);
            return;
        }
        if (category == null) {
            scanCallback.onSnackbarMessage(R.string.missing_category);
            return;
        }
        scanCallback.showLoadingIndicator();
        RezolveSDK.peekInstance().getRezolveSession().getMerchantManager().getMerchantById(
                merchantId,
                new MerchantByIdInterface() {
                    @Override
                    public void onError(@NonNull RezolveError error) {
                        Log.d(TAG, "onRezolveError: "+error.getMessage());
                        scanCallback.hideLoadingIndicator();
                        scanCallback.onToastMessage(error.getMessage());
                    }

                    @Override
                    public void onGetMerchantsDetailsSuccess(Merchant merchantDetails) {
                        scanCallback.hideLoadingIndicator();
                        if (merchantDetails != null) {
                            scanCallback.showProductListView(merchantDetails, category);
                        } else {
                            scanCallback.onSnackbarMessage(R.string.missing_merchants);
                        }
                    }
                }
        );
    }

    public void onFabMainClick() {
        scanCallback.showLoadingIndicator();

        // in production implementation before accessing a manager make sure that the session is not null
        RezolveSDK.peekInstance().getRezolveSession().getMerchantManager().getMerchants(
                MerchantManager.MerchantVisibility.ALL,
                new MerchantCallback() {
                    @Override
                    public void onGetMerchantsSuccess(List<Merchant> merchants) {
                        scanCallback.hideLoadingIndicator();
                        if (merchants != null && merchants.size() > 0) {
                            scanCallback.showMerchantSelector(merchants);
                        } else {
                            scanCallback.onSnackbarMessage(R.string.missing_merchants);
                        }
                    }

                    @Override
                    public void onError(@NonNull RezolveError error) {
                        scanCallback.hideLoadingIndicator();
                        Log.d(TAG, "onRezolveError: "+error.getMessage());
                        scanCallback.onToastMessage(error.getMessage());
                    }
                }
        );
    }
}
