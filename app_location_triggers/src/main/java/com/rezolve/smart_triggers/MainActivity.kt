package com.rezolve.smart_triggers

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import com.rezolve.rxp.client.APIResult
import com.rezolve.rxp.client.data.model.Geofences
import com.rezolve.rxp.client.data.model.Tag
import com.rezolve.rxp.enums.CoordinateSystem
import com.rezolve.rxp.enums.MyAreaFilter
import com.rezolve.rxp.enums.State
import com.rezolve.rxp.sdk.RxpSdkProvider
import com.rezolve.sdk.RezolveInterface
import com.rezolve.sdk.RezolveSession
import com.rezolve.sdk.location.LocationHelper
import com.rezolve.sdk.location.LocationUpdateListener
import com.rezolve.sdk.location.LocationWrapper
import com.rezolve.sdk.location.google.LocationProviderFused
import com.rezolve.sdk.model.network.RezolveError
import com.rezolve.shared.authentication.AuthenticationCallback
import com.rezolve.shared.authentication.AuthenticationResponse
import com.rezolve.shared.authentication.AuthenticationServiceProvider
import com.rezolve.shared.utils.DeviceUtils
import com.rezolve.shared.utils.DialogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sb.rezolve.app.sandbox.BuildConfig
import sb.rezolve.app.sandbox.R

class MainActivity : AppCompatActivity() {

    private lateinit var deviceId: String

    private val REQUEST_CODE_LOCATION = 10203
    private val REQUEST_CODE_BACKGROUND_LOCATION = 10204
    private val BASE_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS // required to show notifications on Android 33+
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (LocationHelper.isLocationPermissionGranted(this).not()
            || PermissionChecker.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED
            || PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, BASE_PERMISSIONS, REQUEST_CODE_LOCATION)
        } else {
            loginUser()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        when(requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.size == BASE_PERMISSIONS.size && grantResults.contains(PackageManager.PERMISSION_DENIED).not()) {
                    /** required to register geofences on android 29+
                     * AND has to be requested AFTER basic location permissions were granted
                     * https://developer.android.com/develop/sensors-and-location/location/permissions#request-background-location
                     */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && PermissionChecker.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND_LOCATION)
                    } else {
                        loginUser()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.missing_location_permission), Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_CODE_BACKGROUND_LOCATION -> {
                if (grantResults.contains(PackageManager.PERMISSION_DENIED).not()) {
                    loginUser()
                } else {
                    Toast.makeText(this, getString(R.string.missing_location_permission), Toast.LENGTH_LONG).show()
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startLocationUpdates() {

        val locationProviderFused = LocationProviderFused.create(this)

        LocationHelper.getInstance(this).addLocationListener(object : LocationUpdateListener {
            override fun onLocationChanged(location: LocationWrapper?) {
                Log.d(TAG, "location update: ${location?.entityToJson()}")
                location?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val updateTrackingApiResult: APIResult<Geofences> = RxpSdkProvider.sdk.rxpClient.updateTracking(
                            location.rezolveLocation.latitude.toFloat(),
                            location.rezolveLocation.longitude.toFloat(),
                            2000,
                            CoordinateSystem.WGS84
                        )
                    }
                }
            }
        })

        locationProviderFused.start()
    }

    private fun loginUser() {
        deviceId = DeviceUtils.getDeviceId(this)
        AuthenticationServiceProvider.getAuthenticationService().login(
            BuildConfig.DEMO_AUTH_USER,
            BuildConfig.DEMO_AUTH_PASSWORD,
            deviceId,
            object : AuthenticationCallback {
                override fun onLoginSuccess(response: AuthenticationResponse) {
                    createSession(response)
                }

                override fun onLoginFailure(message: String) {
                    DialogUtils.showError(this@MainActivity, message)
                }
            })
    }

    private fun createSession(response: AuthenticationResponse) {
        SdkProvider.rezolveSdk.apply {
            setAuthToken(response.token)
            setDeviceIdHeader(deviceId)

            SdkProvider.tokenHolder.accessToken = response.token

            createSession(
                response.token,
                response.entityId,
                response.partnerId,
                null,
                object : RezolveInterface {
                    override fun onInitializationSuccess(
                        rezolveSession: RezolveSession,
                        partnerId: String,
                        entityId: String
                    ) {
                        Log.d(TAG, "RezolveSDK initalization success")
                        startLocationUpdates()

                        CoroutineScope(Dispatchers.IO).launch {
                            rxpCheckIn()
                        }
                    }

                    override fun onInitializationFailure(rezolveError: RezolveError) {
                        DialogUtils.showError(this@MainActivity, rezolveError.message)
                    }
                })
        }
    }

    private suspend fun rxpCheckIn() {
        SdkProvider.pushNotificationProvider.pushToken.collect {
            val checkInApiResult: APIResult<String> = RxpSdkProvider.sdk.rxpClient.checkIn(it)
            if (checkInApiResult is APIResult.Success) {
                //handle successful check in
                Log.d(AppST.TAG, "RxpSdk check-in success: ${checkInApiResult.result}")
                RxpSdkProvider.sdk.runTagsListWorkerOnce()

                val tagsApiResult = RxpSdkProvider.sdk.rxpClient.listTags()
                if (tagsApiResult is APIResult.Success) {
                    val tags = tagsApiResult.result
                    val newTags = mutableListOf<Tag>()
                    tags.forEach { tag ->
                        newTags.add(tag.copy(state = State.ENABLED)) // update tags to user preferences
                    }
                    RxpSdkProvider.sdk.rxpClient.updateTags(newTags)
                }
            } else if (checkInApiResult is APIResult.Error) {
                //handle error
                withContext(Dispatchers.Main) {
                    DialogUtils.showError(this@MainActivity, checkInApiResult.message)
                }
            }
        }
    }

    // returns paginated list of engagements in selected proximity
    private fun getNearbyEngagements() {
        LocationHelper.getInstance(this).lastKnownLocation?.let {
            val result = RxpSdkProvider.sdk.rxpClient.getMyArea(
                latitude = it.rezolveLocation.latitude.toFloat(),
                longitude = it.rezolveLocation.longitude.toFloat(),
                distance = 2000,
                coordinateSystem = CoordinateSystem.WGS84,
                filter = MyAreaFilter.ALL, // use MyAreaFilter.MY to get nearby engagements filtered by user's interests
                limit = 100,
                offset = 0
            )
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
