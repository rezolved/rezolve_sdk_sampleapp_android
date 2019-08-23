package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.rezolve.sdk.RezolveSDK;

public class ProductListActivity extends AppCompatActivity {

    public static final String PARAM_MERCHANT_ID_KEY = "merchantId";
    public static final String PARAM_MERCHANT_BANNER_KEY = "merchantBanner";
    private SpinKitView loadingSpinView; // TODO: code duplication
    private RezolveSDK rezolveSdk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get external parameters
        Intent intent = getIntent();
        String merchantId = null;
        String merchantBanner = null;
        if (intent != null) {
            merchantId = intent.getStringExtra(PARAM_MERCHANT_ID_KEY);
            merchantBanner = intent.getStringExtra(PARAM_MERCHANT_BANNER_KEY);
        }

        // Bind views
        loadingSpinView = findViewById(R.id.loadingSpinView); // TODO: code duplication
        ImageView merchantBannerView = findViewById(R.id.ivMerchantBanner);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        // global
        rezolveSdk = RezolveSDK.peekInstance();

        // load data
        loadBanner(merchantBannerView, merchantBanner);
        loadProducts(recyclerView, merchantId);
    }

    // TODO: code duplication:
    private void displayLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.INVISIBLE);
    }
    // TODO: code duplication ^^^

    private void loadBanner(ImageView merchantBannerView, String merchantBanner) {
        Glide.with(this)
                .load(merchantBanner)
                .placeholder(R.drawable.ic_slider_head)
                .error(android.R.drawable.stat_notify_error)
                .dontTransform()
                .into(merchantBannerView);

    }

    private void loadProducts(RecyclerView recyclerView, String merchantId) {
        // FIXME: implement this method!
        displayLoadingIndicator();
        new Handler().postDelayed(() -> {
            Toast.makeText(this, merchantId, Toast.LENGTH_LONG).show();
            hideLoadingIndicator();
        }, 3000);
    }

}
