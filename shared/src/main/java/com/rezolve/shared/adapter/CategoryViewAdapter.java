package com.rezolve.shared.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.shared.R;
import com.rezolve.shared.utils.sdk.ProductManagerUtils;

public class CategoryViewAdapter extends RecyclerViewAdapter<CategoryViewAdapter.Item, CategoryViewAdapter.ViewHolder> {

    @LayoutRes
    @Override
    protected int getItemLayoutResId() {
        return R.layout.recycler_item_product;
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    protected static class ViewHolder extends BindableRecyclerViewHolder<CategoryViewAdapter.Item> {
        private final ImageView thumbnails;
        private final TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thumbnails = itemView.findViewById(R.id.ivThumb);
            this.name = itemView.findViewById(R.id.tvName);
        }

        @Override
        public void bind(CategoryViewAdapter.Item item) {
            Glide.with(this.thumbnails.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_slider_head)
                    .error(android.R.drawable.stat_notify_error)
                    .dontTransform()
                    .into(this.thumbnails);
            this.name.setText(item.getName());
        }
    }

    public static class Item {
        private ItemType type;
        private String name;
        private String imageUrl;

        private @Nullable DisplayProduct displayProduct;
        private @Nullable Category category;

        public Item(ItemType type, String name, String imageUrl, @NonNull DisplayProduct displayProduct) {
            this.type = type;
            this.name = name;
            this.imageUrl = imageUrl;
            this.displayProduct = displayProduct;
        }

        public Item(ItemType type, String name, String imageUrl, @NonNull Category category) {
            this.type = type;
            this.name = name;
            this.imageUrl = imageUrl;
            this.category = category;
        }

        public ItemType getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        @Nullable
        public DisplayProduct getDisplayProduct() {
            return displayProduct;
        }

        @Nullable
        public Category getCategory() {
            return category;
        }
    }

    public enum ItemType {
        PRODUCT,
        CATEGORY
    }
}
