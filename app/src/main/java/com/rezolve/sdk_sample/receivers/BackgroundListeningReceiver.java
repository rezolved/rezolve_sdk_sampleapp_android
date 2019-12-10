package com.rezolve.sdk_sample.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.rezolve.sdk_sample.MainActivity;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.utils.NotificationHelper;

import static com.rezolve.sdk.core.AutoDetectService.ACTION_PRODUCT_AUDIO_SCAN;
import static com.rezolve.sdk.core.AutoDetectService.INTENT_EXTRA_PRODUCT_ID;
import static com.rezolve.sdk.core.AutoDetectService.INTENT_EXTRA_PRODUCT_TITLE;

public class BackgroundListeningReceiver extends BroadcastReceiver {

    /*
     * onReceive method is being called when BGL service has found audio engagement in the background
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == (ACTION_PRODUCT_AUDIO_SCAN)) {
            String title = intent.getStringExtra(INTENT_EXTRA_PRODUCT_TITLE);
            String productId = intent.getStringExtra(INTENT_EXTRA_PRODUCT_ID);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // We want to display notification to the user with product title
            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            notificationIntent.setAction(intent.getAction());

            // ProductId as extra is required to restore Product object on app launch when user will click on that notification
            notificationIntent.putExtra(NotificationHelper.PARCELABLE_EXTRA_ITEM_ID, productId);


            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(notificationIntent)
                    .getPendingIntent(productId.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                    .setSmallIcon(R.drawable.default_notification_icon)
                    .setContentTitle(title)
                    .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            notificationManager.notify(productId.hashCode(), notification);
        }
    }
}

