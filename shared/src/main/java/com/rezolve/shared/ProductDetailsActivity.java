package com.rezolve.shared;

import static com.rezolve.shared.utils.PriceConstants.PRICE_BREAKDOWN_DISCOUNT;
import static com.rezolve.shared.utils.PriceConstants.PRICE_BREAKDOWN_SHIPPING;
import static com.rezolve.shared.utils.PriceConstants.PRICE_BREAKDOWN_SUBTOTAL;
import static com.rezolve.shared.utils.PriceConstants.PRICE_BREAKDOWN_TAX;
import static com.rezolve.shared.utils.PriceConstants.PRICE_PREFIX;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.ybq.android.spinkit.SpinKitView;
import com.rezolve.sdk.model.cart.CustomConfigurableOption;
import com.rezolve.sdk.model.cart.Order;
import com.rezolve.sdk.model.cart.PriceBreakdown;
import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.shop.OrderSummary;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.shared.services.CheckoutService;
import com.rezolve.shared.services.callbacks.CheckoutCallback;
import com.rezolve.shared.services.callbacks.PaymentCallback;
import com.rezolve.shared.utils.CheckoutUtils;
import com.rezolve.shared.utils.CustomerUtils;
import com.rezolve.shared.utils.DialogUtils;
import com.rezolve.shared.utils.ProductUtils;
import com.synnapps.carouselview.CarouselView;

import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private SpinKitView loadingSpinView;
    private CarouselView previewCarouselView;
    private TextView titleTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private Button quantityIncreaseButton;
    private Button quantityDecreaseButton;
    private TextView paymentDetailsTextView;
    private TextView deliveryDetailsTextView;
    private TextView subtotalPriceTextView;
    private TextView taxTextView;
    private TextView shippingTextView;
    private TextView discountTextView;
    private TextView totalPriceTextView;
    private Button instantBuyButton;

    private Product product;
    private CheckoutService checkoutService;

    private int productQuantity = 1;
    private List<CustomConfigurableOption> customConfigurableOptionList;
    private String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Bind views
        loadingSpinView = findViewById(R.id.loadingSpinView);
        previewCarouselView = findViewById(R.id.previewCarouselView);
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        quantityIncreaseButton = findViewById(R.id.quantityIncreaseButton);
        quantityDecreaseButton = findViewById(R.id.quantityDecreaseButton);
        paymentDetailsTextView = findViewById(R.id.paymentDetailsTextView);
        deliveryDetailsTextView = findViewById(R.id.deliveryDetailsTextView);
        subtotalPriceTextView = findViewById(R.id.subtotalPriceTextView);
        taxTextView = findViewById(R.id.taxTextView);
        shippingTextView = findViewById(R.id.shippingTextView);
        discountTextView = findViewById(R.id.discountTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        instantBuyButton = findViewById(R.id.instantBuyButton);

        quantityIncreaseButton.setOnClickListener(view -> increaseQuantity());
        quantityDecreaseButton.setOnClickListener(view -> decreaseQuantity());
        instantBuyButton.setOnClickListener(view -> buyProduct());

        checkoutService = CheckoutService.getInstance();

        product = ProductUtils.getProductFromArgs(getIntent().getExtras());
        customConfigurableOptionList = CheckoutUtils.getDefaultCustomConfigurableOptionList(product);

        displayProductDetails();
        displayCustomerDetails();
    }

    private void displayProductDetails() {
        previewCarouselView.setPageCount(product.getImages().size());

        previewCarouselView.setImageListener((position, imageView) -> {
            String imageUrl = product.getImages().get(position);
            Glide.with(this).load(imageUrl).into(imageView);
        });

        titleTextView.setText(product.getTitle());
        priceTextView.setText(PRICE_PREFIX + product.getPrice());

        displayQuantity();
        checkoutProduct();
    }

    private void displayCustomerDetails() {
        Address customerAddress = CustomerUtils.getCustomerAddress();
        PaymentCard customerPaymentCard = CustomerUtils.getCustomerPaymentCard(customerAddress.getId());

        paymentDetailsTextView.setText(customerPaymentCard.getBrand() + " " + CustomerUtils.getCustomerPaymentCardPan());
        deliveryDetailsTextView.setText(customerAddress.getLine1() + " " + customerAddress.getCity());
    }

    private void increaseQuantity() {
        productQuantity++;
        displayQuantity();
        // checkout needs to be refreshed with new quantity value
        checkoutProduct();
    }

    private void decreaseQuantity() {
        if (productQuantity == 1) {
            return;
        }

        productQuantity--;
        displayQuantity();
        // checkout needs to be refreshed with new quantity value
        checkoutProduct();
    }

    private void displayQuantity() {
        String quantityPrefix = getResources().getString(R.string.quantity);
        quantityTextView.setText(quantityPrefix + " " + productQuantity);
    }

    private void checkoutProduct() {
        displayLoadingIndicator();

        checkoutService.checkoutProduct(product, productQuantity, customConfigurableOptionList, new CheckoutCallback() {
            @Override
            public void onCheckoutSuccess(Order order) {
                if(!ProductDetailsActivity.this.isFinishing()) {
                    hideLoadingIndicator();

                    orderId = order.getOrderId();
                    displayCheckoutPriceDetails(order.getBreakdowns());
                    displayCheckoutTotalPrice(order.getFinalPrice());
                }
            }

            @Override
            public void onCheckoutFailure(String message) {
                if(!ProductDetailsActivity.this.isFinishing()) {
                    hideLoadingIndicator();
                    DialogUtils.showError(ProductDetailsActivity.this, message);
                }
            }
        });
    }

    private void displayCheckoutPriceDetails(List<PriceBreakdown> priceBreakdowns) {
        for (PriceBreakdown priceBreakdown : priceBreakdowns) {
            String price = PRICE_PREFIX + String.valueOf(priceBreakdown.getAmount());

            switch (priceBreakdown.getType()) {
                case PRICE_BREAKDOWN_SUBTOTAL:
                    subtotalPriceTextView.setText(price);
                    break;
                case PRICE_BREAKDOWN_TAX:
                    taxTextView.setText(price);
                    break;
                case PRICE_BREAKDOWN_SHIPPING:
                    shippingTextView.setText(price);
                    break;
                case PRICE_BREAKDOWN_DISCOUNT:
                    discountTextView.setText(price);
                    break;
            }
        }
    }

    private void displayCheckoutTotalPrice(Float totalPrice) {
        String price = PRICE_PREFIX + String.valueOf(totalPrice);
        totalPriceTextView.setText(price);
    }

    private void buyProduct() {
        if (orderId == null) {
            return;
        }

        displayLoadingIndicator();

        checkoutService.buyProduct(orderId, new PaymentCallback() {
            @Override
            public void onPurchaseSuccess(OrderSummary orderSummary) {
                navigateToOrderSummaryView(orderSummary);
            }

            @Override
            public void onPurchaseFailure(String message) {
                hideLoadingIndicator();
                DialogUtils.showError(ProductDetailsActivity.this, message);
            }
        });
    }

    private void displayLoadingIndicator() {
        loadingSpinView.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        loadingSpinView.setVisibility(View.GONE);
    }

    private void navigateToOrderSummaryView(OrderSummary orderSummary) {
        Intent intent = new Intent(ProductDetailsActivity.this, OrderSummaryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Bundle bundle = new Bundle();
        bundle.putString("order_id", orderSummary.getOrderId());
        intent.putExtras(bundle);

        startActivity(intent);
    }

    private void navigateToMainView() {
        Intent intent = new Intent(ProductDetailsActivity.this, ((MainActivityProvider)getApplicationContext()).getMainActivity());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToMainView();
    }
}
