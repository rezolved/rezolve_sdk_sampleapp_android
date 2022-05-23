package com.rezolve.sdk_sample;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.location.LocationDependencyProvider;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk_sample.model.AuthenticationResponse;
import com.rezolve.sdk_sample.providers.AuthenticationServiceProvider;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.remote.ScanActivityRemote;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.services.callbacks.AuthenticationCallback;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.NotificationUtil;
import com.rezolve.sdk_sample.utils.ProductUtils;

import static com.rezolve.sdk_sample.utils.NotificationUtil.isLaunchedFromNotification;

public class MainActivity extends AppCompatActivity implements MainNavigator {

    private String deviceId;

    private AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();
    private RezolveSDK rezolveSDK = SdkProvider.getInstance().getSDK();

    private Button localScanBtn, remoteScanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginUser();
        prepareNavigationButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] locationPermissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
        Permissions.check(this, locationPermissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                LocationDependencyProvider.locationProvider().start();
            }
        });
    }

    private void prepareNavigationButtons() {
        localScanBtn = findViewById(R.id.local_scan_activity_btn);
        remoteScanBtn = findViewById(R.id.remote_scan_activity_btn);
        localScanBtn.setOnClickListener(view -> navigateToScanView());
        remoteScanBtn.setOnClickListener(view -> navigateToRemoteScanView());
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
                DialogUtils.showError(MainActivity.this, message);
            }
        });
    }

    private void createSession(AuthenticationResponse response) {

        rezolveSDK.setAuthToken(response.getToken());
        rezolveSDK.setDeviceIdHeader(deviceId);

        rezolveSDK.createSession(response.getToken(), response.getEntityId(), response.getPartnerId(), null, new RezolveInterface() {
            @Override
            public void onInitializationSuccess(RezolveSession rezolveSession, String partnerId, String entityId) {
                if(isLaunchedFromNotification(MainActivity.this)) {
                    NotificationUtil.launch(getIntent(), MainActivity.this);
                } else {
                    showNavigationButtons();
                }
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                DialogUtils.showError(MainActivity.this, rezolveError.getMessage());
            }
        });
    }

    private void showNavigationButtons() {
        localScanBtn.setVisibility(View.VISIBLE);
        remoteScanBtn.setVisibility(View.VISIBLE);
        findViewById(R.id.authenticationStatusTextView).setVisibility(View.GONE);
    }

    private void navigateToScanView() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToRemoteScanView() {
        Intent intent = new Intent(MainActivity.this, ScanActivityRemote.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void navigateToProductDetails(@NonNull Product product) {
        // We want to ensure that BGL is stopped even after fresh launch from notification

        Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
        Bundle bundle = ProductUtils.toBundle(product);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
