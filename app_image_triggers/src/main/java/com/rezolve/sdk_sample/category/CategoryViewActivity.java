package com.rezolve.sdk_sample.category;

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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.callbacks.ProductCallback;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.Category;
import com.rezolve.sdk.model.shop.DisplayProduct;
import com.rezolve.sdk.model.shop.Merchant;
import com.rezolve.sdk.model.shop.PageNavigationFilter;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk_sample.R;
import com.rezolve.sdk_sample.navigation.Navigator;
import com.rezolve.shared.model.CategoryItem;
import com.rezolve.shared.utils.DialogUtils;
import com.rezolve.shared.utils.sdk.MerchantManagerUtils;
import com.rezolve.shared.utils.sdk.ProductManagerUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class CategoryViewActivity extends AppCompatActivity {

    private final static String TAG = CategoryViewActivity.class.getSimpleName();

    public static final String PARAM_MERCHANT_JSON_KEY = "merchantJson";
    public static final String PARAM_CATEGORY_JSON_KEY = "categoryJson";
    private SpinKitView loadingSpinView;
    private FloatingActionButton fabMain;
    private ImageView ivBanner;
    private RecyclerView recyclerView;
    private TextView tvMessage;

    private Merchant merchant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // Get external parameters
        Intent intent = getIntent();
        merchant = getMerchant(intent);
        Category category = getCategory(intent);

        // Bind views
        loadingSpinView = findViewById(R.id.loadingSpinView);
        fabMain = findViewById(R.id.fabMain);
        ivBanner = findViewById(R.id.ivBanner);
        tvMessage = findViewById(R.id.tvMessage);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        CategoryViewAdapter adapter = new CategoryViewAdapter(adapterListener);
        recyclerView.setAdapter(adapter);

        // load data
        loadBanner(merchant, category);

        if (category == null) {
            requestCategoryDetails(merchant, null); // initialRequestCategory -> initFab -> loadProducts
        } else {
            initFab(merchant, category);
            loadItems(merchant, category);
        }
    }

    private final CategoryViewAdapter.ClickListener adapterListener = item -> {
        showLoadingIndicator();
        switch (item.getType()) {
            case PRODUCT -> {
                DisplayProduct displayProduct = item.getDisplayProduct();
                if (displayProduct != null) {
                    requestProductDetails(merchant, displayProduct);
                }
            }
            case CATEGORY -> {
                if (item.getCategory() != null) {
                    requestCategoryDetails(merchant, item.getCategory().getId());
                }
            }
        }
    };

    private JSONObject createJsonObject(Intent intent, String intentParamKey) throws NullPointerException, JSONException {
        return intent == null ? null : new JSONObject(intent.getStringExtra(intentParamKey));
    }

    @NonNull
    private Merchant getMerchant(Intent intent) {
        try {
            return Merchant.jsonToEntity(createJsonObject(intent, PARAM_MERCHANT_JSON_KEY));
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Merchant object is required");
    }

    private Category getCategory(Intent intent) {
        try {
            return Category.jsonToEntity(createJsonObject(intent, PARAM_CATEGORY_JSON_KEY));
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initFab(Merchant merchant, Category category) {
        if (fabMain != null) {
            fabMain.setOnClickListener(view -> showCategorySelector(merchant, category));
        }
    }

    // TODO: code duplication:
    private void showLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.GONE);
    }
    // TODO: code duplication ^^^

    private void loadBanner(Merchant merchant, Category category) {
        String bannerUrl = ProductManagerUtils.getImage(category);
        if (TextUtils.isEmpty(bannerUrl)) {
            bannerUrl = MerchantManagerUtils.getBanner(merchant);
        }
        if (bannerUrl != null) {
            Glide.with(this)
                    .load(bannerUrl)
                    .placeholder(R.drawable.ic_slider_head)
                    .error(android.R.drawable.stat_notify_error)
                    .dontTransform()
                    .into(ivBanner);
        }
    }

    private void loadItems(@Nullable Merchant merchant, @Nullable Category category) {
        List<CategoryItem> itemList = ProductManagerUtils.getProductsAndSubcategoriesFromCategory(category);
        if (itemList == null) {
            displayError(getString(R.string.msg_missing_displayproductlist_in_category));
        } else {
            displayItems(itemList);
        }

        showCategorySelector(merchant, category);
    }

    /**
     *  Each category in Rezolve ecosystem can have subcategories. Each subcategory can have
     *  it's own subcategories. It's up to your UX design to decide how you want to show them.
     *  In this example to navigate down the category tree we show a floating button with selector menu.
     */
    private void showCategorySelector(@Nullable Merchant merchant, @Nullable Category category) {
        if (merchant == null) {
            // To fetch category details we need merchant ID
            displayError(getString(R.string.msg_not_possible_to_fetch_categories));
            return;
        }
        List<Category> categoryList = ProductManagerUtils.getCategoryList(category);
        if (categoryList != null && categoryList.size() > 0) {
            DialogUtils.showSelector(
                    CategoryViewActivity.this,
                    getString(R.string.select_category_title),
                    categoryList,
                    (spinnerView, item) -> requestCategoryDetails(merchant, item.getId())
            );
        } else {
            Log.d(TAG, "showCategorySelector: No subcategories found");
        }
    }

    /**
     *  To request Merchant's default Category pass 'null' as Category ID.
     *  To request selected subcategory you have to pass selected Category ID.
     *
     *  Category object contains paginated lists of products and subcategories.
     *  In this example we only fetch first page of results, but if the number of items exceeds
     *  page size you might need to follow with more requests with incremented page indexes.
     */
    private void requestCategoryDetails(@NonNull Merchant merchant, @Nullable String categoryID) {
        showLoadingIndicator();
        PageNavigationFilter categoryFilter = PageNavigationFilter.getDefault(0);
        PageNavigationFilter productFilter = PageNavigationFilter.getDefault(0);

        RezolveSDK.peekInstance().getRezolveSession().getProductManager().getProductsAndCategories(
                merchant.getId(),
                categoryID,
                categoryFilter,
                productFilter,
                new ProductCallback() {
                    @Override
                    public void onGetProductsAndCategoriesSuccess(Category category) {
                        hideLoadingIndicator();
                        initFab(merchant, category);
                        onRequestCategorySuccess(merchant, category);
                    }

                    @Override
                    public void onError(@NonNull RezolveError error) {
                        hideLoadingIndicator();
                        Log.e(TAG, "requestCategoryDetails error: "+error.getMessage());
                    }
                }
        );
    }

    private void requestProductDetails(@NonNull Merchant merchant, @NonNull DisplayProduct displayProduct) {
        RezolveSDK.peekInstance().getRezolveSession().getProductManager().getProduct(
                merchant.getId(),
                displayProduct.getCategoryId(),
                displayProduct.getId(),
                new ProductCallback() {
                    @Override
                    public void onGetProductSuccess(Product product) {
                        hideLoadingIndicator();
                        Navigator.navigateToProductDetails(product, CategoryViewActivity.this);
                    }

                    @Override
                    public void onError(@NonNull RezolveError error) {
                        hideLoadingIndicator();
                        Log.e(TAG, "getProduct error: "+error.getMessage());
                    }
                }
        );
    }

    private void onRequestCategorySuccess(@NonNull Merchant merchant, @Nullable Category category) {
        loadBanner(merchant, category);
        if (category == null) {
            displayError(getString(R.string.msg_category_not_found_in_merchant, merchant.toString()));
        } else {
            loadItems(merchant, category);
        }
    }

    private void displayItems(@NonNull List<CategoryItem> itemList) {
        if (itemList.isEmpty()) {
            displayMessage(getString(R.string.empty_list));
        } else {
            tvMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            ((CategoryViewAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setItems(itemList);
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
}
