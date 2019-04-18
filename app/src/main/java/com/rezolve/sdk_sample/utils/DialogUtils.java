package com.rezolve.sdk_sample.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;

import com.rezolve.sdk_sample.R;

public final class DialogUtils {

    public static void showError(Context context, String message) {
        Resources resources = context.getResources();
        show(context, resources.getString(R.string.dialog_error), message);
    }

    private static void show(Context context, String title, String message) {
        Resources resources = context.getResources();
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        dialog.setTitle(title)
              .setIcon(R.drawable.default_notification_icon)
              .setMessage(message)
              .setPositiveButton(resources.getText(R.string.dialog_ok), (dialogInterface, i) -> {
                  // nothing to implement here
              })
              .show();
    }
}
