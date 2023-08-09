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
}
