package com.rezolve.sdk_sample.services;

import com.rezolve.sdk_sample.model.RegistrationResponse;

public interface AuthenticationInterface {
    void onRegistrationSuccess(RegistrationResponse response);
    void onRegistrationFailure();
}
