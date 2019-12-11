package com.rezolve.sdk_sample.services.callbacks;

import com.rezolve.sdk_sample.model.AuthenticationResponse;

public interface AuthenticationCallback {
    void onLoginSuccess(AuthenticationResponse response);

    void onLoginFailure(String message);
}
