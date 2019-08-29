package com.rezolve.sdk_sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.managers.ProductManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk_sample.adapter.DisplayProductAdapter;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.rezolve.sdk_sample.utils.sdk.MerchantManagerUtils;
import com.rezolve.sdk_sample.utils.sdk.ProductManagerUtils;
import com.rezolve.sdk_sample.utils.sdk.RezolveSdkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class ProductListActivity extends AppCompatActivity {

    public static final String PARAM_MERCHANT_JSON_KEY = "merchantJson";
    public static final String PARAM_CATEGORY_JSON_KEY = "categoryJson";
    private SpinKitView loadingSpinView; // TODO: code duplication
    private RecyclerView recyclerView;
    private TextView tvMessage;
    private ProductManager productManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get external parameters
        Merchant merchant = null;
        Category category = null;
        Intent intent = getIntent();
        if (intent != null) {
            String merchantJson = intent.getStringExtra(PARAM_MERCHANT_JSON_KEY);
            if (merchantJson != null) {
                try {
                    merchant = Merchant.jsonToEntity(new JSONObject(merchantJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            String categoryJson = intent.getStringExtra(PARAM_CATEGORY_JSON_KEY);
            if (categoryJson != null) {
                try {
                    category = Category.jsonToEntity(new JSONObject(categoryJson));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        // Bind views
        loadingSpinView = findViewById(R.id.loadingSpinView); // TODO: code duplication
        ImageView merchantBannerView = findViewById(R.id.ivMerchantBanner);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new DisplayProductAdapter());
        tvMessage = findViewById(R.id.tvMessage);

        // global
        productManager = RezolveSdkUtils.getProductManager(RezolveSDK.peekInstance());

        // load data
        loadBanner(merchantBannerView, merchant);
        loadProducts(merchant, category);
    }

    // TODO: code duplication:
    private void displayLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.INVISIBLE);
    }
    // TODO: code duplication ^^^

    private void loadBanner(ImageView merchantBannerView, Merchant merchant) {
        Glide.with(this)
                .load(MerchantManagerUtils.getBanner(merchant))
                .placeholder(R.drawable.ic_slider_head)
                .error(android.R.drawable.stat_notify_error)
                .dontTransform()
                .into(merchantBannerView);

    }

    private void loadProducts(Merchant merchant, @Nullable Category category, boolean isRecurrentCall) {
        if (category == null) {
            // We don't have category so we need to request of it
            if (merchant != null && !isRecurrentCall) {
                requestCategory(merchant, null);
            } else {
                // We are not possible to achieve category
                displayError("Not possible to achieve category");
            }
            return;
        }

        List<DisplayProduct> displayProductList = ProductManagerUtils.getProductFromCategory(category);
        if (displayProductList != null) {
            displayProductList(displayProductList);
        }

        // Choice category
        List<Category> categoryList = ProductManagerUtils.getCategoryList(category);
        if (categoryList != null && categoryList.size() > 0) {
            DialogUtils.showChoicer(
                    ProductListActivity.this,
                    getString(R.string.category_choicer_title),
                    categoryList,
                    (spinnerView, item) -> requestCategory(merchant, item)
            );
        }
    }

    private void loadProducts(Merchant merchant, Category category) {
        loadProducts(merchant, category, false);
    }

    private void requestCategory(@NonNull Merchant merchant, @Nullable Category category) {
        ProductManagerUtils.getCategory(
                productManager,
                merchant.getId(),
                category,
                new BaseGetCategoryCallback() {
                    @Override
                    public void onSuccess(Category category) {
                        if (category == null) {
                            displayError("Missing category for merchant " + merchant.toString());
                        } else {
                            // Recurrence
                            loadProducts(merchant, category, true);
                        }
                    }
                }
        );
    }

    private void displayProductList(List<DisplayProduct> displayProductList) {
        if (displayProductList == null || displayProductList.isEmpty()) {
            displayMessage(getString(R.string.empty_list));
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            ((DisplayProductAdapter) Objects.requireNonNull(recyclerView.getAdapter())).updateData(displayProductList);
        }
    }

    private void displayMessage(String message) {
        recyclerView.setVisibility(View.GONE);
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
        tvMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void displayError(String message) {
        displayMessage(message);
        tvMessage.setTextColor(Color.RED);
    }

    private void displayRezolveError(RezolveError rezolveError) {
        displayError(RezolveSdkUtils.formatedRezolveError(getApplicationContext(), rezolveError));
    }

    //
    // Redundant callbacks
    //

    private class BaseProcessingInterface implements RezolveSdkUtils.ProcessingInterface {

        @Override
        public void processingStarted() {
            displayLoadingIndicator();
        }

        @Override
        public void processingFinished() {
            hideLoadingIndicator();
        }

        @Override
        public void onRezolveError(@NonNull RezolveError rezolveError) {
            displayRezolveError(rezolveError);
        }
    }

    private abstract class BaseGetCategoryCallback extends BaseProcessingInterface
            implements ProductManagerUtils.GetCategoryCallback {
    }

}
