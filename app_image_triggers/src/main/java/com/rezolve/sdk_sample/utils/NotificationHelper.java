package com.rezolve.sdk_sample.utils;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.StringRes;

public class NotificationHelper {
    public static final String CHANNEL_ID = "Rezolve Sample";
    public static final String PARCELABLE_EXTRA_ITEM_ID = "parcelable_extra_item_id";

    public static void createNotificationChannel(Context context, @StringRes int channelName, @StringRes int channelDescription) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(channelName);
            String descriptionText = context.getString(channelDescription);
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(descriptionText);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
