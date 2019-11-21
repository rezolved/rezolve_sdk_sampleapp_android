package com.rezolve.sdk_sample.model;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {
    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    private String token;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
