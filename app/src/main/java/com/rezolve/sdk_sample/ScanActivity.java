package com.rezolve.sdk_sample;

import static android.Manifest.permission.CAMERA;

import static com.rezolve.sdk_sample.utils.NotificationUtil.isLaunchedFromNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.scan.core.errors.CameraError;
import com.rezolve.scan.core.errors.ReaderError;
import com.rezolve.scan.core.errors.ReaderException;
import com.rezolve.scan.core.video.ScanHint;
import com.rezolve.scan.core.video.VideoScanManager;
import com.rezolve.scan.core.video.VideoScanManagerListener;
import com.rezolve.scan.video.VideoScanManagerProvider;
import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.resolver.Payload;
import com.rezolve.sdk.resolver.Resolvable;
import com.rezolve.sdk.resolver.ResolvedContent;
import com.rezolve.sdk.resolver.ResolverError;
import com.rezolve.sdk.resolver.ResolverListener;
import com.rezolve.sdk.resolver.ScanResultResolver;
import com.rezolve.sdk.resolver.UrlTrigger;
import com.rezolve.sdk.ssp.resolver.ResolveResultListener;
import com.rezolve.sdk.ssp.resolver.ResolverResultListenersRegistry;
import com.rezolve.sdk.ssp.resolver.result.CategoryResult;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;
import com.rezolve.sdk.ssp.resolver.result.ProductResult;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk.ssp.resolver.result.SspCategoryResult;
import com.rezolve.sdk.ssp.resolver.result.SspProductResult;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.utils.NotificationUtil;
import com.rezolve.shared.ProductDetailsActivity;
import com.rezolve.shared.authentication.AuthenticationCallback;
import com.rezolve.shared.authentication.AuthenticationResponse;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.DeviceUtils;
import com.rezolve.shared.utils.DialogUtils;
import com.rezolve.shared.utils.ProductUtils;
import com.rezolve.shared.utils.sdk.MerchantManagerUtils;
import com.rezolve.shared.utils.sdk.RezolveSdkUtils;

import java.util.List;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity implements MainNavigator {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private String deviceId;

    private AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();
    private RezolveSDK rezolveSDK = SdkProvider.getInstance().getSDK();

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private TextView hintView;
    private PreviewView previewView;
    private SpinKitView loadingSpinView;
    private final VideoScanManager videoScanManager = VideoScanManagerProvider.getVideoScanManager();
//    private final AudioScanManager audioScanManager = AudioScanManagerProvider.getAudioScanManager();

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
            if (result instanceof ProductResult productResult) {
                onProductResult(productResult.getProduct(), productResult.getCategoryId());
            } else if (result instanceof CategoryResult categoryResult) {
                onCategoryResult(categoryResult.getCategory(), categoryResult.getMerchantId());
            } else if (result instanceof SspActResult act) {
                if (act.sspAct.getPageBuildingBlocks() != null && !act.sspAct.getPageBuildingBlocks().isEmpty()) {
                    onSspActResult(act);
                }
            } else if (result instanceof SspProductResult sspProductResult) {
                toastRezolveTrigger(sspProductResult.getSspProduct().getEngagementName(), sspProductResult.getSspProduct().getRezolveTrigger());
            } else if (result instanceof SspCategoryResult sspCategoryResult) {
                toastRezolveTrigger(sspCategoryResult.getSspCategory().getEngagementName(), sspCategoryResult.getSspCategory().getRezolveTrigger());
            }
        }

        private void toastRezolveTrigger(String name, String rezolveTrigger) {
            Toast.makeText(getBaseContext(), name + " - " + rezolveTrigger, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResolverError(@NonNull UUID uuid, @NonNull ResolverError resolverError) {
            if (resolverError instanceof ResolverError.Error error) {
                onScanError(error.rezolveError.getErrorType(), error.message);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        previewView = findViewById(R.id.scanView);
        hintView = findViewById(R.id.rezolve_scan_hint);
        videoScanManager.attachScanView(previewView);
        loadingSpinView = findViewById(R.id.loadingSpinView);
        loginUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        videoScanManager.addVideoScanManagerListener(videoScanManagerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ResolverResultListenersRegistry.getInstance().add(resolveResultListener);
        String[] scannerPermissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        Permissions.check(this, scannerPermissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
//                initializeScanner();
            }
        });

        //      String[] locationPermissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
        //        Permissions.check(this, locationPermissions, null, null, new PermissionHandler() {
        //            @Override
        //            public void onGranted() {
        //                LocationDependencyProvider.locationProvider().start();
        //            }
        //        });

        videoScanManager.attachScanView(previewView);
        startVideoScan();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        audioScanManager.stopAudioScan();
//        audioScanManager.destroy();
        ResolverResultListenersRegistry.getInstance().remove(resolveResultListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVideoScan();
        videoScanManager.removeVideoScanManagerListener(videoScanManagerListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVideoScan();
    }

    private void loginUser() {
        deviceId = DeviceUtils.getDeviceId(this);
        authenticationService.login(BuildConfig.DEMO_AUTH_USER, BuildConfig.DEMO_AUTH_PASSWORD, deviceId, new AuthenticationCallback() {
            @Override
            public void onLoginSuccess(AuthenticationResponse response) {
                createSession(response);
            }

            @Override
            public void onLoginFailure(String message) {
                DialogUtils.showError(ScanActivity.this, message);
            }
        });
    }

    private void createSession(AuthenticationResponse response) {

        rezolveSDK.setAuthToken(response.getToken());
        rezolveSDK.setDeviceIdHeader(deviceId);

        rezolveSDK.createSession(response.getToken(), response.getEntityId(), response.getPartnerId(), null, new RezolveInterface() {
            @Override
            public void onInitializationSuccess(RezolveSession rezolveSession, String partnerId, String entityId) {
                if(isLaunchedFromNotification(ScanActivity.this)) {
                    NotificationUtil.launch(getIntent(), ScanActivity.this);
                } else {
                    initFab();
                    findViewById(R.id.authenticationStatusTextView).setVisibility(View.GONE);
                }
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                DialogUtils.showError(ScanActivity.this, rezolveError.getMessage());
            }
        });
    }

    private VideoScanManagerListener videoScanManagerListener = new VideoScanManagerListener() {

        private static final String TAG = "VSML";
        @Override
        public void onCameraAvailable() {
        }

        @Override
        public void onCameraError(@NonNull CameraError cameraError) {
        }

        @Override
        public void onNewScanHint(@Nullable ScanHint hint) {
            if (hint != null) {
                runOnUiThread(() -> hintView.setText(hint.name()));
            }
        }

        @Override
        public void onReaderError(@NonNull ReaderError readerError) {
        }

        @Override
        public void onReaderException(@NonNull ReaderException readerException) {
        }

        @Override
        public void onReaderResult(int newPayloadsSize) {
            Log.d("SCAN", "onReaderResult: "+newPayloadsSize);
            if (newPayloadsSize > 0) {
                if(getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                    runOnUiThread(() -> {
//                        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
//                        navController.navigate(R.id.navigation_notifications);
                    });
                }
            }
        }
    };

    @SuppressLint("MissingPermission")
    private void startVideoScan() {
        videoScanManager.clearCache();

        if (wasCameraPermissionGranted()) {
            videoScanManager.startCamera();
            videoScanManager.attachReader();
        }
    }

    private void stopVideoScan() {
        videoScanManager.detachReader();
        videoScanManager.stopCamera();
    }

    private boolean wasCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeScanner() {
//        audioScanManager.clearCache();
//        audioScanManager.startAudioScan();
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
        runOnUiThread(() -> loadingSpinView.setVisibility(View.VISIBLE));
    }

    private void hideLoadingIndicator() {
        runOnUiThread(() -> loadingSpinView.setVisibility(View.GONE));
    }

    @Override
    public void navigateToProductDetails(@NonNull Product product) {
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
        Log.e(TAG, rezolveError.getMessage());
        showSnackbar(view, rezolveError.getMessage(), Color.RED);
    }
    private void initFab() {
        FloatingActionButton fabMain = findViewById(R.id.fabMain);
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
                                    getString(R.string.select_merchant_title),
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

    private class BaseProcessingInterface implements RezolveSdkUtils.ProcessingInterface {

        private final View view;

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
