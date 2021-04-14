package com.rezolve.sdk_sample.sspact;

import com.rezolve.sdk.ssp.model.form.SelectionOption;

public interface SspActBlockEventListener {
    void onDateBlockSelected(BlockWrapper blockWrapper, int position);
    void onSelectBlockOptionSelected(BlockWrapper blockWrapper, SelectionOption selectionOption);
    void onTextInputBlockChange(BlockWrapper blockWrapper, String text);
}
