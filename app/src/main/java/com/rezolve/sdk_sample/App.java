package com.rezolve.sdk_sample;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.rezolve.sdk.HttpClientConfig;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.api.authentication.auth0.AuthParams;
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory;
import com.rezolve.sdk.api.authentication.auth0.SspHttpClient;
import com.rezolve.sdk.old_ssp.managers.SspActManager;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.resolver.ResolverConfiguration;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.shared.MainActivityProvider;
import com.rezolve.shared.SspActManagerProvider;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.ProductUtils;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.authentication.SampleAuthRequestProvider;

import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class App extends Application implements SspActManagerProvider, MainActivityProvider {

    private SspActManager sspActManager;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();
        authenticationService.init(BuildConfig.DEMO_AUTH_SERVER, BuildConfig.REZOLVE_SDK_API_KEY);

        RezolveSDK rezolveSDK = new RezolveSDK.Builder()
                .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
                .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
                .setAuthRequestProvider(new SampleAuthRequestProvider())
                .build();
        SdkProvider.getInstance().init(rezolveSDK);

        AuthParams authParams = new AuthParams(
                                    BuildConfig.AUTH0_CLIENT_ID,
                                    BuildConfig.AUTH0_CLIENT_SECRET,
                                    BuildConfig.AUTH0_API_KEY,
                                    BuildConfig.AUTH0_AUDIENCE,
                                    BuildConfig.AUTH0_ENDPOINT,
                                    BuildConfig.SSP_ENGAGEMENT_ENDPOINT,
                                    BuildConfig.SSP_ACT_ENDPOINT
                                );

        HttpClientConfig httpConfig = new HttpClientConfig.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        HttpClientFactory httpClientFactory = new HttpClientFactory.Builder()
                .setHttpClientConfig(httpConfig)
                .setAuthParams(authParams)
                .build();

        SspHttpClient sspHttpClient = httpClientFactory.createHttpClient(BuildConfig.SSP_ENDPOINT);

        sspActManager = new SspActManager(sspHttpClient, rezolveSDK);

        new ResolverConfiguration.Builder(rezolveSDK)
                .enableBarcode1dResolver(true)
                .enableSspResolver(sspActManager, 400)
                .build(this);
    }

//    private void navigateToSspActView(SspAct act) {
//        Intent intent = new Intent(this, SspActActivity.class);
//        Bundle bundle = ProductUtils.toBundle(act);
//        intent.putExtras(bundle);
//        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    @Override
    public SspActManager getSspActManager() {
        return sspActManager;
    }

    @Override
    public Class<?> getMainActivity() {
        return ScanActivity.class;
    }
}
