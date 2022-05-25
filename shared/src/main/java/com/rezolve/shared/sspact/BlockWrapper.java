package com.rezolve.shared.sspact;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.ssp.model.PageBuildingBlock;

public class BlockWrapper {
    @NonNull public PageBuildingBlock block;
    @Nullable public String answerToDisplay;

    public BlockWrapper(PageBuildingBlock block) {
        this.block = block;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof BlockWrapper)) {
            return false;
        }
        BlockWrapper p1 = (BlockWrapper)obj;
        return block.equals(p1.block) && answerToDisplay.equals(p1.answerToDisplay);
    }
}
