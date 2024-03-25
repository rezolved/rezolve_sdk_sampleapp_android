package com.rezolve.smart_triggers

import com.rezolve.sdk.RezolveSDK
import com.rezolve.sdk.api.TokenHolder
import com.rezolve.sdk.ssp.managers.SspActManager

object SdkProvider {
    lateinit var rezolveSdk: RezolveSDK
    lateinit var sspActManager: SspActManager
    lateinit var pushNotificationProvider: FCMManager
    lateinit var tokenHolder: TokenHolder
}
