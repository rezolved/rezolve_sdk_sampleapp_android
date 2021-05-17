package com.rezolve.sdk_sample.providers;

import androidx.annotation.NonNull;

import com.rezolve.sdk.ssp.managers.RemoteScanResolver;

public class RemoteScanResolverProvider {
    private static final RemoteScanResolverProvider instance = new RemoteScanResolverProvider();

    private RemoteScanResolver remoteScanResolver;

    public static RemoteScanResolverProvider getInstance() {
        return instance;
    }

    private RemoteScanResolverProvider() {
    }

    public void init(@NonNull RemoteScanResolver remoteScanResolver) {
        this.remoteScanResolver = remoteScanResolver;
    }

    @NonNull
    public RemoteScanResolver getRemoteScanResolver() {
        return remoteScanResolver;
    }
}
