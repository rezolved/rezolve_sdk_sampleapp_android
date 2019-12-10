package com.rezolve.sdk_sample;

import android.app.Activity;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.rezolve.sdk_sample.services.BackgroundListeningService;

public class AppLifecycleListener implements LifecycleObserver {
    private Activity activity;

    /*
    * This LifecycleListener responds to app lifecycle changes to start/stop BGL
    * But it's not required for BGL to work, BackgroundListeningService can be called from onResume/onPause method inside activity
    */

    public AppLifecycleListener(Activity activity) {
        this.activity = activity;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onMoveToForeground() {
        BackgroundListeningService.getInstance().stop(activity, false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onMoveToBackground() {
        BackgroundListeningService.getInstance().start(activity);
    }
}
