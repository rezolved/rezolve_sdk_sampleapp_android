package com.rezolve.sdk_sample.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.rezolve.sdk.model.shop.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.reactivex.annotations.Nullable;

public class ProductUtils {
    private final static String ARG_PRODUCT_AS_JSON = "product_json_string";

    public static Product getProductFromList(List list, String productId){
        for (Object object: list){
            if (object instanceof Product){
                if ( ((Product)object).getId().equals(productId)){
                    return (Product) object;
                }
            }
        }
        return null;
    }


    public static Product getProductFromArgs(@Nullable Bundle bundle) {
        Product product = null;
        if(bundle != null) {
            try {
                JSONObject productJson = new JSONObject(bundle.getString(ARG_PRODUCT_AS_JSON));
                product = Product.jsonToEntity(productJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return product;
    }

    public static @NonNull Bundle toBundle(@NonNull Product product) {
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_AS_JSON, product.entityToJson().toString());
        return args;
    }
}
