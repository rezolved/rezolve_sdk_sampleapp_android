package com.rezolve.shared.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

public abstract class RecyclerViewAdapter<T, VH extends BindableRecyclerViewHolder<T>> extends RecyclerView.Adapter<VH> implements View.OnClickListener {
    private ArrayList<T> items = new ArrayList<>();
    private OnItemClickListener<T> onItemClickListener;

    // Abstract

    @LayoutRes
    abstract protected int getItemLayoutResId();

    abstract protected VH createViewHolder(View view);

    // RecyclerView.Adapter

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutResId(), parent, false);
        view.setOnClickListener(this);
        return createViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull VH viewHolder, int position) {
        T item = items.get(position);
        viewHolder.bind(item);
        viewHolder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // View.OnClickListener

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            //noinspection unchecked
            onItemClickListener.onItemClick(view, (T) view.getTag());
        }
    }

    // PUBLIC

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void updateData(Collection<T> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T item);
    }
}
