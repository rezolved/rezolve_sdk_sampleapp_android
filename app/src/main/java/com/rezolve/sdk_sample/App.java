package com.rezolve.sdk_sample;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.rezolve.sdk.HttpClientConfig;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.api.authentication.auth0.AuthParams;
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory;
import com.rezolve.sdk.api.authentication.auth0.SspHttpClient;
import com.rezolve.sdk.location.google.LocationProviderFused;
import com.rezolve.sdk.old_ssp.helper.GeozoneNotificationCallbackHelper;
import com.rezolve.sdk.old_ssp.interfaces.GeofenceEngagementsListener;
import com.rezolve.sdk.old_ssp.interfaces.GeozoneNotificationCallback;
import com.rezolve.sdk.old_ssp.managers.GeofenceManager;
import com.rezolve.sdk.old_ssp.managers.SspActManager;
import com.rezolve.sdk.ssp.helper.NotificationChannelProperties;
import com.rezolve.sdk.ssp.helper.NotificationProperties;
import com.rezolve.sdk.ssp.managers.RemoteScanResolver;
import com.rezolve.sdk.ssp.model.EngagementsUpdatePolicy;
import com.rezolve.sdk.ssp.model.GeolocationTriggeredEngagement;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspObject;
import com.rezolve.sdk.ssp.resolver.ResolverConfiguration;
import com.rezolve.sdk_sample.providers.RemoteScanResolverProvider;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.shared.MainActivityProvider;
import com.rezolve.shared.SspActManagerProvider;
import com.rezolve.shared.sspact.SspActActivity;
import com.rezolve.shared.utils.ProductUtils;
import com.rezolve.shared.authentication.AuthenticationService;
import com.rezolve.shared.authentication.AuthenticationServiceProvider;
import com.rezolve.shared.authentication.SampleAuthRequestProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class App extends Application implements SspActManagerProvider, MainActivityProvider {

    private static final String TAG = "App";

    private static final String GEOFENCE_FOREGROUND_CHANNEL_ID = "1";
    private static final String ENGAGEMENTS_ALERTS_CHANNEL_ID = "2";

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

        NotificationProperties geofenceForegroundNotificationProperties = new NotificationProperties(
                GEOFENCE_FOREGROUND_CHANNEL_ID,
                R.drawable.ic_slider_head,
                ContextCompat.getColor(this, R.color.colorAccent),
                NotificationCompat.PRIORITY_MIN,
                0,
                null,
                null,
                true
        );

        NotificationProperties geofenceAlertNotificationProperties = new NotificationProperties(
                ENGAGEMENTS_ALERTS_CHANNEL_ID,
                R.drawable.ic_slider_head,
                ContextCompat.getColor(this, R.color.red),
                NotificationCompat.PRIORITY_HIGH,
                Notification.DEFAULT_ALL,
                new long[] {1000, 1000, 1000, 1000, 1000},
                Settings.System.DEFAULT_NOTIFICATION_URI,
                true
        );

        List<NotificationChannelProperties> geofenceLocationChannels = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            geofenceLocationChannels.add(new NotificationChannelProperties(
                    GEOFENCE_FOREGROUND_CHANNEL_ID,
                    "Geofence and Location Detection",
                    "",
                    NotificationManager.IMPORTANCE_LOW,
                    false,
                    false,
                    null,
                    false
            ));
            geofenceLocationChannels.add(new NotificationChannelProperties(
                    ENGAGEMENTS_ALERTS_CHANNEL_ID,
                    "Engagements Alerts",
                    "",
                    NotificationManager.IMPORTANCE_HIGH,
                    true,
                    true,
                    Settings.System.DEFAULT_NOTIFICATION_URI,
                    false
            ));
        }

        final GeofenceManager geofenceManager = new GeofenceManager.Builder()
                .sspActManager(sspActManager)
                .engagementsUpdatePolicy(new EngagementsUpdatePolicy.Builder()
                        .silencePeriodMS(TimeUnit.MINUTES.toMillis(5))
                        .maxCacheTimeMS(TimeUnit.MINUTES.toMillis(5))
                        .build())
                .notificationChannelPropertiesList(geofenceLocationChannels)
                .engagementAlertNotification(geofenceAlertNotificationProperties)
                .context(this)
                .build();

        final LocationProviderFused locationProviderFused = LocationProviderFused.create(this);
        locationProviderFused.start();
        geofenceManager.startGeofenceTracking();

        geofenceManager.addEngagementListener(new GeofenceEngagementsListener() {
            @Override
            public void onGeolocationTriggeredEngagementUpdate(List<GeolocationTriggeredEngagement> list) {
                Log.d(TAG, "onGeolocationTriggeredEngagementUpdate: " + list);
            }
        });

        GeozoneNotificationCallbackHelper.getInstance().addCallback(new GeozoneNotificationCallback() {
            @Override
            public void onDisplayed(@NonNull SspObject sspObject, @NonNull GeolocationTriggeredEngagement engagement) {
                Log.d(TAG, "onDisplayed: " + sspObject + ", " + engagement);
            }

            @Override
            public boolean onSelected(@NonNull SspObject sspObject) {
                Log.d(TAG, "onSelected: " + sspObject);
                if (sspObject instanceof SspAct) {
                    SspAct act = (SspAct)sspObject;
                    if (act.getPageBuildingBlocks() != null && !act.getPageBuildingBlocks().isEmpty()) {
                        navigateToSspActView(act);
                    }
                }
                return false;
            }
        });

        RemoteScanResolverProvider.getInstance().init(new RemoteScanResolver(sspActManager, sspHttpClient));
    }

    private void navigateToSspActView(SspAct act) {
        Intent intent = new Intent(this, SspActActivity.class);
        Bundle bundle = ProductUtils.toBundle(act);
        intent.putExtras(bundle);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
