package com.rezolve.shared.sspact.blocks;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.appcompat.widget.AppCompatTextView;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.form.FontStyle;
import com.rezolve.sdk.ssp.model.form.FontWeight;
import com.rezolve.sdk.ssp.model.form.Style;
import com.rezolve.sdk.ssp.model.form.TextAlign;

public class SspTextBlock extends AppCompatTextView {
    public SspTextBlock(Context context) {
        super(context);
    }

    public SspTextBlock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SspTextBlock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBlock(PageBuildingBlock block) {
        this.setText(block.getData().getText());
        this.setTextColor(Color.parseColor(block.getStyle().getColor()));
        this.setBackgroundColor(Color.parseColor(block.getStyle().getBackgroundColor()));
        this.setTypeface(null, getTypeface(block.getStyle()));
        this.setGravity(getGravity(block.getStyle().getTextAlign()));
    }

    private int getGravity(TextAlign textAlign) {
        switch (textAlign) {
            case RIGHT:
                return Gravity.RIGHT;
            case CENTER:
                return Gravity.CENTER;
            case LEFT:
            default:
                return Gravity.LEFT;
        }
    }

    private int getTypeface(Style style) {
        if (style.getFontWeight() == FontWeight.BOLD && style.getFontStyle() == FontStyle.ITALIC) return Typeface.BOLD_ITALIC;
        if (style.getFontWeight() == FontWeight.BOLD) return Typeface.BOLD;
        if (style.getFontStyle() == FontStyle.ITALIC) return Typeface.ITALIC;
        return Typeface.NORMAL;
    }
}
