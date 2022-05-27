package com.rezolve.sdk_sample;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.resolver.ResolverError;
import com.rezolve.sdk.resolver.UrlTrigger;
import com.rezolve.sdk.scan.audio.AudioScanManager;
import com.rezolve.sdk.scan.audio.AudioScanManagerProvider;
import com.rezolve.sdk.scan.video.VideoScanManager;
import com.rezolve.sdk.scan.video.VideoScanManagerProvider;
import com.rezolve.sdk.ssp.resolver.ResolveResultListener;
import com.rezolve.sdk.ssp.resolver.ResolverResultListenersRegistry;
import com.rezolve.sdk.ssp.resolver.result.CategoryResult;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;
import com.rezolve.sdk.ssp.resolver.result.ProductResult;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk.ssp.resolver.result.SspCategoryResult;
import com.rezolve.sdk.ssp.resolver.result.SspProductResult;
import com.rezolve.shared.ProductDetailsActivity;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.DialogUtils;
import com.rezolve.shared.utils.ProductUtils;
import com.rezolve.shared.utils.sdk.MerchantManagerUtils;
import com.rezolve.shared.utils.sdk.RezolveSdkUtils;

import java.util.List;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private SpinKitView loadingSpinView;
    private FloatingActionButton fabMain;
    private VideoScanManager videoScanManager = VideoScanManagerProvider.getVideoScanManager();
    private AudioScanManager audioScanManager = AudioScanManagerProvider.getAudioScanManager();

    private final ResolveResultListener resolveResultListener = new ResolveResultListener() {
        @Override
        public void onProcessingStarted(@NonNull UUID uuid) {
            Log.d(TAG, "onProcessingStarted: " + uuid);
            processingStarted();
        }

        @Override
        public void onProcessingFinished(@NonNull UUID uuid) {
            Log.d(TAG, "onProcessingFinished: " + uuid);
            processingFinished();
        }

        @Override
        public void onProcessingUrlTriggerStarted(@NonNull UUID uuid, @NonNull UrlTrigger urlTrigger) {
            Log.d(TAG, "onProcessingUrlTriggerStarted: " + uuid + " url: " + urlTrigger.getUrl());
        }

        @Override
        public void onContentResult(@NonNull UUID uuid, @NonNull ContentResult result) {
            Log.d(TAG, "onContentResult: " + result);
            if(result instanceof ProductResult) {
                ProductResult productResult = (ProductResult) result;
                onProductResult(productResult.getProduct(), productResult.getCategoryId());
            } else if(result instanceof CategoryResult) {
                CategoryResult categoryResult = (CategoryResult) result;
                onCategoryResult(categoryResult.getCategory(), categoryResult.getMerchantId());
            } else if(result instanceof SspActResult) {
                SspActResult act = (SspActResult) result;
                if (act.sspAct.getPageBuildingBlocks() != null && !act.sspAct.getPageBuildingBlocks().isEmpty()) {
                    onSspActResult(act);
                }
            } else if(result instanceof SspProductResult) {
                SspProductResult sspProductResult = (SspProductResult) result;
                toastRezolveTrigger(sspProductResult.getSspProduct().getEngagementName(),
                        sspProductResult.getSspProduct().getRezolveTrigger());
            } else if(result instanceof SspCategoryResult) {
                SspCategoryResult sspCategoryResult = (SspCategoryResult) result;
                toastRezolveTrigger(sspCategoryResult.getSspCategory().getEngagementName(),
                        sspCategoryResult.getSspCategory().getRezolveTrigger());
            }
        }

        private void toastRezolveTrigger(String name, String rezolveTrigger) {
            Toast.makeText(getBaseContext(), name + " - " + rezolveTrigger, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResolverError(@NonNull UUID uuid, @NonNull ResolverError resolverError) {
            if(resolverError instanceof  ResolverError.Error) {
                ResolverError.Error error = (ResolverError.Error) resolverError;
                onScanError(error.rezolveError.getErrorType(), error.message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        loadingSpinView = findViewById(R.id.loadingSpinView);

        videoScanManager.createImageReader();

        initFab();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoScanManager.destroyImageReader();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ResolverResultListenersRegistry.getInstance().add(resolveResultListener);
        String[] scannerPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        Permissions.check(this, scannerPermissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                initializeScanner();
            }
        });

        videoScanManager.clearCache();
        videoScanManager.startCamera();
        videoScanManager.attachReader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoScanManager.detachReader();
        audioScanManager.stopAudioScan();
        audioScanManager.destroy();
        ResolverResultListenersRegistry.getInstance().remove(resolveResultListener);
    }

    private void initializeScanner() {
        audioScanManager.clearCache();
        audioScanManager.startAudioScan();
    }

    public void onProductResult(Product product, String categoryId) {
        navigateToProductDetailsView(product);
    }

    public void onCategoryResult(Category category, String merchantId) {
        navigateToProductListView(merchantId, category);
    }

    private void onSspActResult(SspActResult act) {
        navigateToSspActView(act);
    }

    public void onScanError(RezolveError.RezolveErrorType rezolveErrorType, String errorMsg) {
        hideLoadingIndicator();
        DialogUtils.showError(this, rezolveErrorType.name() + "\n" + errorMsg);
    }

    public void processingStarted() {
        displayLoadingIndicator();
    }

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
        Bundle bundle = ProductUtils.toBundle(product);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void navigateToSspActView(SspActResult act) {
        Intent intent = new Intent(ScanActivity.this, SspActActivity.class);
        Bundle bundle = ProductUtils.toBundle(act.getSspAct());
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
