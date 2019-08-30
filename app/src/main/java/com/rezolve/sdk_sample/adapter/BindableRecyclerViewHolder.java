package com.rezolve.sdk_sample.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

abstract class BindableRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    BindableRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(T item);
}
