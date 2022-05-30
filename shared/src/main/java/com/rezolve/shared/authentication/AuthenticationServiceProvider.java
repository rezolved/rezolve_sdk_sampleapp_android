package com.rezolve.shared.authentication;

public class AuthenticationServiceProvider {

    private static class BillPughSingleton {
        private static final AuthenticationService INSTANCE = new AuthenticationService();
    }

    public static AuthenticationService getAuthenticationService() {
        return BillPughSingleton.INSTANCE;
    }

}
