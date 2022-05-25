package com.rezolve.smart_triggers

import android.app.Application
import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.rezolve.rxp.client.APIResult
import com.rezolve.rxp.client.observeAccessTokenAsFlow
import com.rezolve.rxp.push.PushNotificationDIProvider
import com.rezolve.rxp.push.PushNotificationProvider
import com.rezolve.rxp.sdk.RxpSdk
import com.rezolve.rxp.sdk.RxpSdkProvider
import com.rezolve.sdk.HttpClientConfig
import com.rezolve.sdk.RezolveSDK
import com.rezolve.sdk.RezolveSDK.AuthRequestProvider
import com.rezolve.sdk.RezolveSDK.GetAuthRequest
import com.rezolve.sdk.api.TokenHolder
import com.rezolve.sdk.api.authentication.auth0.AuthParams
import com.rezolve.sdk.api.authentication.auth0.HttpClientFactory
import com.rezolve.sdk.ssp.google.GoogleGeofenceDetector
import com.rezolve.sdk.ssp.helper.NotificationHelperImpl
import com.rezolve.sdk.ssp.helper.NotificationProperties
import com.rezolve.sdk.ssp.managers.SspActManager
import com.rezolve.shared.MainActivityProvider
import com.rezolve.shared.SspActManagerProvider
import com.rezolve.shared.authentication.AuthenticationServiceProvider
import com.rezolve.shared.authentication.SampleAuthRequestProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import sb.rezolve.app.sandbox.BuildConfig
import sb.rezolve.app.sandbox.R
import java.util.concurrent.TimeUnit

class AppST : Application(), SspActManagerProvider, MainActivityProvider {

    private val geofenceAlertsNotificationChannelId = "RezolveSmartTriggersSampleChannelId"

    private lateinit var authRequestProvider: AuthRequestProvider
    private lateinit var preferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
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

        val rxpSdk = RxpSdk.Builder(this)
            .authenticator(authenticator)
            .accessTokenFlowable(tokenHolder.observeAccessTokenAsFlow())
            .notificationAlerts(geofenceEngagementAlerts)
            .pushNotificationProvider(SdkProvider.pushNotificationProvider)
            .notificationHelper(NotificationHelperImpl(this))
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

        override fun setAccessToken(accessToken: String?) {
            Log.d(
                "TokenHolder",
                "setAccessToken: $accessToken, observers: ${accessTokenChangeListenerList.joinToString()}"
            )
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

    override fun getSspActManager(): SspActManager = SdkProvider.sspActManager
    override fun getMainActivity(): Class<*> = MainActivity::class.java

    companion object {
        const val TAG = "AppST"
    }
}