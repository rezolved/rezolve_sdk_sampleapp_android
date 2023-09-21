package com.rezolve.sdk_sample;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import com.rezolve.sdk.HttpClientConfig;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.api.authentication.auth0.AuthParams;
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory;
import com.rezolve.sdk.api.authentication.auth0.SspHttpClient;
import com.rezolve.sdk.old_ssp.managers.SspActManager;
import com.rezolve.sdk.ssp.resolver.ResolverConfiguration;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.scan.ScanActivity;
import com.rezolve.shared.MainActivityProvider;
import com.rezolve.shared.SspActManagerProvider;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.authentication.SampleAuthRequestProvider;

import java.util.concurrent.TimeUnit;

public class App extends Application implements SspActManagerProvider, MainActivityProvider {

    private SspActManager sspActManager;

    @Override
    public void onCreate() {
        super.onCreate();
        setupRezolveSDK();
    }

    private void setupRezolveSDK() {
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
// TODO this has to be fixed before the release. Right now RXP SBX produces QR codes with bad urls. https://rezolvetech.slack.com/archives/C042KJENSQL/p1693477828888649?thread_ts=1693297522.150259&cid=C042KJENSQL
// to detect RXP generated QR Codes define a host or set up an UrlVerificator.
// If your QR Codes match regex "^(https?://" + host + "/(resolve|instant)\\?engagementid=.*)" use SspUrlHost:
//                .setSspUrlHost("instant.eu.rezolve.com")
// Otherwise use UrlVerificator to create your own conditions, when QR Code should be resolved into an engagement.
// Remember that QR Code must carry valid engagement id for given host.
                .setUrlVerificator(url -> {
                    Uri uri = Uri.parse(url);
                    return uri.getHost().equals("instant.eu.rezolve.com") && !TextUtils.isEmpty(uri.getQueryParameter("engagementid"));
                })
                .build(this);
    }

    @Override
    public SspActManager getSspActManager() {
        return sspActManager;
    }

    @Override
    public Class<?> getMainActivity() {
        return ScanActivity.class;
    }
}
