package com.rezolve.sdk_sample.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

public final class DeviceUtils {

    private static final String KEY_DEVICE_ID = "device_id";

    public static String userIdentifier;

    public static String getDeviceId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.contains(KEY_DEVICE_ID)) {
            return sharedPreferences.getString(KEY_DEVICE_ID, null);
        } else {
            String deviceId = UUID.randomUUID().toString();
            editor.putString(KEY_DEVICE_ID, deviceId);
            editor.apply();
            return deviceId;
        }
    }

    public static String generateUserIdentifier() {
        long currentTime = DateUtils.getCurrentTimestampInSeconds();

        // Currently I'll use timestamp as ID and email prefix for the user registration
        userIdentifier = String.valueOf(currentTime);
        return userIdentifier;
    }
}
