package com.rezolve.sdk_sample.sspact;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.ssp.model.Link;
import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.form.DateConditions;
import com.rezolve.sdk.ssp.model.form.FontStyle;
import com.rezolve.sdk.ssp.model.form.FontWeight;
import com.rezolve.sdk.ssp.model.form.SelectionOption;
import com.rezolve.sdk.ssp.model.form.Style;
import com.rezolve.sdk.ssp.model.form.TextAlign;
import com.rezolve.sdk.ssp.model.form.Type;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.sspact.blocks.SspBlockWebview;
import com.rezolve.sdk_sample.sspact.blocks.SspTextBlock;
import com.rezolve.sdk_sample.utils.DateUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

public class SspActBlockAdapter extends ListAdapter<BlockWrapper, SspActBlockAdapter.ViewHolder> {

    private final int VIEW_TYPE_HEADER = R.layout.item_ssp_block_header;
    private final int VIEW_TYPE_WEBVIEW = R.layout.item_ssp_block_webview;
    private final int VIEW_TYPE_PARAGRAPH = R.layout.item_ssp_block_paragraph;
    private final int VIEW_TYPE_DIVIDER = R.layout.item_ssp_block_divider;
    private final int VIEW_TYPE_IMAGE = R.layout.item_ssp_block_image;
    private final int VIEW_TYPE_VIDEO = R.layout.item_ssp_block_video;
    private final int VIEW_TYPE_DATE_FIELD = R.layout.item_ssp_block_date_field;
    private final int VIEW_TYPE_SELECT = R.layout.item_ssp_block_select;
    private final int VIEW_TYPE_TEXT_INPUT = R.layout.item_ssp_block_text_input;
    private final int VIEW_TYPE_COUPON = R.layout.item_ssp_block_coupon;
    private final int VIEW_TYPE_EMPTY = R.layout.item_ssp_block_empty;
    private final int VIEW_TYPE_LINK = R.layout.item_ssp_block_link;

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
            case VIEW_TYPE_HEADER: return new HeaderViewHolder(layoutInflater.inflate(VIEW_TYPE_HEADER, parent, false));
            case VIEW_TYPE_WEBVIEW: return new WebViewHolder(layoutInflater.inflate(VIEW_TYPE_WEBVIEW, parent, false));
            case VIEW_TYPE_PARAGRAPH: return new ParagraphViewHolder(layoutInflater.inflate(VIEW_TYPE_PARAGRAPH, parent, false));
            case VIEW_TYPE_DIVIDER: return new DividerViewHolder(layoutInflater.inflate(VIEW_TYPE_DIVIDER, parent, false));
            case VIEW_TYPE_IMAGE: return new ImageViewHolder(layoutInflater.inflate(VIEW_TYPE_IMAGE, parent, false));
            case VIEW_TYPE_VIDEO: return new VideoViewHolder(layoutInflater.inflate(VIEW_TYPE_VIDEO, parent, false));
            case VIEW_TYPE_DATE_FIELD: return new DateFieldViewHolder(layoutInflater.inflate(VIEW_TYPE_DATE_FIELD, parent, false));
            case VIEW_TYPE_SELECT: return new SelectViewHolder(layoutInflater.inflate(VIEW_TYPE_SELECT, parent, false));
            case VIEW_TYPE_TEXT_INPUT: return new TextInputViewHolder(layoutInflater.inflate(VIEW_TYPE_TEXT_INPUT, parent, false));
            case VIEW_TYPE_COUPON: return new SspBlockCouponViewHolder(layoutInflater.inflate(VIEW_TYPE_COUPON, parent, false));
            case VIEW_TYPE_LINK: return new LinkViewHolder(layoutInflater.inflate(VIEW_TYPE_LINK, parent, false));
            default: return new EmptyViewHolder(layoutInflater.inflate(VIEW_TYPE_EMPTY, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SspActBlockAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position), position);
    }

    @Override
    public int getItemViewType(int position) {
        PageBuildingBlock block = getItem(position).block;
        switch(block.getType()) {
            case HEADER: return hasPageBuildingBlockContent(block) ? VIEW_TYPE_WEBVIEW : VIEW_TYPE_HEADER;
            case PARAGRAPH: return hasPageBuildingBlockContent(block) ? VIEW_TYPE_WEBVIEW : VIEW_TYPE_PARAGRAPH;
            case DIVIDER: return VIEW_TYPE_DIVIDER;
            case IMAGE: return VIEW_TYPE_IMAGE;
            case VIDEO: return VIEW_TYPE_VIDEO;
            case DATE_FIELD: return VIEW_TYPE_DATE_FIELD;
            case SELECT: return VIEW_TYPE_SELECT;
            case TEXT_FIELD: return VIEW_TYPE_TEXT_INPUT;
            case COUPON: return VIEW_TYPE_COUPON;
            case LINK: return VIEW_TYPE_LINK;
            default: return VIEW_TYPE_EMPTY;
        }
    }

    private boolean hasPageBuildingBlockContent(PageBuildingBlock block) {
        if (block.getContent() != null && !block.getContent().isEmpty()) {
            return true;
        }

        return false;
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

    class WebViewHolder extends ViewHolder {
        private final SspBlockWebview sspBlockHeaderWebview;

        public WebViewHolder(@NonNull View itemView) {
            super(itemView);
            sspBlockHeaderWebview = itemView.findViewById(R.id.ssp_block_header_webview);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            sspBlockHeaderWebview.setBlock(block.block);
        }
    }

    class SspBlockCouponViewHolder extends ViewHolder {

        private final AppCompatImageView couponQrCodeImageView;
        private final AppCompatTextView couponTitleTextView;

        public SspBlockCouponViewHolder(@NonNull View itemView) {
            super(itemView);
            couponQrCodeImageView = itemView.findViewById(R.id.ssp_block_coupon_qr_code);
            couponTitleTextView = itemView.findViewById(R.id.ssp_block_coupon_title);
        }

        @Override
        void bind(BlockWrapper block, int position) {
            setQrCodeImageView(block.block.getData().getUrl());
            setBlockTitle(block.block);
        }

        private void setQrCodeImageView(String url) {
            if (url != null) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .error(R.mipmap.ic_launcher)
                        .into(couponQrCodeImageView);
            }
        }

        private void setBlockTitle(PageBuildingBlock value) {
            couponTitleTextView.setText(getTextForBlock(value));

            if (value.getStyle() != null) {
                couponTitleTextView.setTextColor(Color.parseColor(value.getStyle().getColor()));
                couponTitleTextView.setBackgroundColor(Color.parseColor(value.getStyle().getBackgroundColor()));
                couponTitleTextView.setTypeface(null, getTypeface(value.getStyle()));
                couponTitleTextView.setGravity(getGravity(value.getStyle().getTextAlign()));
            }
        }

        private int getTypeface(Style style) {
            if (style.getFontWeight() == FontWeight.BOLD && style.getFontStyle() == FontStyle.ITALIC)
                return Typeface.BOLD_ITALIC;
            if (style.getFontWeight() == FontWeight.BOLD)
                return Typeface.BOLD;
            if (style.getFontStyle() == FontStyle.ITALIC)
                return Typeface.ITALIC;

            return Typeface.NORMAL;
        }

        private int getGravity(TextAlign textAlign) {
            switch (textAlign) {
                case CENTER:
                    return Gravity.CENTER;
                case RIGHT:
                    return Gravity.END;
                default:
                    return Gravity.START;
            }
        }

        private String getTextForBlock(PageBuildingBlock value) {
            if (value.getType() == Type.COUPON) {
                DateConditions dateConditions = value.getData().getDateConditions();
                if (dateConditions != null && dateConditions.getEndDateLimit() != null) {
                    return itemView.getContext().getResources().getString(R.string.ssp_block_coupon_expiration,
                            DateUtils.getHumanReadableDateSspDefaultFormatWithTime(dateConditions.getEndDateLimit()));
                }
            } else {
                String text = value.getData().getText();
                if (text != null) {
                    return text;
                }
            }

            return "";
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
            Glide.with(sspActImageView.getContext())
                    .load(block.block.getData().getUrl())
                    .into(sspActImageView);
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

    class LinkViewHolder extends ViewHolder {

        private final AppCompatImageView linkImageView;
        private final AppCompatTextView linkTitleTextView;
        private final AppCompatTextView linkDescriptionTextView;

        public LinkViewHolder(@NonNull View itemView) {
            super(itemView);
            linkImageView = itemView.findViewById(R.id.image);
            linkTitleTextView = itemView.findViewById(R.id.title);
            linkDescriptionTextView = itemView.findViewById(R.id.description);
        }


        @Override
        void bind(BlockWrapper item, int position) {
            Link link = item.block.getLink();
            if (link != null) {
                setLinkImageView(link.getThumbnail());
                setLinkTitle(link.getTitle(), link.getSubtitle());
            }
        }

        private void setLinkImageView(String url) {
            if (url != null) {
                Glide.with(itemView.getContext())
                        .load(url)
                        .error(R.mipmap.ic_launcher)
                        .into(linkImageView);
            }
        }

        private void setLinkTitle(String title, String subtitle) {
            linkTitleTextView.setText(title);
            linkDescriptionTextView.setText(subtitle);
        }
    }

    class EmptyViewHolder extends ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(BlockWrapper item, int position) {
            //do nothing, it's a fallback for new, unknown types
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
