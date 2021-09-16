package com.rezolve.sdk_sample.sspact;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.ssp.model.form.SelectionOption;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.sspact.blocks.SspTextBlock;

import net.cachapa.expandablelayout.ExpandableLayout;

public class SspActBlockAdapter extends ListAdapter<BlockWrapper, SspActBlockAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEADER = R.layout.item_ssp_block_header;
    private final int VIEW_TYPE_PARAGRAPH = R.layout.item_ssp_block_paragraph;
    private final int VIEW_TYPE_DIVIDER = R.layout.item_ssp_block_divider;
    private final int VIEW_TYPE_IMAGE = R.layout.item_ssp_block_image;
    private final int VIEW_TYPE_VIDEO = R.layout.item_ssp_block_video;
    private final int VIEW_TYPE_DATE_FIELD = R.layout.item_ssp_block_date_field;
    private final int VIEW_TYPE_SELECT = R.layout.item_ssp_block_select;
    private final int VIEW_TYPE_TEXT_INPUT = R.layout.item_ssp_block_text_input;

    private LayoutInflater layoutInflater;

    private SspActBlockEventListener eventListener;

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
            case VIEW_TYPE_IMAGE: return new ImageViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_image, parent, false));
            case VIEW_TYPE_VIDEO: return new VideoViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_video, parent, false));
            case VIEW_TYPE_DATE_FIELD: return new DateFieldViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_date_field, parent, false));
            case VIEW_TYPE_SELECT: return new SelectViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_select, parent, false));
            case VIEW_TYPE_TEXT_INPUT: return new TextInputViewHolder(layoutInflater.inflate(R.layout.item_ssp_block_text_input, parent, false));
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
            case DATE_FIELD: return VIEW_TYPE_DATE_FIELD;
            case SELECT: return VIEW_TYPE_SELECT;
            case TEXT_FIELD: return VIEW_TYPE_TEXT_INPUT;
            default: throw new IllegalArgumentException("Invalid type: "+getItem(position).block.getType());
        }
    }

    abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(BlockWrapper block, int position);
    }

    class HeaderViewHolder extends ViewHolder {
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

    class ParagraphViewHolder extends ViewHolder {
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

    class DividerViewHolder extends ViewHolder {
        public DividerViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(BlockWrapper block, int position) {
        }
    }

    class ImageViewHolder extends ViewHolder {
        private final AppCompatImageView sspActImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            sspActImageView = itemView.findViewById(R.id.ssp_act_image);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            Glide.with(sspActImageView.getContext()).load(block.block.getData().getUrl()).into(sspActImageView); //NPE getContext
        }
    }

    class VideoViewHolder extends ViewHolder {
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

    class DateFieldViewHolder extends ViewHolder {
        private final ConstraintLayout container;
        private final AppCompatTextView label;
        private final AppCompatTextView answer;

        private BlockWrapper blockWrapper;
        private int position;

        public DateFieldViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.ssp_act_date_field_container);
            label = itemView.findViewById(R.id.ssp_block_date_button);
            answer = itemView.findViewById(R.id.ssp_block_date_answer_display);
            container.setOnClickListener(view -> eventListener.onDateBlockSelected(blockWrapper, position));
        }

        @Override
        void bind(BlockWrapper block, int position) {
            this.blockWrapper = block;
            this.position = position;
            setTextForLabel(block, label);
            answer.setText(TextUtils.isEmpty(blockWrapper.answerToDisplay) ? answer.getContext().getString(R.string.ssp_block_select) : blockWrapper.answerToDisplay);
        }
    }

    class TextInputViewHolder extends ViewHolder {
        private final AppCompatTextView label;
        private final AppCompatEditText inputField;

        private BlockWrapper blockWrapper;

        public TextInputViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.ssp_block_text_input_label);
            inputField = itemView.findViewById(R.id.ssp_block_text_input_edittext);
            inputField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    blockWrapper.answerToDisplay = charSequence.toString();
                    setColorForLabel(blockWrapper, label);
                    eventListener.onTextInputBlockChange(blockWrapper, charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) { }
            });
        }

        @Override
        void bind(BlockWrapper block, int position) {
            this.blockWrapper = block;
            setTextForLabel(block, label);
        }
    }

    class SelectViewHolder extends ViewHolder {
        private final AppCompatTextView label;
        private final AppCompatTextView button;
        private final ExpandableLayout expandableLayout;
        private final LinearLayout options;

        private BlockWrapper blockWrapper;

        public SelectViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.ssp_block_select_label);
            button = itemView.findViewById(R.id.ssp_block_select_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandableLayout.toggle();
                }
            });
            expandableLayout = itemView.findViewById(R.id.ssp_block_select_layout);
            options = itemView.findViewById(R.id.ssp_block_option_list);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            this.blockWrapper = block;
            setTextForLabel(block, label);
            button.setText(TextUtils.isEmpty(block.answerToDisplay) ? button.getContext().getString(R.string.ssp_block_select) : block.answerToDisplay);

            options.removeAllViews();
            for (SelectionOption selectionOption : block.block.getData().getSelectionOptions()) {
                TextView view = new TextView(options.getContext());
                view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                view.setPadding(20, 12, 0, 12);
                view.setText(selectionOption.getDescription());
                view.setTextColor(ContextCompat.getColor(options.getContext(), R.color.almost_black));
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        expandableLayout.collapse();
                        eventListener.onSelectBlockOptionSelected(block, selectionOption);
                    }
                });
                options.addView(view);
            }
        }
    }

    private void setTextForLabel(BlockWrapper block, TextView label) {
        label.setText(block.block.getData().getText().concat((block.block.isRequired() ? label.getContext().getString(R.string.ssp_field_required_suffix) : "")));
        setColorForLabel(block, label);
    }

    private void setColorForLabel(BlockWrapper block, TextView label) {
        label.setTextColor(ContextCompat.getColor(label.getContext(), block.block.isRequired() && TextUtils.isEmpty(block.answerToDisplay) ? R.color.ssp_block_field_required : R.color.black));
    }

    public void setEventListener(SspActBlockEventListener listener) {
        this.eventListener = listener;
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