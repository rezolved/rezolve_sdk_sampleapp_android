package com.rezolve.shared.utils.sdk;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.core.interfaces.MerchantInterface;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.SupportedDeliveryMethod;

import java.util.List;

public class MerchantManagerUtils {

    //
    // Getters
    //

    public static void getMerchants(@Nullable MerchantManager merchantManager,
                                    @NonNull MerchantManager.MerchantVisibility merchantVisibility,
                                    @NonNull GetMerchantsCallback callback) {
        if (merchantManager == null) {
            callback.onRezolveError(RezolveSdkUtils.createMissingMerchantManagerError());
        } else {
            callback.processingStarted();
            merchantManager.getMerchants(merchantVisibility, callback);
        }
    }

    public static String getBanner(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        List<String> bannerThumbs = merchant.getBannerThumbs();
        if (bannerThumbs != null && bannerThumbs.size() > 0) {
            for (String bannerThumb : bannerThumbs) {
                if (!TextUtils.isEmpty(bannerThumb)) {
                    return bannerThumb;
                }
            }
        }
        return merchant.getBanner();
    }

    //
    // Interfaces
    //

    public interface GetMerchantsCallback extends MerchantInterface, RezolveSdkUtils.ProcessingInterface {

        void onSuccess(List<Merchant> merchantList);

        @Override
        default void onGetMerchantsSuccess(List<Merchant> list) {
            processingFinished();
            onSuccess(list);
        }

        @Override
        default void onGetShippingMethodsSuccess(List<SupportedDeliveryMethod> list) {
            processingFinished();
        }

        @Override
        default void onError(@NonNull RezolveError rezolveError) {
            processingFinished();
            onRezolveError(rezolveError);
        }
    }
}
