package com.rezolve.sdk_sample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.ybq.android.spinkit.SpinKitView;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.sdk.core.interfaces.ScanManagerInterface;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.core.managers.ScanManager;
import com.rezolve.sdk.model.foreign.RezolveScanResult;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.ScannedData;
import com.rezolve.sdk.views.RezolveScanView;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.sdk.MerchantManagerUtils;
import com.rezolve.sdk_sample.utils.sdk.RezolveSdkUtils;

import java.util.List;

public class ScanActivity extends AppCompatActivity implements ScanManagerInterface {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private SpinKitView loadingSpinView;
    private FloatingActionButton fabMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        loadingSpinView = findViewById(R.id.loadingSpinView);

        initFab();

    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                initializeScanner();
            }
        });
    }

    private void initializeScanner() {
        RezolveScanView scanView = findViewById(R.id.scanView);
        ScanManager scanManager = RezolveSdkUtils.getScanManager(this, true, true);

        scanView.refresh();
        scanManager.stopVideoScan();
        scanManager.destroy();
        scanManager.startVideoScan(this, scanView);
        scanManager.startAudioScan(this, null);
    }

    @Override
    public void onProductResult(Product product, String categoryId) {
        navigateToProductDetailsView(product);
    }

    @Override
    public void onCategoryResult(Category category, String merchantId) {
        navigateToProductListView(merchantId, category);
    }

    @Override
    public void onRezolveResult(RezolveScanResult rezolveScanResult) {
        Log.e(TAG, "[NON IMPLEMENTED]\nonRezolveResult(" + rezolveScanResult + ")");
    }

    @Override
    public void onScanError(RezolveError.RezolveErrorType rezolveErrorType, String errorMsg, ScannedData scannedData) {
        hideLoadingIndicator();
        DialogUtils.showError(this, rezolveErrorType.name() + "\n" + errorMsg);
    }

    @Override
    public void processingStarted() {
        displayLoadingIndicator();
    }

    @Override
    public void processingFinished() {
        hideLoadingIndicator();
    }

    private void displayLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.GONE);
    }

    private void navigateToProductDetailsView(Product product) {
        Intent intent = new Intent(ScanActivity.this, ProductDetailsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(ProductDetailsActivity.PARAM_PRODUCT_KEY, product.entityToJson().toString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void navigateToProductListView(String merchantId, Category category) {
        View view = getWindow().getDecorView().getRootView();
        if (merchantId == null) {
            showSnackbar(view, getString(R.string.missing_merchants), Color.YELLOW);
            return;
        }
        if (category == null) {
            showSnackbar(view, getString(R.string.missing_category), Color.YELLOW);
            return;
        }
        MerchantManagerUtils.getMerchants(
                RezolveSdkUtils.getMerchantManager(),
                getApplicationContext(),
                MerchantManager.MerchantVisibility.ALL,
                new BaseGetMerchantsCallback(view) {
                    @Override
                    public void onSuccess(List<Merchant> merchantList) {
                        if (merchantList != null) {
                            for (Merchant merchant : merchantList) {
                                if (merchant != null && merchantId.equalsIgnoreCase(merchant.getId())) {
                                    navigateToProductListView(merchant, category);
                                    return;
                                }
                            }
                        }
                        showSnackbar(view, getString(R.string.missing_merchants), Color.YELLOW);
                    }
                }
        );
    }

    private void navigateToProductListView(@NonNull Merchant merchant, @Nullable Category category) {
        Intent intent = new Intent(ScanActivity.this, ProductListActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(ProductListActivity.PARAM_MERCHANT_JSON_KEY, merchant.entityToJson().toString());
        if (category != null) {
            bundle.putString(ProductListActivity.PARAM_CATEGORY_JSON_KEY, category.entityToJson().toString());
        }
        intent.putExtras(bundle);

        startActivity(intent);
    }

    //#
    //# MENU ACTION AND MERCHANT LIST WORKAROUND:
    //#

    private void showSnackbar(View view, String message, @ColorInt int color) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }

    private void showWarningSnackbar(View view, @StringRes int stringResId) {
        showSnackbar(view, getString(stringResId), Color.YELLOW);
    }

    private void showRezolveErrorSnackbar(View view, RezolveError rezolveError) {
        String errorMessage = RezolveSdkUtils.formatedRezolveError(
                getApplicationContext(),
                rezolveError
        );
        Log.e(TAG, errorMessage);
        showSnackbar(view, errorMessage, Color.RED);
    }
    private void initFab() {
        fabMain = findViewById(R.id.fabMain);
        if (fabMain != null) {
            fabMain.setOnClickListener(this::onFabMainClick);
        }
    }

    private void onFabMainClick(View view) {
        MerchantManagerUtils.getMerchants(
                RezolveSdkUtils.getMerchantManager(),
                getApplicationContext(),
                MerchantManager.MerchantVisibility.ALL,
                new BaseGetMerchantsCallback(view) {
                    @Override
                    public void onSuccess(List<Merchant> merchantList) {
                        if (merchantList != null && merchantList.size() > 0) {
                            DialogUtils.showChoicer(
                                    ScanActivity.this,
                                    getString(R.string.merchant_choicer_title),
                                    merchantList,
                                    (spinnerView, item) -> navigateToProductListView(item, null)

                            );
                        } else {
                            showWarningSnackbar(view, R.string.missing_merchants);
                        }
                    }
                }
        );
    }

    //#
    //# MENU ACTION AND MERCHANT LIST WORKAROUND ^^^
    //#

    //
    // Redundant callbacks
    //

    private class BaseProcessingInterface implements RezolveSdkUtils.ProcessingInterface {

        private View view;

        BaseProcessingInterface(View view) {
            this.view = view;
        }

        @Override
        public void processingStarted() {
            displayLoadingIndicator();
        }

        @Override
        public void processingFinished() {
            hideLoadingIndicator();
        }

        @Override
        public void onRezolveError(@NonNull RezolveError rezolveError) {
            showRezolveErrorSnackbar(view, rezolveError);
        }
    }

    private abstract class BaseGetMerchantsCallback extends BaseProcessingInterface
            implements MerchantManagerUtils.GetMerchantsCallback {

        BaseGetMerchantsCallback(View view) {
            super(view);
        }
    }

}