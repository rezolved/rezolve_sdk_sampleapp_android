package com.rezolve.sdk_sample.services.callbacks;

import com.rezolve.sdk_sample.model.RegistrationResponse;

public interface AuthenticationCallback {
    void onRegistrationSuccess(RegistrationResponse response);
    void onRegistrationFailure();
}
