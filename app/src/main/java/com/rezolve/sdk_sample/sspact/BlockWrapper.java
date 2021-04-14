package com.rezolve.sdk_sample.sspact;

import androidx.annotation.Nullable;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;

public class BlockWrapper {
    public PageBuildingBlock block;
    public String answerToDisplay;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof BlockWrapper)) {
            return false;
        }
        BlockWrapper p1 = (BlockWrapper)obj;
        return block.equals(p1.block) && answerToDisplay.equals(p1.answerToDisplay);
    }
}
