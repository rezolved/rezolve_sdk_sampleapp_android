package com.rezolve.sdk_sample.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.model.SspAct;
import com.rezolve.sdk.ssp.model.SspCategory;
import com.rezolve.sdk.ssp.model.SspProduct;
import com.rezolve.sdk_sample.navigation.MainNavigator;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationUtil {

    public static final String PARCELABLE_EXTRA_ITEM_ID = "Parcelable_extra_id";
    public static final String PARCELABLE_EXTRA_MERCHANT_NAME = "Parcelable_extra_merchant_name";
    public static final String SERIALIZED_PRODUCT_RESULT = "serialized_product_result";
    public static final String SERIALIZED_CATEGORY_RESULT = "serialized_category_result";

    public static final String ACTION_PRODUCT_AUDIO_SCAN = ";PRODUCT_AUDIO_SCAN";
    public static final String ACTION_CATEGORY_AUDIO_SCAN = ";ACTION_CATEGORY_AUDIO_SCAN";
    public static final String ACTION_SSP_ACT_SCAN = ";action_ssp_act_scan";
    public static final String SERIALIZED_SSP_ACT = ";serialized_ssp_act";
    public static final String SERIALIZED_SSP_PRODUCT = ";serialized_ssp_product";
    public static final String SERIALIZED_SSP_CATEGORY = ";serialized_ssp_category";

    public static boolean isLaunchedFromNotification(Activity activity) {
        return activity.getIntent() != null
                && activity.getIntent().getAction() != null
                && isValidAction(activity.getIntent().getAction());
    }

    public static void launch(@NonNull Intent receivedIntent, @NonNull MainNavigator navigator) {
        String action = receivedIntent.getAction();

        if (action == null) {
            return;
        }

        switch (action) {
            case ACTION_PRODUCT_AUDIO_SCAN -> {
                final String productResult = receivedIntent.getStringExtra(SERIALIZED_PRODUCT_RESULT);
                try {
                    JSONObject jsonObject = new JSONObject(productResult);
                    Product product = Product.jsonToEntity(jsonObject.getJSONObject("product"));
                    navigator.navigateToProductDetails(product);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
            case ACTION_CATEGORY_AUDIO_SCAN -> {
                final String productResult = receivedIntent.getStringExtra(SERIALIZED_CATEGORY_RESULT);
                try {
                    JSONObject jsonObject = new JSONObject(productResult);
                    Category category = Category.jsonToEntity(jsonObject.getJSONObject("category"));
                    String merchantId = jsonObject.getString("merchantId");
                    //navigator.navigateToProductDetails();
                    //TODO
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
            case ACTION_SSP_ACT_SCAN -> {

                if (receivedIntent.hasExtra(SERIALIZED_SSP_ACT)) {
                    final String sspActSerialized = receivedIntent.getStringExtra(SERIALIZED_SSP_ACT);
                    try {
                        final SspAct sspAct = SspAct.jsonToEntity(new JSONObject(sspActSerialized));
                        //TODO
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                } else if (receivedIntent.hasExtra(SERIALIZED_SSP_PRODUCT)) {
                    final String sspProductSerialized = receivedIntent.getStringExtra(SERIALIZED_SSP_PRODUCT);
                    try {
                        final SspProduct sspProduct = SspProduct.jsonToEntity(new JSONObject(sspProductSerialized));
                        //TODO
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (receivedIntent.hasExtra(SERIALIZED_SSP_CATEGORY)) {
                    final String sspCategorySerialized = receivedIntent.getStringExtra(SERIALIZED_SSP_CATEGORY);
                    try {
                        final SspCategory sspCategory = SspCategory.jsonToEntity(new JSONObject(sspCategorySerialized));
                        //TODO
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                break;
            }
        }

    }

    private static boolean isValidAction(String action) {
        return action.equals(ACTION_PRODUCT_AUDIO_SCAN)
                || action.equals(ACTION_CATEGORY_AUDIO_SCAN)
                || action.equals(ACTION_SSP_ACT_SCAN);
    }

}
