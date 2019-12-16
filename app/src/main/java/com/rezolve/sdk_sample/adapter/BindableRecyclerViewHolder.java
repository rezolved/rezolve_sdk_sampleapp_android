package com.rezolve.sdk_sample.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

abstract class BindableRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    BindableRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(T item);
}
