package com.rezolve.sdk_sample.scan;

import static com.rezolve.sdk_sample.utils.NotificationUtil.isLaunchedFromNotification;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.rezolve.scan.audio.AudioScanManagerProvider;
import com.rezolve.scan.core.audio.AudioScanManager;
import com.rezolve.scan.core.errors.CameraError;
import com.rezolve.scan.core.errors.ReaderError;
import com.rezolve.scan.core.errors.ReaderException;
import com.rezolve.scan.core.video.ScanHint;
import com.rezolve.scan.core.video.VideoScanManager;
import com.rezolve.scan.core.video.VideoScanManagerListener;
import com.rezolve.scan.video.VideoScanManagerProvider;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.resolver.ResolverError;
import com.rezolve.sdk.resolver.UrlTrigger;
import com.rezolve.sdk.ssp.resolver.ResolveResultListener;
import com.rezolve.sdk.ssp.resolver.ResolverResultListenersRegistry;
import com.rezolve.sdk.ssp.resolver.result.ContentResult;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.authentication.UserAuthenticator;
import com.rezolve.sdk_sample.navigation.Navigator;
import com.rezolve.sdk_sample.utils.NotificationUtil;
import com.rezolve.shared.utils.DialogUtils;

import java.util.List;
import java.util.UUID;

public class ScanActivity extends AppCompatActivity implements UserAuthenticator.Callback, ScanCallback {

    private static final String TAG = ScanActivity.class.getSimpleName();

    private ScanViewModel scanViewModel;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private TextView hintView;
    private PreviewView previewView;
    private SpinKitView loadingSpinView;
    private final VideoScanManager videoScanManager = VideoScanManagerProvider.getVideoScanManager();
    private final AudioScanManager audioScanManager = AudioScanManagerProvider.getAudioScanManager();

    @Override
    public void onInitializationSuccess() {
        if(isLaunchedFromNotification(this)) {
            NotificationUtil.launch(getIntent(), this);
        } else {
            initFab();
            findViewById(R.id.authenticationStatusTextView).setVisibility(View.GONE);
        }
    }

    private final ResolveResultListener resolveResultListener = new ResolveResultListener() {
        @Override
        public void onProcessingStarted(@NonNull UUID uuid) {
            Log.d(TAG, "onProcessingStarted: " + uuid);
            showLoadingIndicator();
        }

        @Override
        public void onProcessingFinished(@NonNull UUID uuid) {
            Log.d(TAG, "onProcessingFinished: " + uuid);
            hideLoadingIndicator();
        }

        @Override
        public void onProcessingUrlTriggerStarted(@NonNull UUID uuid, @NonNull UrlTrigger urlTrigger) {
            Log.d(TAG, "onProcessingUrlTriggerStarted: " + uuid + " url: " + urlTrigger.getUrl());
        }

        @Override
        public void onContentResult(@NonNull UUID uuid, @NonNull ContentResult result) {
            Log.d(TAG, "onContentResult: " + result);
            scanViewModel.onContentResult(result);
        }

        @Override
        public void onResolverError(@NonNull UUID uuid, @NonNull ResolverError resolverError) {
            Log.d(TAG, "onResolverError: " + resolverError);
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

        scanViewModel = new ScanViewModel(this);
        new UserAuthenticator(this, this).loginUser();
    }

    @Override
    public void onStart() {
        super.onStart();
        videoScanManager.addVideoScanManagerListener(videoScanManagerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] scannerPermissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        ResolverResultListenersRegistry.getInstance().add(resolveResultListener);

        videoScanManager.attachScanView(previewView);
        startVideoScan();
        startAudioScan();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(scannerPermissions, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAudioScan();
        ResolverResultListenersRegistry.getInstance().remove(resolveResultListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopVideoScan();
        videoScanManager.removeVideoScanManagerListener(videoScanManagerListener);
    }


    /**
     *  Optional listener for more detailed callbacks
     */
    private final VideoScanManagerListener videoScanManagerListener = new VideoScanManagerListener() {

        private static final String TAG = "VSML";
        @Override
        public void onCameraAvailable() {
            Log.d(TAG, "onCameraAvailable");
        }

        @Override
        public void onCameraError(@NonNull CameraError cameraError) {
            Log.d(TAG, "onCameraError: "+cameraError);
        }

        @Override
        public void onNewScanHint(@Nullable ScanHint hint) {
            if (hint != null) {
                runOnUiThread(() -> hintView.setText(hint.name()));
            }
        }

        @Override
        public void onReaderError(@NonNull ReaderError readerError) {
            Log.d(TAG, "onReaderError: "+readerError.getMessage());
        }

        @Override
        public void onReaderException(@NonNull ReaderException readerException) {
            Log.d(TAG, "onReaderException: "+readerException.getMessage());
            readerException.printStackTrace();
        }

        @Override
        public void onReaderResult(int newPayloadsSize) {
            Log.d(TAG, "how many new payloads detected: "+newPayloadsSize);
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

    private void startAudioScan() {
        audioScanManager.clearCache();

        if (wasMicrophonePermissionGranted()) {
            audioScanManager.startAudioScan();
        }
    }

    private void stopAudioScan() {
        audioScanManager.stopAudioScan();
        audioScanManager.destroy();
    }

    private boolean wasCameraPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean wasMicrophonePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void initFab() {
        FloatingActionButton fabMain = findViewById(R.id.fabMain);
        if (fabMain != null) {
            fabMain.setOnClickListener(view -> scanViewModel.onFabMainClick());
        }
    }

    private void onScanError(RezolveError.RezolveErrorType rezolveErrorType, String errorMsg) {
        hideLoadingIndicator();
        DialogUtils.showError(this, rezolveErrorType.name() + "\n" + errorMsg);
    }

    @Override
    public void showLoadingIndicator() {
        runOnUiThread(() -> loadingSpinView.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideLoadingIndicator() {
        runOnUiThread(() -> loadingSpinView.setVisibility(View.GONE));
    }

    @Override
    public void onSnackbarMessage(@StringRes int message) {
        View view = getWindow().getDecorView().getRootView();
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(Color.YELLOW);
        snackbar.show();
    }

    @Override
    public void showProductDetails(Product product) {
        Navigator.navigateToProductDetails(product, this);
    }

    @Override
    public void showSspActView(SspActResult act) {
        Navigator.navigateToSspActView(act, this);
    }

    @Override
    public void showProductListView(Merchant merchantDetails, Category category) {
        Navigator.navigateToProductListView(merchantDetails, category, this);
    }

    @Override
    public void showMerchantSelector(List<Merchant> merchants) {
        DialogUtils.showSelector(
                ScanActivity.this,
                getString(R.string.select_merchant_title),
                merchants,
                (spinnerView, item) -> Navigator.navigateToProductListView(item, null, this)

        );
    }

    @Override
    public void onToastMessage(String text) {
        Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show();
    }
}
