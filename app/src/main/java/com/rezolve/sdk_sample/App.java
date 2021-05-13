package com.rezolve.sdk_sample;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.rezolve.sdk.HttpClientConfig;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.api.authentication.auth0.AuthParams;
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory;
import com.rezolve.sdk.api.authentication.auth0.SspHttpClient;
import com.rezolve.sdk.location.google.LocationProviderFused;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.ssp.helper.NotificationChannelProperties;
import com.rezolve.sdk.ssp.helper.NotificationProperties;
import com.rezolve.sdk.ssp.managers.GeofenceManager;
import com.rezolve.sdk.ssp.managers.RemoteScanResolver;
import com.rezolve.sdk.ssp.managers.SspActManager;
import com.rezolve.sdk.ssp.model.EngagementsUpdatePolicy;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspCategory;
import com.rezolve.sdk.ssp.model.SspObject;
import com.rezolve.sdk.ssp.model.SspProduct;
import com.rezolve.sdk.ssp.resolver.ResolverConfiguration;
import com.rezolve.sdk_sample.providers.AuthenticationServiceProvider;
import com.rezolve.sdk_sample.providers.RemoteScanResolverProvider;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.services.AuthenticationService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        SspActManager sspActManager = new SspActManager(sspHttpClient);

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
                ContextCompat.getColor(this, R.color.colorAccent),
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
                .engagementsUpdatePolicy(new EngagementsUpdatePolicy.Builder().build())
                .notificationChannelPropertiesList(geofenceLocationChannels)
                .engagementAlertNotification(geofenceAlertNotificationProperties)
                .context(this)
                .build();

        final LocationProviderFused locationProviderFused = LocationProviderFused.create(this);
        registerGeofenceListener();
        locationProviderFused.start();
        geofenceManager.startGeofenceTracking();

        RemoteScanResolverProvider.getInstance().init(new RemoteScanResolver(sspActManager, sspHttpClient));
    }

    private void registerGeofenceListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GEOFENCE_NOTIFICATION_DISPLAYED);
        intentFilter.addAction(ACTION_GEOFENCE_NOTIFICATION_SELECTED);
        registerReceiver(geofenceBroadcastReceiver, intentFilter);
    }

    BroadcastReceiver geofenceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            final String sender = intent.getStringExtra(KEY_SENDER_PACKAGE_NAME);
            if(!context.getPackageName().equalsIgnoreCase(sender)) {
                Log.d(TAG, "Ignoring intent from: " + sender +", expected: " + context.getPackageName());
                return;
            }

            if(action != null) {
                switch (action) {
                    case ACTION_GEOFENCE_NOTIFICATION_DISPLAYED: {
                        final String name = intent.getStringExtra(KEY_NAME);
                        final String shortDescription = intent.getStringExtra(KEY_DESCRIPTION_SHORT);
                        final String actId = intent.getStringExtra(KEY_ACT_ID);
                        final SspObject sspObject = getSspObjectFromIntent(intent);
                        Log.d(TAG, action + ": " + name + ", " + shortDescription + ", " + actId + ", " + sspObject);
                        break;
                    }
                    case ACTION_GEOFENCE_NOTIFICATION_SELECTED: {
                        final SspObject sspObject = getSspObjectFromIntent(intent);
                        Log.d(TAG, action + ": " + sspObject);
                        break;
                    }
                }
            }

        }

        @Nullable
        private SspObject getSspObjectFromIntent(@NonNull Intent intent) {
            SspObject sspObject = null;

            if(intent.hasExtra(KEY_SSP_ACT)) {
                try {
                    sspObject = SspAct.jsonToEntity(new JSONObject(intent.getStringExtra(KEY_SSP_ACT)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(intent.hasExtra(KEY_SSP_CATEGORY)) {
                try {
                    sspObject = SspCategory.jsonToEntity(new JSONObject(intent.getStringExtra(KEY_SSP_CATEGORY)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if(intent.hasExtra(KEY_SSP_PRODUCT)) {
                try {
                    sspObject = SspProduct.jsonToEntity(new JSONObject(intent.getStringExtra(KEY_SSP_PRODUCT)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return sspObject;
        }
    };
}
