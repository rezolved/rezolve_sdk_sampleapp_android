package com.rezolve.sdk_sample.services;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.interfaces.AutoDetectInterface;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.receivers.BackgroundListeningReceiver;
import com.rezolve.sdk_sample.utils.NotificationHelper;

import java.util.List;

import static com.rezolve.sdk.core.AutoDetectService.ACTION_PRODUCT_AUDIO_SCAN;

public class BackgroundListeningService implements AutoDetectInterface, AudioManager.OnAudioFocusChangeListener {
    private final String TAG = BackgroundListeningService.class.getSimpleName();

    private boolean isBackgroundListeningEnabled = false;

    private static BackgroundListeningService instance;

    public static BackgroundListeningService getInstance() {
        if (instance == null) {
            instance = new BackgroundListeningService();
        }
        return instance;
    }

    public void start(Activity activity) {
        if (!isBackgroundListeningEnabled) {
            if(RezolveSDK.peekInstance() == null) {
                return;
            }

            RezolveSession rezolveSession = RezolveSDK.peekInstance().getRezolveSession();
            Context context = activity.getApplicationContext();

            // For Android 8.0+ support we have to create notification channel
            NotificationHelper.createNotificationChannel(context, R.string.notification_channel_name,
                    R.string.notification_channel_description);

            // We want to request audio input for BGL from the system
            requestAudioFocus(context);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION_PRODUCT_AUDIO_SCAN);
            context.registerReceiver(new BackgroundListeningReceiver(), intentFilter);

            if (rezolveSession != null) {
                // BGL starts
                rezolveSession.getAutoDetectManager().startAutoDetectService(activity, null, this);
            }
        }
    }

    public void stop(Activity activity, boolean isLaunchedFromNotification) {
        if (isBackgroundListeningEnabled || isLaunchedFromNotification) {
            RezolveSession rezolveSession = RezolveSDK.peekInstance().getRezolveSession();
            Context context = activity.getApplicationContext();

            if (rezolveSession != null) {
                // We want to release audio input for other apps that might be working
                abandonAudioFocus(context);
                // BGL stops
                rezolveSession.getAutoDetectManager().stopAutoDetectService(context);
            }
        }
    }

    private void requestAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                                .setOnAudioFocusChangeListener(this)
                                                .build();

            audioManager.requestAudioFocus(request);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(audioManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest request = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .build();

            audioManager.abandonAudioFocusRequest(request);
        } else {
            audioManager.abandonAudioFocus(this);
        }
    }

    @Override
    public void onAutoDetectResults(List list) {
        Log.i(TAG, "A list with " + list.size() + " object(s) was received from Rezolve Auto Detect");
    }

    @Override
    public void onAudioFocusChange(int i) {
    }
}
