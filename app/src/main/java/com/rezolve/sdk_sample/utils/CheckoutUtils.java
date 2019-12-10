package com.rezolve.sdk_sample.utils;

import androidx.annotation.Nullable;

import com.rezolve.sdk.model.cart.CheckoutProduct;
import com.rezolve.sdk.model.cart.CustomConfigurableOption;
import com.rezolve.sdk.model.shop.CustomOption;
import com.rezolve.sdk.model.shop.CustomOptionValue;
import com.rezolve.sdk.model.shop.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckoutUtils {

    public static List<CustomConfigurableOption> getDefaultCustomConfigurableOptionList(@Nullable Product product) {
        ArrayList<CustomConfigurableOption> result = new ArrayList<>();
        if (product != null) for (CustomOption customOption : product.getCustomOptions()) {
            CustomConfigurableOption customConfigurableOption = new CustomConfigurableOption();
            customConfigurableOption.setOptionId(customOption.getOptionId());

            // In case when values list for this custom option is null
            CustomOptionValue customOptionValue = null;
            if(customOption.getValues() != null) {
                customOptionValue = customOption.getValues().size() > 0 ? customOption.getValues().get(0) : null;
            }

            // TODO: improve default value according to type
            customConfigurableOption.setValue(new String[]{customOptionValue == null ? "NULL value" : customOptionValue.getValueId()});
            result.add(customConfigurableOption);
        }
        return result;
    }

    public static void addCustomConfigurableOption(CheckoutProduct checkoutProduct, CustomConfigurableOption customConfigurableOption) {
        int oldSize = checkoutProduct.getCustomOptions().size();
        checkoutProduct.addCustomConfigurableOption(checkoutProduct, customConfigurableOption);
        // In case of issue in `addCustomConfigurableOption` method:
        if (oldSize == checkoutProduct.getCustomOptions().size()) {
            checkoutProduct.getCustomOptions().add(customConfigurableOption);
        }
    }

}
