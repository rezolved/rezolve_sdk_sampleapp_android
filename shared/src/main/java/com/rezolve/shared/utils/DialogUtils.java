package com.rezolve.shared.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.rezolve.shared.R;
import com.rezolve.shared.adapter.RecyclerViewAdapter;

import java.util.List;

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

    public static <T> void showChoicer(Activity activity, String title, @NonNull List<T> choices, RecyclerViewAdapter.OnItemClickListener<T> onItemClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        final Spinner spinner = new Spinner(activity);
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, choices);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        builder.setView(spinner);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            try {
                //noinspection unchecked
                onItemClickListener.onItemClick(spinner, (T) spinner.getSelectedItem());
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }
}
