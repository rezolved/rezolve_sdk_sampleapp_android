package com.rezolve.sdk_sample;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.rezolve.sdk.HttpClientConfig;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.api.HttpClient;
import com.rezolve.sdk.api.authentication.auth0.AuthParams;
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory;
import com.rezolve.sdk.api.authentication.auth0.SspHttpClient;
import com.rezolve.sdk.core.interfaces.PaymentOptionInterface;
import com.rezolve.sdk.core.interfaces.TriggerInterface;
import com.rezolve.sdk.location.google.LocationProviderFused;
import com.rezolve.sdk.model.cart.CheckoutProduct;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.PaymentOption;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.ScannedData;
import com.rezolve.sdk.ssp.helper.GeozoneNotificationCallbackHelper;
import com.rezolve.sdk.ssp.helper.NotificationChannelProperties;
import com.rezolve.sdk.ssp.helper.NotificationHelper;
import com.rezolve.sdk.ssp.helper.NotificationHelperImpl;
import com.rezolve.sdk.ssp.helper.NotificationProperties;
import com.rezolve.sdk.ssp.interfaces.GeofenceEngagementsListener;
import com.rezolve.sdk.ssp.interfaces.GeozoneNotificationCallback;
import com.rezolve.sdk.ssp.interfaces.SspFromEngagementInterface;
import com.rezolve.sdk.ssp.managers.GeofenceManager;
import com.rezolve.sdk.ssp.managers.SspActManager;
import com.rezolve.sdk.ssp.model.EngagementsUpdatePolicy;
import com.rezolve.sdk.ssp.model.GeolocationTriggeredEngagement;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspCategory;
import com.rezolve.sdk.ssp.model.SspObject;
import com.rezolve.sdk.ssp.model.SspProduct;
import com.rezolve.sdk.ssp.resolver.ResolverConfiguration;
import com.rezolve.sdk.ssp.resolver.result.SspActResult;
import com.rezolve.sdk_sample.providers.AuthenticationServiceProvider;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.services.AuthenticationService;
import com.rezolve.sdk_sample.sspact.SspActActivity;
import com.rezolve.sdk_sample.utils.ProductUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.ACTION_GEOFENCE_NOTIFICATION_DISPLAYED;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.ACTION_GEOFENCE_NOTIFICATION_SELECTED;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_ACT_ID;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_DESCRIPTION_SHORT;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_NAME;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_SENDER_PACKAGE_NAME;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_SSP_ACT;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_SSP_CATEGORY;
import static com.rezolve.sdk.ssp.managers.GeofenceManager.Const.KEY_SSP_PRODUCT;

public class App extends Application {

    private static final String TAG = "App";

    private static final String GEOFENCE_FOREGROUND_CHANNEL_ID = "1";
    private static final String ENGAGEMENTS_ALERTS_CHANNEL_ID = "2";

    private SspActManager sspActManager;

    private RezolveSDK.AuthRequestProvider authRequestProvider = new RezolveSDK.AuthRequestProvider() {

        private final
        AuthenticationService authenticationService = AuthenticationServiceProvider.getAuthenticationService();

        @NonNull
        @Override
        public RezolveSDK.GetAuthRequest getAuthRequest() {
            Log.d(TAG, "getAuthRequest");
            final String tokenJson = authenticationService.ping();
            if(TextUtils.isEmpty(tokenJson)) {
                return RezolveSDK.GetAuthRequest.error(new RezolveError(new IOException("Empty token")));
            } else {
                return RezolveSDK.GetAuthRequest.authorizationHeader(tokenJson);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        RezolveSDK rezolveSDK = new RezolveSDK.Builder()
                .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
                .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
                .setAuthRequestProvider(authRequestProvider)
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

        sspActManager = new SspActManager(sspHttpClient);

        new ResolverConfiguration.Builder(rezolveSDK)
                .enableBarcode1dResolver(true)
                .enableCoreResolver(true)
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

        NotificationHelper notificationHelper = new NotificationHelperImpl(this);

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
    }

    private void navigateToSspActView(SspAct act) {
        Intent intent = new Intent(this, SspActActivity.class);
        Bundle bundle = ProductUtils.toBundle(act);
        intent.putExtras(bundle);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public SspActManager getSspActManager() {
        return sspActManager;
    }
}
