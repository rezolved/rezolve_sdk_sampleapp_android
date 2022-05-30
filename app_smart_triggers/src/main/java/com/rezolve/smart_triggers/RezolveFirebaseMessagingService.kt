package com.rezolve.smart_triggers

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class RezolveFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("RezolveFirebaseMessagingService.onMessageReceived: $message")
        SdkProvider.pushNotificationProvider.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        SdkProvider.pushNotificationProvider.updateToken(token)
    }

    companion object {
        const val TAG = "RezolveFMS"
    }
}
