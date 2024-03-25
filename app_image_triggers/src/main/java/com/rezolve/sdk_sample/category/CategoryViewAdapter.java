package com.rezolve.sdk_sample.category;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk_sample.R;
import com.rezolve.shared.model.CategoryItem;

import java.util.ArrayList;
import java.util.List;

public class CategoryViewAdapter extends RecyclerView.Adapter<CategoryViewAdapter.CategoryViewHolder> {

    private List<CategoryItem> items = new ArrayList<>();

    private final ClickListener clickListener;

    public CategoryViewAdapter(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_item_product, viewGroup, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position >= 0) {
            holder.bind(items.get(position));
            holder.itemView.setOnClickListener(v -> clickListener.onItemClick(items.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<CategoryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     *  For simplicity we use same ViewHolder for both subcategories and products.
     *  In production app it's recommended to diversify the views.
     */
    protected static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thumbnail = itemView.findViewById(R.id.ivThumb);
            this.name = itemView.findViewById(R.id.tvName);
        }

        public void bind(CategoryItem item) {
            Glide.with(this.thumbnail.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_slider_head)
                    .error(android.R.drawable.stat_notify_error)
                    .dontTransform()
                    .into(this.thumbnail);
            this.name.setText(item.getName());
        }
    }

    public interface ClickListener {
        void onItemClick(CategoryItem item);
    }
}
