package com.rezolve.sdk_sample.model;

import com.google.gson.annotations.SerializedName;

public class PingResponse {

    @SerializedName("accessToken")
    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
