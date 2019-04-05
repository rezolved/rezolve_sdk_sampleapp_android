package com.rezolve.sdk_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class ProductDetailsActivity extends AppCompatActivity {

    private final String PRICE_PREFIX = "$";

    private ImageView previewImageView;
    private TextView titleTextView;
    private TextView priceTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        previewImageView = findViewById(R.id.previewImageView);
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTitleView);

        displayProductDetails();
    }

    private void displayProductDetails() {
        // TODO Retrieving whole Product object
        Bundle bundle = this.getIntent().getExtras();
        String title = bundle.getString("title");
        Float price = bundle.getFloat("price");
        String imagePreviewUrl = bundle.getString("previewImage");

        Glide.with(this).load(imagePreviewUrl).into(previewImageView);
        titleTextView.setText(title);
        priceTextView.setText(PRICE_PREFIX + price);
    }
}
