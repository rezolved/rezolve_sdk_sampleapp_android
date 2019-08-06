package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.model.RegistrationResponse;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.services.callbacks.AuthenticationCallback;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.TokenUtils;

public class MainActivity extends AppCompatActivity {

    private String entityId;
    private String partnerId;
    private String deviceId;

    private AuthenticationService authenticationService;
    private RezolveSDK rezolveSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authenticationService = new AuthenticationService();
        registerUser();
    }

    private void registerUser() {
        authenticationService.register(new AuthenticationCallback() {
            @Override
            public void onRegistrationSuccess(RegistrationResponse response) {
                entityId = response.getEntityId();
                partnerId = response.getPartnerId();

                createSession();
            }

            @Override
            public void onRegistrationFailure(String message) {
                DialogUtils.showError(MainActivity.this, message);
            }
        });
    }

    private void createSession() {
        deviceId = DeviceUtils.getDeviceId(this);
        String accessToken = TokenUtils.createAccessToken(entityId, partnerId, deviceId);

        rezolveSDK = new RezolveSDK.Builder()
                .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
                .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
                .setAuthRequestProvider(() -> {
                    if (Looper.myLooper() == Looper.getMainLooper()) {
                        throw new IllegalStateException("You can't run this method from main thread");
                    }
                    return RezolveSDK.GetAuthRequest.authorizationHeader(accessToken);
                })
                .build();

        rezolveSDK.setAuthToken(accessToken);
        rezolveSDK.setDeviceIdHeader(deviceId);
        SdkProvider.getInstance().init(rezolveSDK);

        rezolveSDK.createSession(accessToken, entityId, partnerId, new RezolveInterface() {
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
