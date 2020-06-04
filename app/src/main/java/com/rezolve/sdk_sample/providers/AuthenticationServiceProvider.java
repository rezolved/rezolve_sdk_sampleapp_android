package com.rezolve.sdk_sample.providers;

import com.rezolve.sdk_sample.services.AuthenticationService;

public class AuthenticationServiceProvider {

    private static class BillPughSingleton {
        private static final AuthenticationService INSTANCE = new AuthenticationService();
    }

    public static AuthenticationService getAuthenticationService() {
        return BillPughSingleton.INSTANCE;
    }

}
