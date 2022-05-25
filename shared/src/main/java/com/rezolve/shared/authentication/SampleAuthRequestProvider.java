package com.rezolve.shared.authentication;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.model.network.RezolveError;

import java.io.IOException;

public class SampleAuthRequestProvider implements RezolveSDK.AuthRequestProvider {

    private final AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();

    @NonNull
    @Override
    public RezolveSDK.GetAuthRequest getAuthRequest() {
        Log.d("SampleAuthRequest", "getAuthRequest");
        final String tokenJson = authenticationService.ping();
        if(TextUtils.isEmpty(tokenJson)) {
            return RezolveSDK.GetAuthRequest.error(new RezolveError(new IOException("Empty token")));
        } else {
            return RezolveSDK.GetAuthRequest.authorizationHeader(tokenJson);
        }
    }
}
