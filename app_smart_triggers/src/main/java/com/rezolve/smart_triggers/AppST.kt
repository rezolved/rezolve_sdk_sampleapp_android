package com.rezolve.smart_triggers

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rezolve.rxp.client.observeAccessTokenAsFlow
import com.rezolve.rxp.client.observeLocationAsFlow
import com.rezolve.rxp.data.database.RXPSdkDatabase
import com.rezolve.rxp.domain.di.RXPSdkDatabaseProvider
import com.rezolve.rxp.push.PushNotificationDIProvider
import com.rezolve.rxp.push.PushNotificationProvider
import com.rezolve.rxp.sdk.RxpSdk
import com.rezolve.rxp.sdk.RxpSdkProvider
import com.rezolve.rxp.sdk.geofence.GeozoneNotificationCallback
import com.rezolve.rxp.sdk.geofence.GeozoneNotificationCallbackHelper
import com.rezolve.sdk.HttpClientConfig
import com.rezolve.sdk.RezolveSDK
import com.rezolve.sdk.RezolveSDK.AuthRequestProvider
import com.rezolve.sdk.RezolveSDK.GetAuthRequest
import com.rezolve.sdk.api.TokenHolder
import com.rezolve.sdk.api.authentication.auth0.AuthParams
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory
import com.rezolve.sdk.location.LocationHelper
import com.rezolve.sdk.logger.RezLog
import com.rezolve.sdk.model.network.RezolveError
import com.rezolve.sdk.ssp.google.GoogleGeofenceDetector
import com.rezolve.sdk.ssp.helper.NotificationChannelProperties
import com.rezolve.sdk.ssp.helper.NotificationHelperImpl
import com.rezolve.sdk.ssp.helper.NotificationProperties
import com.rezolve.sdk.ssp.managers.SspActManager
import com.rezolve.sdk.ssp.model.SspAct
import com.rezolve.sdk.ssp.model.SspObject
import com.rezolve.shared.MainActivityProvider
import com.rezolve.shared.SspActManagerProvider
import com.rezolve.shared.authentication.AuthenticationServiceProvider
import com.rezolve.shared.authentication.SampleAuthRequestProvider
import com.rezolve.shared.sspact.SspActActivity
import com.rezolve.shared.utils.ProductUtils
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import sb.rezolve.app.sandbox.BuildConfig
import sb.rezolve.app.sandbox.R
import java.util.concurrent.TimeUnit

class AppST : Application(), SspActManagerProvider, MainActivityProvider {

    private val geofenceAlertsNotificationChannelId = "RezolveSmartTriggersSampleChannelId"

    private val shouldRemoveNotificationAfterSelection = true

    private lateinit var authRequestProvider: AuthRequestProvider
    private lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        RezLog.LOG_LEVEL = RezLog.VERBOSE
        init()
        initRxpSdk()
    }

    private fun init() {
        preferences = getSharedPreferences("app.st", Context.MODE_PRIVATE)

        val authenticationService = AuthenticationServiceProvider.getAuthenticationService()
        authenticationService.init(BuildConfig.DEMO_AUTH_SERVER, BuildConfig.REZOLVE_SDK_API_KEY)

        authRequestProvider = SampleAuthRequestProvider()

        val rezolveSDK = RezolveSDK.Builder()
            .setApiKey(BuildConfig.REZOLVE_SDK_API_KEY)
            .setEnv(BuildConfig.REZOLVE_SDK_ENVIRONMENT)
            .setAuthRequestProvider(authRequestProvider)
            .build()
        SdkProvider.rezolveSdk = rezolveSDK

        val authParams = AuthParams(
            BuildConfig.AUTH0_CLIENT_ID,
            BuildConfig.AUTH0_CLIENT_SECRET,
            BuildConfig.AUTH0_API_KEY,
            BuildConfig.AUTH0_AUDIENCE,
            BuildConfig.AUTH0_ENDPOINT,
            BuildConfig.SSP_ENGAGEMENT_ENDPOINT,
            BuildConfig.SSP_ACT_ENDPOINT
        )

        val httpConfig = HttpClientConfig.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val httpClientFactory = HttpClientFactory.Builder()
            .setHttpClientConfig(httpConfig)
            .setAuthParams(authParams)
            .build()

        val sspHttpClient = httpClientFactory.createHttpClient(BuildConfig.SSP_ENDPOINT)

        SdkProvider.sspActManager = SspActManager(sspHttpClient, rezolveSDK)
    }

    private fun initRxpSdk() {
        GeozoneNotificationCallbackHelper.getInstance().addCallback(geozoneNotificationCallback)

        val notificationHelper = NotificationHelperImpl(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannelProperties(
                geofenceAlertsNotificationChannelId,
                "myGeofenceAlertsNotificationChannelName",
                "",
                NotificationManager.IMPORTANCE_HIGH,
                true,
                true,
                Settings.System.DEFAULT_NOTIFICATION_URI,
                false
            )

            notificationHelper.createNotificationChannel(notificationChannel)
        }

        val geofenceEngagementAlerts = NotificationProperties(
            geofenceAlertsNotificationChannelId,               // channel id
            R.mipmap.ic_launcher,                              // small icon
            ContextCompat.getColor(this, R.color.colorPrimary), // color
            NotificationCompat.PRIORITY_HIGH,                  // priority
            Notification.DEFAULT_ALL,                          // notification options. The value should be one or more of the following fields combined
            //    with bitwise-or: Notification.DEFAULT_SOUND, Notification.DEFAULT_VIBRATE, Notification.DEFAULT_LIGHTS.
            //    For all default values, use Notification.DEFAULT_ALL.
            longArrayOf(1000, 1000, 1000, 1000, 1000),         // vibration pattern
            Settings.System.DEFAULT_NOTIFICATION_URI,          // sound
            true                                               // auto cancel
        )

        PushNotificationDIProvider.pushNotificationProvider = createFcmManager()

        val geofenceDetector = GoogleGeofenceDetector.Builder()
            .transitionTypes(GoogleGeofenceDetector.TRANSITION_TYPE_ENTER or GoogleGeofenceDetector.TRANSITION_TYPE_EXIT)
            .build(this)
        geofenceDetector.unregisterAll()

        RXPSdkDatabaseProvider.database = RXPSdkDatabase.getDatabase(context = applicationContext)

        val rxpSdk = RxpSdk.Builder(this)
            .authenticator(authenticator)
            .accessTokenFlowable(tokenHolder.observeAccessTokenAsFlow())
            .notificationAlerts(geofenceEngagementAlerts)
            .location(LocationHelper.getInstance(this).observeLocationAsFlow())
            .pushNotificationProvider(SdkProvider.pushNotificationProvider)
            .notificationHelper(notificationHelper)
            .apiKey(BuildConfig.REZOLVE_SDK_API_KEY)
            .endpoint(BuildConfig.SMART_TRIGGERS_BASE_ENDPOINT)
            .geofenceDetector(geofenceDetector)
            .sspActManager(SdkProvider.sspActManager)
            .build()

        RxpSdkProvider.sdk = rxpSdk
    }

    private fun createFcmManager(): PushNotificationProvider = FCMManager(this).also { fcmManager ->
        SdkProvider.pushNotificationProvider = fcmManager
    }

    private val authenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) > 1) {
                return null // If it has already been tried, give up.
            }
            val getAuthRequest: GetAuthRequest = authRequestProvider.authRequest
            if (getAuthRequest.isSuccessful) {
                val headersMap: MutableMap<String, String>? = getAuthRequest.headersMap
                val builder = response.request.newBuilder()
                if (headersMap != null) {
                    for ((key, value) in headersMap) {
                        builder.header(key, value)
                    }
                }
                return builder.build()
            }
            return null
        }

        private fun responseCount(response: Response?): Int {
            return if (response == null) 0 else 1 + responseCount(response.priorResponse)
        }
    }

    private val tokenHolder = object : TokenHolder {

        private val ACCESS_TOKEN = "access_token"

        private val accessTokenChangeListenerList: MutableList<TokenHolder.AccessTokenChangeListener> =
            mutableListOf()

        /**
         *  Initially token is set after successful creation of the session. Check MainActivity#createSession().
         *  Remember to update your access token in tokenHolder when it's refreshed.
         */
        override fun setAccessToken(accessToken: String?) {
            Log.d("TokenHolder", "setAccessToken: $accessToken, observers: ${accessTokenChangeListenerList.joinToString()}")
            preferences.edit().putString(ACCESS_TOKEN, accessToken.orEmpty()).apply()
            accessTokenChangeListenerList.forEach { it.onAccessTokenChanged(accessToken) }
        }

        override fun getAccessToken(): String {
            return preferences.getString(ACCESS_TOKEN, "").orEmpty()
        }

        override fun registerAccessTokenChangeListener(accessTokenChangeListener: TokenHolder.AccessTokenChangeListener) {
            accessTokenChangeListenerList.add(accessTokenChangeListener)
            accessTokenChangeListener.onAccessTokenChanged(accessToken)
        }

        override fun unregisterAccessTokenChangeListener(accessTokenChangeListener: TokenHolder.AccessTokenChangeListener) {
            accessTokenChangeListenerList.remove(accessTokenChangeListener)
        }
    }.also { holder ->
        SdkProvider.tokenHolder = holder
    }

    private val geozoneNotificationCallback = object : GeozoneNotificationCallback {
        override fun onDisplayed(
            title: String,
            subtitle: String,
            thumbnailUrl: String?,
            engagementId: String,
            actId: String
        ) {
            Log.d(MainActivity.TAG, "notification displayed: $title, $engagementId")
        }

        override fun onSelected(sspObject: SspObject): Boolean {
            Log.d(MainActivity.TAG, "notification selected: ${sspObject.title}, ${sspObject.engagementId}")
            if (sspObject is SspAct) {
                navigateToSspActView(sspObject)
            } else {
                // handle other cases
            }
            return shouldRemoveNotificationAfterSelection
        }

        override fun onError(error: RezolveError, engagementId: String) {
            Log.d(MainActivity.TAG, "notification error: ${error.message}, $engagementId")
        }
    }

    private fun navigateToSspActView(act: SspAct) {
        val intent = Intent(this, SspActActivity::class.java)
        val bundle = ProductUtils.toBundle(act)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun getSspActManager(): SspActManager = SdkProvider.sspActManager
    override fun getMainActivity(): Class<*> = MainActivity::class.java

    companion object {
        const val TAG = "AppST"
    }
}
