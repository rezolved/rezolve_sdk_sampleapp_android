package com.rezolve.sdk_sample.model;

import com.rezolve.sdk.model.shop.Product;
import org.parceler.Parcel;
import java.util.List;

@Parcel
public class ProductDetails {
    String id;
    String merchantId;
    String title;
    Float price;
    List<String> images;

    public ProductDetails() {}

    public ProductDetails(Product product) {
        id = product.getId();
        merchantId = product.getMerchantId();
        title = product.getTitle();
        price = product.getPrice();
        images = product.getImages();
    }

    public String getId() {
        return id;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getTitle() {
        return title;
    }

    public Float getPrice() {
        return price;
    }

    public List<String> getImages() {
        return images;
    }
}
