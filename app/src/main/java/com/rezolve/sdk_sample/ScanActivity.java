package com.rezolve.sdk_sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.ybq.android.spinkit.SpinKitView;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.interfaces.ScanManagerInterface;
import com.rezolve.sdk.core.managers.ScanManager;
import com.rezolve.sdk.model.foreign.RezolveScanResult;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.ScannedData;
import com.rezolve.sdk.views.RezolveScanView;

public class ScanActivity extends AppCompatActivity implements ScanManagerInterface {

    private RezolveSDK rezolveSdk;

    private SpinKitView loadingSpinView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        loadingSpinView = findViewById(R.id.loadingSpinView);
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
}