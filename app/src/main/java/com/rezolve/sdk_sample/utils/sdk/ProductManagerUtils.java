package com.rezolve.sdk_sample.utils.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.rezolve.sdk.core.interfaces.ProductInterface;
import com.rezolve.sdk.core.managers.ProductManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.sdk.model.shop.PageNavigationFilter;
import com.rezolve.sdk.model.shop.PageResult;
import com.rezolve.sdk.model.shop.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductManagerUtils {

    //
    // Getters
    //

    public static void getCategory(@Nullable ProductManager productManager,
                                   @NonNull String merchantId,
                                   @Nullable Category category,
                                   @NonNull PageNavigationFilter categoryFilter,
                                   @NonNull PageNavigationFilter productFilter,
                                   @NonNull GetCategoryCallback callback) {
        if (productManager == null) {
            callback.onRezolveError(RezolveSdkUtils.createMissingProductManagerError());
        } else {
            callback.processingStarted();
            productManager.getProductsAndCategories(merchantId, category, categoryFilter, productFilter, callback);
        }
    }

    public static void getCategory(@Nullable ProductManager productManager,
                                   @NonNull String merchantId,
                                   @Nullable Category category,
                                   @NonNull GetCategoryCallback callback) {
        getCategory(productManager, merchantId, category, PageNavigationFilter.getDefault(-1), PageNavigationFilter.getDefault(-1), callback);
    }

    public static void getCategory(@Nullable ProductManager productManager,
                                   @NonNull String merchantId,
                                   @NonNull GetCategoryCallback callback) {
        getCategory(productManager, merchantId, null, callback);
    }

    public static List<DisplayProduct> getProductFromCategory(Category category) {
        if (category == null) {
            return null;
        }
        ArrayList<DisplayProduct> result = new ArrayList<>();
        if (category.hasProducts()) {
            try {
                result.addAll(category.getProductPageResult().getItems());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
        }
        return result;
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
            try {
                result.addAll(category.getCategories());
            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }
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

    //
    // Interfaces
    //

    public interface GetCategoryCallback extends ProductInterface, RezolveSdkUtils.ProcessingInterface {

        void onSuccess(Category category);

        @Override
        default void onGetCategoriesSuccess(Category category) {
            processingFinished();
        }

        @Override
        default void onGetCategorySuccess(Category category) {
            processingFinished();
        }

        @Override
        default void onGetProductsSuccess(PageResult<DisplayProduct> pageResult) {
            processingFinished();
        }

        @Override
        default void onGetProductSuccess(Product product) {
            processingFinished();
        }

        @Override
        default void onGetProductsAndCategoriesSuccess(Category category) {
            processingFinished();
            onSuccess(category);
        }

        @Override
        default void onError(@NonNull RezolveError rezolveError) {
            processingFinished();
            onRezolveError(rezolveError);
        }
    }
}
