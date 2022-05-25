package com.rezolve.shared.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

abstract class BindableRecyclerViewHolder<T> extends RecyclerView.ViewHolder {

    BindableRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(T item);
}
