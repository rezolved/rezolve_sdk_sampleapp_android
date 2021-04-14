package com.rezolve.sdk_sample.sspact;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.sspact.blocks.SspTextBlock;

public class SspActBlockAdapter extends ListAdapter<BlockWrapper, SspActBlockAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEADER = R.layout.item_ssp_block_header;
    private final int VIEW_TYPE_PARAGRAPH = R.layout.item_ssp_block_paragraph;
    private final int VIEW_TYPE_DIVIDER = R.layout.item_ssp_block_divider;
    private final int VIEW_TYPE_IMAGE = R.layout.item_ssp_block_image;
    private final int VIEW_TYPE_VIDEO = R.layout.item_ssp_block_video;
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
            case VIEW_TYPE_HEADER: return new HeaderViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_header, parent, false));
            case VIEW_TYPE_PARAGRAPH: return new ParagraphViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
            case VIEW_TYPE_DIVIDER: return new DividerViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_divider, parent, false));
            case VIEW_TYPE_IMAGE: return new ImageViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
            case VIEW_TYPE_VIDEO: return new VideoViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
//            case VIEW_TYPE_DATE_FIELD: return new DateFieldViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
//            case VIEW_TYPE_SELECT: return new SelectViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
//            case VIEW_TYPE_TEXT_INPUT: return new TextInputViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_paragraph, parent, false));
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
            case PARAGRAPH: return VIEW_TYPE_PARAGRAPH;
            case DIVIDER: return VIEW_TYPE_DIVIDER;
            case IMAGE: return VIEW_TYPE_IMAGE;
            case VIDEO: return VIEW_TYPE_VIDEO;
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
        private final SspTextBlock sspActSspTextBlock;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActSspTextBlock = itemView.findViewById(R.id.ssp_act_header);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            sspActSspTextBlock.setBlock(block.block);
        }
    }

    static class ParagraphViewHolder extends ViewHolder {
        private final SspTextBlock sspActSspTextBlock;

        public ParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActSspTextBlock = itemView.findViewById(R.id.ssp_act_paragraph);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            sspActSspTextBlock.setBlock(block.block);
        }
    }

    static class DividerViewHolder extends ViewHolder {
        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(BlockWrapper block, int position) {
        }
    }

    static class ImageViewHolder extends ViewHolder {
        private final AppCompatImageView sspActImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActImageView = itemView.findViewById(R.id.ssp_act_image);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            Glide.with(sspActImageView.getContext()).load(block.block.getData().getUrl()).into(sspActImageView);
        }
    }

    static class VideoViewHolder extends ViewHolder {
        private final WebView sspActVideoView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActVideoView = itemView.findViewById(R.id.ssp_act_video);
        }

        @SuppressLint("SetJavaScriptEnabled")
        @Override
        void bind(BlockWrapper block, int position) {
            sspActVideoView.getSettings().setJavaScriptEnabled(true);
            sspActVideoView.getSettings().setLoadWithOverviewMode(true);
            sspActVideoView.getSettings().setUseWideViewPort(true);
            sspActVideoView.loadUrl(block.block.getData().getUrl());
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