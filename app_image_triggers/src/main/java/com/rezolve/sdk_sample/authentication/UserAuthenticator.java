package com.rezolve.sdk_sample.authentication;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.BuildConfig;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.shared.authentication.AuthenticationCallback;
import com.rezolve.shared.authentication.AuthenticationResponse;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.utils.DeviceUtils;
import com.rezolve.shared.utils.DialogUtils;

/**
 *
 */
public class UserAuthenticator {

    private Activity activity;
    private Callback callback;
    private String deviceId;
    private final AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();
    private final RezolveSDK rezolveSDK = SdkProvider.getInstance().getSDK();


    public UserAuthenticator(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.deviceId = DeviceUtils.getDeviceId(activity);
    }

    public void loginUser() {
        authenticationService.login(BuildConfig.DEMO_AUTH_USER, BuildConfig.DEMO_AUTH_PASSWORD, deviceId, new AuthenticationCallback() {
            @Override
            public void onLoginSuccess(AuthenticationResponse response) {
                // once your Authentication service has provided required data from Rezolve systems, you can now establish RezolveSDK session.
                createSession(response);
            }

            @Override
            public void onLoginFailure(String message) {
                DialogUtils.showError(activity, message);
            }
        });
    }

    private void createSession(AuthenticationResponse response) {

        rezolveSDK.setAuthToken(response.getToken());
        rezolveSDK.setDeviceIdHeader(deviceId);

        rezolveSDK.createSession(response.getToken(), response.getEntityId(), response.getPartnerId(), null, new RezolveInterface() {
            @Override
            public void onInitializationSuccess(RezolveSession rezolveSession, String partnerId, String entityId) {
                callback.onInitializationSuccess();
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                DialogUtils.showError(activity, rezolveError.getMessage());
            }
        });
    }

    public interface Callback {
        void onInitializationSuccess();
    }
}
