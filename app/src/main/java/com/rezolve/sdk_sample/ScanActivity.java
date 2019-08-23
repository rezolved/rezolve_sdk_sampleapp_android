package com.rezolve.sdk_sample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.github.ybq.android.spinkit.SpinKitView;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.interfaces.MerchantInterface;
import com.rezolve.sdk.core.interfaces.ScanManagerInterface;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.core.managers.ScanManager;
import com.rezolve.sdk.model.foreign.RezolveScanResult;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.ScannedData;
import com.rezolve.sdk.model.shop.SupportedDeliveryMethod;
import com.rezolve.sdk.views.RezolveScanView;

import java.util.List;

public class ScanActivity extends AppCompatActivity implements ScanManagerInterface {

    private static final String TAG = ScanActivity.class.getSimpleName();
    private RezolveSDK rezolveSdk;

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

        rezolveSdk = RezolveSDK.peekInstance();

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
        ScanManager scanManager = rezolveSdk.getRezolveSession().getScanManager(this, true, true);

        scanView.refresh();
        scanManager.stopVideoScan();
        scanManager.destroy();
        scanManager.startVideoScan(this, scanView);
        scanManager.startAudioScan(this, null);

    }

    @Override
    public void onProductResult(Product product) {
        navigateToProductDetailsView(product);
    }

    @Override
    public void onCategoryResult(Category category, String s) {
    }

    @Override
    public void onRezolveResult(RezolveScanResult rezolveScanResult) {
    }

    @Override
    public void onScanError(RezolveError.RezolveErrorType rezolveErrorType, String errorMsg, ScannedData scannedData) {
        hideLoadingIndicator();
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
        loadingSpinView.setVisibility(View.INVISIBLE);
    }

    private void navigateToProductDetailsView(Product product) {
        Intent intent = new Intent(ScanActivity.this, ProductDetailsActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(ProductDetailsActivity.PARAM_PRODUCT_KEY, product.entityToJson().toString());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    //#
    //# MENU ACTION AND MERCHANT LIST WORKAROUND:
    //#

    private void initFab() {
        fabMain = findViewById(R.id.fabMain);
        if (fabMain != null) {
            fabMain.setOnClickListener(this::onFabMainClick);
        }
    }

    private void onFabMainClick(View view) {
        displayLoadingIndicator();
        requestMerchantList(MerchantManager.MerchantVisibility.ALL, new MerchantInterface() {
            @Override
            public void onGetMerchantsSuccess(List<Merchant> merchantList) {
                hideLoadingIndicator();
                if (merchantList != null && merchantList.size() > 0) {
                    navigateToProductListView(merchantList.get(0));
                } else {
                    showSnackbar(view, getString(R.string.missing_merchants), Color.YELLOW);
                }
            }

            @Override
            public void onGetShippingMethodsSuccess(List<SupportedDeliveryMethod> list) {
                hideLoadingIndicator();
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                hideLoadingIndicator();
                String errorMessage = String.format(
                        getString(R.string.rezolve_error_format),
                        rezolveError.getErrorType().toString(),
                        rezolveError.getErrorMessage().toString(),
                        rezolveError.getMessage() == null ? "NULL" : rezolveError.getMessage()
                );
                Log.e(TAG, errorMessage);
                showSnackbar(view, errorMessage, Color.RED);
            }
        });
    }

    private void requestMerchantList(MerchantManager.MerchantVisibility merchantVisibility, MerchantInterface merchantInterface) {
        MerchantManager merchantManager = rezolveSdk.getRezolveSession() == null ? null : rezolveSdk.getRezolveSession().getMerchantManager();
        if (merchantManager == null) {
            merchantInterface.onError(new RezolveError(RezolveError.RezolveErrorType.CUSTOM, RezolveError.RezolveErrorMessage.CUSTOM, "Missing Merchant Manager"));
        } else {
            merchantManager.getMerchants(getApplicationContext(), merchantVisibility, merchantInterface);
        }
    }

    private void showSnackbar(View view, String message, @ColorInt int color) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(color);
        snackbar.show();
    }

    private void navigateToProductListView(Merchant merchant) {
        Intent intent = new Intent(ScanActivity.this, ProductListActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString(ProductListActivity.PARAM_MERCHANT_ID_KEY, merchant.getId());
        bundle.putString(ProductListActivity.PARAM_MERCHANT_BANNER_KEY, merchant.getBanner());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    //#
    //# MENU ACTION AND MERCHANT LIST WORKAROUND ^^^
    //#

}