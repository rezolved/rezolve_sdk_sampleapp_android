package com.rezolve.shared.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;

public class CategoryItem {
    private final ItemType type;
    private final String name;
    private final String imageUrl;

    private @Nullable DisplayProduct displayProduct;
    private @Nullable Category category;

    public CategoryItem(ItemType type, String name, String imageUrl, @NonNull DisplayProduct displayProduct) {
        this.type = type;
        this.name = name;
        this.imageUrl = imageUrl;
        this.displayProduct = displayProduct;
    }

    public CategoryItem(ItemType type, String name, String imageUrl, @NonNull Category category) {
        this.type = type;
        this.name = name;
        this.imageUrl = imageUrl;
        this.category = category;
    }

    public ItemType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public DisplayProduct getDisplayProduct() {
        return displayProduct;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    public enum ItemType {
        PRODUCT,
        CATEGORY
    }
}