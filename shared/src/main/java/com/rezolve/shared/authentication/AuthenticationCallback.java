package com.rezolve.shared.authentication;

public interface AuthenticationCallback {
    void onLoginSuccess(AuthenticationResponse response);

    void onLoginFailure(String message);
}
