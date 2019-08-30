package com.rezolve.sdk_sample.providers;

import android.support.annotation.NonNull;

import com.rezolve.sdk.RezolveSDK;

public class SdkProvider {
    private static final SdkProvider instance = new SdkProvider();

    private RezolveSDK rezolveSDK;

    public static SdkProvider getInstance() {
        return instance;
    }

    private SdkProvider() {
    }

    public void init(@NonNull RezolveSDK rezolveSDK) {
        this.rezolveSDK = rezolveSDK;
    }

    @NonNull
    public RezolveSDK getSDK() {
        return rezolveSDK;
    }
}
