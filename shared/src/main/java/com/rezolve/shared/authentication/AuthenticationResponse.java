package com.rezolve.shared.authentication;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {
    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("sdkEntity")
    private String entityId;

    @SerializedName("sdkPartner")
    private String partnerId;

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

    public String getEntityId() {
        return entityId;
    }

    public String getPartnerId() {
        return partnerId;
    }
}
