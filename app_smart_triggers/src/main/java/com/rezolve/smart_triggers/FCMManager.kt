package com.rezolve.smart_triggers

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.rezolve.rxp.client.data.model.PushMessage
import com.rezolve.rxp.client.data.model.PushToken
import com.rezolve.rxp.push.PushNotificationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class FCMManager constructor(context: Context) : PushNotificationProvider {

    private val _token = MutableSharedFlow<PushToken>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _messages = MutableSharedFlow<PushMessage>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val pushToken: Flow<PushToken>
        get() = _token.distinctUntilChanged()

    override val messages: Flow<PushMessage>
        get() = _messages.distinctUntilChanged()

    init {
        FirebaseApp.initializeApp(context)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Fetching FCM registration token failed", task.exception)
                _token.tryEmit(PushToken.None)
                return@OnCompleteListener
            }
            val token = task.result
            if (token != null) {
                _token.tryEmit(PushToken.FCM(token))
            } else {
                Log.e(TAG, "FCM token is null")
                _token.tryEmit(PushToken.None)
                return@OnCompleteListener
            }
        })
    }

    fun updateToken(newToken: String) {
        _token.tryEmit(PushToken.FCM(newToken))
    }

    fun onMessageReceived(message: RemoteMessage) {
        println("$TAG.onMessageReceived: $message")
        _messages.tryEmit(message.toPushMessage())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun resetCache(){
        _messages.resetReplayCache()
    }

    companion object {
        const val TAG = "FCM_Manager"
    }
}

private fun RemoteMessage.toPushMessage() = PushMessage(this.data)
