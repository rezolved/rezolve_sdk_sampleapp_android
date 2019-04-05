package com.rezolve.sdk_sample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.rezolve.sdk.RezolveInterface;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.model.RegistrationResponse;
import com.rezolve.sdk_sample.services.AuthenticationInterface;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.TokenUtils;

public class MainActivity extends AppCompatActivity {

    private String mEntityId;
    private String mPartnerId;
    private String mDeviceId;

    private AuthenticationService mAuthenticationService;

    private RezolveSDK mRezolveSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuthenticationService = new AuthenticationService();
        registerUser();
    }

    private void registerUser() {
        mAuthenticationService.register(new AuthenticationInterface() {
            @Override
            public void onRegistrationSuccess(RegistrationResponse response) {
                mEntityId = response.getEntityId();
                mPartnerId = response.getPartnerId();

                createSession();
            }

            @Override
            public void onRegistrationFailure() {
                // TODO AlertDialog call
            }
        });
    }

    private void createSession() {
        mDeviceId = DeviceUtils.getDeviceId(this);
        String accessToken = TokenUtils.createAccessToken(mEntityId, mPartnerId, mDeviceId);

        mRezolveSDK = new RezolveSDK.Builder()
                .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
                .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
                .build();

        mRezolveSDK.setAuthToken(accessToken);

        mRezolveSDK.createSession(accessToken, mEntityId, mPartnerId, new RezolveInterface() {
            @Override
            public void onInitializationSuccess(RezolveSession rezolveSession, String entityId, String partnerId) {
                mRezolveSDK.setDeviceIdHeader(mDeviceId);
            }

            @Override
            public void onInitializationFailure(@NonNull RezolveError rezolveError) {
                // TODO AlertDialog call
            }
        });

    }
}
