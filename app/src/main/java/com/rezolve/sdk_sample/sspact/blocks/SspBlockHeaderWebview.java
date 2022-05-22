package com.rezolve.sdk_sample.sspact.blocks;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;
import com.rezolve.sdk.ssp.model.form.Type;

public class SspBlockHeaderWebview extends WebView {

    private static final String HTML_HEAD = "<html><head>";
    private static final String HTML_BODY = "</head><body>";

    //  If needed to add custom fonts, put the .ttf file in /main/assets/fonts/ and add it like on the example below
    // "@font-face { font-family: 'arial black'; src: url('file:///android_asset/fonts/arialblack.ttf'); }"
    private static final String CUSTOM_FONTS = "<style type='text/css'></style>";

    private static final String HEADER_SPAN_STYLE = "<span style=\"font-size: 18pt;\">";
    private static final String HEADER_SPAN_END = "</span>";
    private static final String HTML_END = "</body></html>";

    public SspBlockHeaderWebview(Context context) {
        super(context);
    }

    public SspBlockHeaderWebview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SspBlockHeaderWebview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBlock(PageBuildingBlock block) {
        if (block.getStyle() != null) {
            setBackgroundColor(Color.parseColor(block.getStyle().getBackgroundColor()));
        }

        String htmlCode = getHtmlContent(block.getContent(), block.getType());
        loadDataWithBaseURL(null, htmlCode, "text/html; charset=utf-8;", "utf-8", null);
    }

    private String getHtmlContent(String content, Type type) {
        StringBuilder htmlCode = new StringBuilder("");
        htmlCode.append(HTML_HEAD);
        htmlCode.append(CUSTOM_FONTS);
        htmlCode.append(HTML_BODY);

        if (type == Type.HEADER) {
            htmlCode.append(HEADER_SPAN_STYLE);
        }

        htmlCode.append(content);

        if (type == Type.HEADER) {
            htmlCode.append(HEADER_SPAN_END);
        }

        htmlCode.append(HTML_END);

        return htmlCode.toString();
    }
}
