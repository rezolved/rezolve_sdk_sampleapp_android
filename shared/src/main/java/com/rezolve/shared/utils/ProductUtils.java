package com.rezolve.shared.utils;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.ssp.model.SspAct;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProductUtils {
    private final static String ARG_PRODUCT_AS_JSON = "product_json_string";
    private final static String ARG_SSP_ACT_AS_JSON = "act_json_string";

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

    public static SspAct getSspActFromArgs(@Nullable Bundle bundle) {
        SspAct sspAct = null;
        if(bundle != null) {
            try {
                JSONObject actJson = new JSONObject(bundle.getString(ARG_SSP_ACT_AS_JSON));
                sspAct = SspAct.jsonToEntity(actJson);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return sspAct;
    }

    public static @NonNull Bundle toBundle(@NonNull Product product) {
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_AS_JSON, product.entityToJson().toString());
        return args;
    }

    public static @NonNull Bundle toBundle(@NonNull SspAct sspAct) {
        Bundle args = new Bundle();
        args.putString(ARG_SSP_ACT_AS_JSON, sspAct.entityToJson().toString());
        return args;
    }
}
