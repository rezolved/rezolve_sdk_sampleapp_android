package com.rezolve.sdk_sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.rezolve.sdk.core.managers.ProductManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.shared.adapter.DisplayProductAdapter;
import com.rezolve.shared.utils.DialogUtils;
import com.rezolve.shared.utils.sdk.MerchantManagerUtils;
import com.rezolve.shared.utils.sdk.ProductManagerUtils;
import com.rezolve.shared.utils.sdk.RezolveSdkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class ProductListActivity extends AppCompatActivity {

    public static final String PARAM_MERCHANT_JSON_KEY = "merchantJson";
    public static final String PARAM_CATEGORY_JSON_KEY = "categoryJson";
    private SpinKitView loadingSpinView; // TODO: code duplication
    private FloatingActionButton fabMain;
    private ImageView ivBanner;
    private RecyclerView recyclerView;
    private TextView tvMessage;
    private ProductManager productManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get external parameters
        Intent intent = getIntent();
        Merchant merchant = getMerchant(intent);
        Category category = getCategory(intent);

        // Bind views
        loadingSpinView = findViewById(R.id.loadingSpinView); // TODO: code duplication
        fabMain = findViewById(R.id.fabMain);
        ivBanner = findViewById(R.id.ivBanner);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(new DisplayProductAdapter());
        tvMessage = findViewById(R.id.tvMessage);

        // global
        productManager = RezolveSdkUtils.getProductManager();

        // load data
        loadBanner(merchant, category);

        if (category == null && merchant != null) {
            initialRequestCategory(merchant); // initialRequestCategory -> initFab -> loadProducts
        } else {
            initFab(merchant, category);
            loadProducts(merchant, category);
        }
    }

    private JSONObject createJsonObject(Intent intent, String intentParamKey) throws NullPointerException, JSONException {
        return new JSONObject(intent == null ? null : intent.getStringExtra(intentParamKey));
    }

    private Merchant getMerchant(Intent intent) {
        try {
            return Merchant.jsonToEntity(createJsonObject(intent, PARAM_MERCHANT_JSON_KEY));
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Category getCategory(Intent intent) {
        try {
            return Category.jsonToEntity(createJsonObject(intent, PARAM_CATEGORY_JSON_KEY));
        } catch (NullPointerException npe) {
            // DO NOTHING
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initFab(Merchant merchant, Category category) {
        if (fabMain != null) {
            fabMain.setOnClickListener(view -> showCategoryChoicer(merchant, category));
        }
    }

    // TODO: code duplication:
    private void displayLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.GONE);
    }
    // TODO: code duplication ^^^

    private void loadBanner(String bannerUrl) {
        Glide.with(this)
                .load(bannerUrl)
                .placeholder(R.drawable.ic_slider_head)
                .error(android.R.drawable.stat_notify_error)
                .dontTransform()
                .into(ivBanner);
    }

    private void loadBanner(Merchant merchant, Category category) {
        String bannerUrl = ProductManagerUtils.getImage(category);
        if (TextUtils.isEmpty(bannerUrl)) {
            bannerUrl = MerchantManagerUtils.getBanner(merchant);
        }
        loadBanner(bannerUrl);
    }

    private void loadProducts(@Nullable Merchant merchant, @Nullable Category category) {
        loadProducts(category);
        showCategoryChoicer(merchant, category);
    }

    private void loadProducts(@Nullable Category category) {
        List<DisplayProduct> displayProductList = ProductManagerUtils.getProductFromCategory(category);
        if (displayProductList == null) {
            displayError(getString(R.string.msg_missing_displayproductlist_in_category));
        } else {
            displayProductList(displayProductList);
        }
    }

    private void showCategoryChoicer(@Nullable Merchant merchant, @Nullable Category category) {
        if (merchant == null) {
            // We are not possible to fetch category
            displayError(getString(R.string.msg_not_possible_to_fetch_categories));
            return;
        }
        // Choice category
        List<Category> categoryList = ProductManagerUtils.getCategoryList(category);
        if (categoryList != null && categoryList.size() > 0) {
            DialogUtils.showChoicer(
                    ProductListActivity.this,
                    getString(R.string.select_category_title),
                    categoryList,
                    (spinnerView, item) -> requestCategory(merchant, item.getId())
            );
        } else {
            Toast.makeText(this, R.string.msg_category_not_found, Toast.LENGTH_SHORT).show();
        }
    }


    // Base request
    private void requestCategory(@NonNull Merchant merchant,
                                 @Nullable String categoryId,
                                 @NonNull ProductManagerUtils.GetCategoryCallback callback) {
        ProductManagerUtils.getCategory(
                productManager,
                merchant.getId(),
                categoryId,
                callback
        );
    }

    // Initial request
    private void initialRequestCategory(@NonNull Merchant merchant) {
        requestCategory(merchant, null, new BaseGetCategoryCallback() {
            @Override
            public void onSuccess(Category category) {
                initFab(merchant, category);
                onRequestCategorySuccess(merchant, category);
            }
        });
    }

    // Normal request
    private void requestCategory(@NonNull Merchant merchant, @Nullable String categoryId) {
        requestCategory(merchant, categoryId, new BaseGetCategoryCallback() {
            @Override
            public void onSuccess(Category category) {
                onRequestCategorySuccess(merchant, category);
            }
        });
    }

    // Just reduce code duplication
    private void onRequestCategorySuccess(@NonNull Merchant merchant, @Nullable Category category) {
        loadBanner(merchant, category);
        if (category == null) {
            displayError(getString(R.string.msg_category_not_found_in_merchant, merchant.toString()));
        } else {
            loadProducts(merchant, category);
        }
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
            displayError(rezolveError.getMessage());
        }
    }

    private abstract class BaseGetCategoryCallback extends BaseProcessingInterface
            implements ProductManagerUtils.GetCategoryCallback {
    }

}
