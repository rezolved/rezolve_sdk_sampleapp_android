package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.model.AuthenticationResponse;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.services.callbacks.AuthenticationCallback;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.TokenUtils;

import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String deviceId;

    private AuthenticationService authenticationService;
    private RezolveSDK rezolveSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticationService = new AuthenticationService();
        loginUser();
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
        rezolveSDK = new RezolveSDK.Builder()
                .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
                .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
                .setAuthRequestProvider(() -> {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        throw new IllegalStateException("You can't run this method from main thread");
                    }
                    return RezolveSDK.GetAuthRequest.authorizationHeader(response.getToken());
                })
                .build();

        rezolveSDK.setAuthToken(response.getToken());
        rezolveSDK.setDeviceIdHeader(deviceId);
        SdkProvider.getInstance().init(rezolveSDK);

        rezolveSDK.createSession(response.getToken(), response.getEntityId(), response.getPartnerId(), new RezolveInterface() {
            @Override
            public void onInitializationSuccess(RezolveSession rezolveSession, String partnerId, String entityId) {
                navigateToScanView();
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                DialogUtils.showError(MainActivity.this, rezolveError.getMessage());
            }
        });
    }

    private void navigateToScanView() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
