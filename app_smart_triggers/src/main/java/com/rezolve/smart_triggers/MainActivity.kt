package com.rezolve.smart_triggers

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.rezolve.rxp.client.APIResult
import com.rezolve.rxp.sdk.RxpSdkProvider
import sb.rezolve.app.sandbox.R

import com.rezolve.rxp.client.data.model.Tag
import com.rezolve.rxp.enums.State
import com.rezolve.sdk.RezolveInterface
import com.rezolve.sdk.RezolveSession
import com.rezolve.sdk.model.network.RezolveError
import com.rezolve.shared.authentication.AuthenticationCallback
import com.rezolve.shared.authentication.AuthenticationResponse
import com.rezolve.shared.authentication.AuthenticationServiceProvider
import com.rezolve.shared.utils.DeviceUtils
import com.rezolve.shared.utils.DialogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sb.rezolve.app.sandbox.BuildConfig

import android.content.pm.PackageManager
import com.rezolve.sdk.location.LocationHelper
import com.rezolve.sdk.location.LocationUpdateListener
import com.rezolve.sdk.location.LocationWrapper
import com.rezolve.sdk.location.google.LocationProviderFused
import android.R.attr.radius

import com.rezolve.rxp.client.data.model.Geofences

import com.rezolve.rxp.enums.CoordinateSystem
import com.rezolve.rxp.enums.MyAreaFilter
import com.rezolve.rxp.sdk.geofence.*
import com.rezolve.sdk.ssp.model.SspObject


class MainActivity : AppCompatActivity() {

    private lateinit var deviceId: String

    private val REQUEST_CODE_LOCATION = 10203
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (LocationHelper.isLocationPermissionGranted(this).not()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_LOCATION)
        } else {
            loginUser()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.size == REQUIRED_PERMISSIONS.size && grantResults.contains(PackageManager.PERMISSION_DENIED).not()) {
                    loginUser()
                }
            }
        }
    }

    private fun startLocationUpdates() {

        val locationProviderFused = LocationProviderFused.create(this)

        LocationHelper.getInstance(this).addLocationListener(object : LocationUpdateListener {
            override fun onLocationChanged(location: LocationWrapper?) {
                Log.d(TAG, "location update: ${location?.entityToJson()}")
                location?.let {
                    CoroutineScope(Dispatchers.Default).launch {
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

                        CoroutineScope(Dispatchers.Default).launch {
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
//                setRxpTags()
            } else if (checkInApiResult is APIResult.Error) {
                //handle error
                DialogUtils.showError(this@MainActivity, checkInApiResult.message)
            }
        }
    }

    // gets list of user tags from server
    private fun getRxpTags() {
        // check user interests
        val tagsResult = RxpSdkProvider.sdk.rxpClient.listTags()
        when (tagsResult) {
            is APIResult.Success -> {
                tagsResult.result.forEach {
                    Log.d(TAG,"GET selected tag: $it")
                }
            }
            is APIResult.Error -> {
                Log.d(TAG,"GET tag error: ${tagsResult.message}")
            }
        }
    }

    // updates list of tags in local DB and on the server
    private fun setRxpTags() {
        RxpSdkProvider.sdk.rxpDataBase.tagDAO().updateAllTagsToState(State.ENABLED)
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