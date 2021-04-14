package com.rezolve.sdk_sample.sspact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.sspact.blocks.Header;

public class SspActBlockAdapter extends ListAdapter<BlockWrapper, SspActBlockAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEADER = R.layout.ssp_block_header;
//    private final int VIEW_TYPE_PARAGRAPH = R.layout.item_ssp_block_paragraph;
//    private final int VIEW_TYPE_DIVIDER = R.layout.item_ssp_block_divider;
//    private final int VIEW_TYPE_IMAGE = R.layout.item_ssp_block_image;
//    private final int VIEW_TYPE_VIDEO = R.layout.item_ssp_block_video;
//    private final int VIEW_TYPE_DATE_FIELD = R.layout.item_ssp_block_date_field;
//    private final int VIEW_TYPE_SELECT = R.layout.item_ssp_block_select;
//    private final int VIEW_TYPE_TEXT_INPUT = R.layout.item_ssp_block_text_input;

    private LayoutInflater layoutInflater;

    protected SspActBlockAdapter() {
        super(new BlockWrapperItemDiff());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }

        switch(viewType) {
            case VIEW_TYPE_HEADER: return new HeaderViewHolder(layoutInflater.inflate(R.layout.ssp_block_header, parent, false));
            default: throw new IllegalArgumentException("Invalid viewtype: "+viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SspActBlockAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        switch(getItem(position).block.getType()) {
            case HEADER: return VIEW_TYPE_HEADER;
//            case PARAGRAPH: return VIEW_TYPE_PARAGRAPH;
//            case DIVIDER: return VIEW_TYPE_DIVIDER;
//            case IMAGE: return VIEW_TYPE_IMAGE;
//            case VIDEO: return VIEW_TYPE_VIDEO;
//            case DATE_FIELD: return VIEW_TYPE_DATE_FIELD;
//            case SELECT: return VIEW_TYPE_SELECT;
//            case TEXT_FIELD: return VIEW_TYPE_TEXT_INPUT;
            default: throw new IllegalArgumentException("Invalid type: "+getItem(position).block.getType());
        }
    }

    abstract static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(BlockWrapper block, int position);
    }

    static class HeaderViewHolder extends ViewHolder {
        private final Header sspActHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActHeader = itemView.findViewById(R.id.ssp_act_header);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            sspActHeader.setBlock(block.block);
        }
    }
}

class BlockWrapperItemDiff extends DiffUtil.ItemCallback<BlockWrapper> {

    @Override
    public boolean areItemsTheSame(BlockWrapper p0, BlockWrapper p1){
        return p0.block.getId().equals(p1.block.getId());
    }

    @Override
    public boolean areContentsTheSame(BlockWrapper p0, BlockWrapper p1){
        return p0.equals(p1);
    }
}