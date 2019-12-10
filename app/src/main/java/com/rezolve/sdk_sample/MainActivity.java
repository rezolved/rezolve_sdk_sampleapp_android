package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.AutoDetectService;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk_sample.model.RegistrationResponse;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.services.BackgroundListeningService;
import com.rezolve.sdk_sample.services.callbacks.AuthenticationCallback;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.NotificationHelper;
import com.rezolve.sdk_sample.utils.ProductUtils;
import com.rezolve.sdk_sample.utils.TokenUtils;

import java.util.List;

import static com.rezolve.sdk.core.AutoDetectService.ACTION_PRODUCT_AUDIO_SCAN;

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

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener(this));

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
                if(isLaunchedFromNotification()) {
                    navigateToProductDetails();
                } else {
                    navigateToScanView();
                }
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                DialogUtils.showError(MainActivity.this, rezolveError.getMessage());
            }
        });
    }

    private boolean isLaunchedFromNotification() {
        return this.getIntent() != null
                && this.getIntent().getAction() != null
                && (this.getIntent().getAction().equals(ACTION_PRODUCT_AUDIO_SCAN));
    }

    private void navigateToScanView() {
        Intent intent = new Intent(MainActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void navigateToProductDetails() {
        // We want to ensure that BGL is stopped even after fresh launch from notification
        BackgroundListeningService.getInstance().stop(this, true);

        List list = AutoDetectService.getScannedObjects();
        String productId = getIntent().getStringExtra(NotificationHelper.PARCELABLE_EXTRA_ITEM_ID);

        Product product = ProductUtils.getProductFromList(list, productId);

        Intent intent = new Intent(MainActivity.this, ProductDetailsActivity.class);
        Bundle bundle = ProductUtils.toBundle(product);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
