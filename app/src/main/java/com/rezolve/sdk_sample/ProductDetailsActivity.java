package com.rezolve.sdk_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk_sample.model.ProductDetails;
import com.synnapps.carouselview.CarouselView;

import org.parceler.Parcels;

public class ProductDetailsActivity extends AppCompatActivity {

    private final String PRICE_PREFIX = "$";

    private CarouselView previewCarouselView;
    private TextView titleTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private Button quantityIncreaseButton;
    private Button quantityDecreaseButton;

    private ProductDetails productDetails;
    private int productQuantity = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        previewCarouselView = findViewById(R.id.previewCarouselView);
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        quantityIncreaseButton = findViewById(R.id.quantityIncreaseButton);
        quantityDecreaseButton = findViewById(R.id.quantityDecreaseButton);

        quantityIncreaseButton.setOnClickListener(view -> increaseQuantity());
        quantityDecreaseButton.setOnClickListener(view -> decreaseQuantity());

        productDetails = Parcels.unwrap(getIntent().getParcelableExtra("product_details"));
    }

    @Override
    protected void onResume() {
        super.onResume();

        displayProductDetails();
    }

    private void displayProductDetails() {
        previewCarouselView.setPageCount(productDetails.getImages().size());

        previewCarouselView.setImageListener((position, imageView) -> {
            String imageUrl = productDetails.getImages().get(position);
            Glide.with(this).load(imageUrl).into(imageView);
        });

        titleTextView.setText(productDetails.getTitle());
        priceTextView.setText(PRICE_PREFIX + productDetails.getPrice());

        displayQuantity();
    }

    private void increaseQuantity() {
        productQuantity++;
        displayQuantity();
    }

    private void decreaseQuantity() {
        if(productQuantity == 1) {
            return;
        }

        productQuantity--;
        displayQuantity();
    }

    private void displayQuantity() {
        String quantityPrefix = getResources().getString(R.string.quantity);
        quantityTextView.setText(quantityPrefix + " " + productQuantity);
    }
}
