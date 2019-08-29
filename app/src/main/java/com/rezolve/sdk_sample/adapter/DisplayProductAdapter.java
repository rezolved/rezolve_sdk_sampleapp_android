package com.rezolve.sdk_sample.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.utils.sdk.ProductManagerUtils;

public class DisplayProductAdapter extends RecyclerViewAdapter<DisplayProduct, DisplayProductAdapter.ViewHolder> {

    // RecyclerViewAdapter

    @LayoutRes
    @Override
    protected int getItemLayoutResId() {
        return R.layout.recycler_item_product;
    }

    @Override
    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    // BindableRecyclerViewHolder

    protected static class ViewHolder extends BindableRecyclerViewHolder<DisplayProduct> {
        private final ImageView thumbnails;
        private final TextView name;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.thumbnails = itemView.findViewById(R.id.ivThumb);
            this.name = itemView.findViewById(R.id.tvName);
        }

        @Override
        public void bind(DisplayProduct item) {
            Glide.with(this.thumbnails.getContext())
                    .load(ProductManagerUtils.getImage(item))
                    .placeholder(R.drawable.ic_slider_head)
                    .error(android.R.drawable.stat_notify_error)
                    .dontTransform()
                    .into(this.thumbnails);
            this.name.setText(item.getName());
        }
    }
}
