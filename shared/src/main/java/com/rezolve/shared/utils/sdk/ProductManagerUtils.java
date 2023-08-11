package com.rezolve.shared.utils.sdk;

import android.text.TextUtils;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.shared.model.CategoryItem;

import java.util.ArrayList;
import java.util.List;

public class ProductManagerUtils {
    public static List<CategoryItem> getProductsAndSubcategoriesFromCategory(Category category) {
        if (category == null) {
            return null;
        }
        List<CategoryItem> items = new ArrayList<>();
        if (category.hasCategories()) {
            for (Category subcategory : category.getCategoryPageResult().getItems()) {
                items.add(new CategoryItem(
                        CategoryItem.ItemType.CATEGORY,
                        subcategory.getName(),
                        getImage(subcategory),
                        subcategory)
                );
            }
        }

        if (category.hasProducts()) {
            for (DisplayProduct displayProduct : category.getProductPageResult().getItems()) {
                items.add(new CategoryItem(
                        CategoryItem.ItemType.PRODUCT,
                        displayProduct.getName(),
                        getImage(displayProduct),
                        displayProduct)
                );
            }
        }
        return items;
    }

    public static List<Category> getCategoryList(Category category) {
        if (category == null) {
            return null;
        }
        ArrayList<Category> result = new ArrayList<>();
        if (category.hasCategories()) {
            try {
                result.addAll(category.getCategoryPageResult().getItems());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
        return result;
    }

    /**
     *  Depending on configuration, Categories and DisplayProducts usually have assigned one main image.
     *  It's available with getImage() method.
     *
     *  However, full size images are usually too large to display them efficiently on mobile devices.
     *  Scaled down versions of the main image are available with getImageThumbs() method.
     *
     *  There is, however, an exception: if the original image provided by merchant is too small,
     *  the thumbnails might not be generated. Because of that it's recommended to check if the thumbnails
     *  are present and use main image as a fallback.
     */
    public static <T> String getImage(T object) {
        if (object == null) {
            return null;
        }
        String result;
        if (object instanceof DisplayProduct) {
            result = getImage(((DisplayProduct) object).getImageThumbs(), ((DisplayProduct) object).getImage());
        } else if (object instanceof Category) {
            result = getImage(((Category) object).getImageThumbs(), ((Category) object).getImage());
        } else {
            result = null;
        }
        return result;
    }

    private static String getImage(List<String> imageThumbs, String image) {
        if (imageThumbs != null && imageThumbs.size() > 0) {
            for (String imageThumb : imageThumbs) {
                if (!TextUtils.isEmpty(imageThumb)) {
                    return imageThumb;
                }
            }
        }
        return image;
    }
}
