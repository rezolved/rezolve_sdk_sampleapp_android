package com.rezolve.sdk_sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.model.cart.Order;
import com.rezolve.sdk.model.cart.PriceBreakdown;
import com.rezolve.sdk.model.shop.OrderSummary;
import com.rezolve.sdk_sample.model.ProductDetails;
import com.rezolve.sdk_sample.services.callbacks.CheckoutCallback;
import com.rezolve.sdk_sample.services.CheckoutService;
import com.rezolve.sdk_sample.services.callbacks.PaymentCallback;
import com.synnapps.carouselview.CarouselView;

import org.parceler.Parcels;

import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private final String PRICE_PREFIX = "$";
    private final String PRICE_BREAKDOWN_SUBTOTAL = "unit";
    private final String PRICE_BREAKDOWN_TAX = "tax";
    private final String PRICE_BREAKDOWN_SHIPPING = "shipping";
    private final String PRICE_BREAKDOWN_DISCOUNT = "discount";

    private CarouselView previewCarouselView;
    private TextView titleTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private Button quantityIncreaseButton;
    private Button quantityDecreaseButton;
    private TextView subtotalPriceTextView;
    private TextView taxTextView;
    private TextView shippingTextView;
    private TextView discountTextView;
    private TextView totalPriceTextView;
    private Button instantBuyButton;

    private ProductDetails productDetails;
    private CheckoutService checkoutService;

    private int productQuantity = 1;
    private String orderId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Bind views
        previewCarouselView = findViewById(R.id.previewCarouselView);
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTextView);
        quantityTextView = findViewById(R.id.quantityTextView);
        quantityIncreaseButton = findViewById(R.id.quantityIncreaseButton);
        quantityDecreaseButton = findViewById(R.id.quantityDecreaseButton);
        subtotalPriceTextView = findViewById(R.id.subtotalPriceTextView);
        taxTextView = findViewById(R.id.taxTextView);
        shippingTextView = findViewById(R.id.shippingTextView);
        discountTextView = findViewById(R.id.discountTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        instantBuyButton = findViewById(R.id.instantBuyButton);

        quantityIncreaseButton.setOnClickListener(view -> increaseQuantity());
        quantityDecreaseButton.setOnClickListener(view -> decreaseQuantity());
        instantBuyButton.setOnClickListener(view -> buyProduct());
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkoutService = CheckoutService.peekInstance();
        productDetails = Parcels.unwrap(getIntent().getParcelableExtra("product_details"));
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
        checkoutProduct();
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
        checkoutService.checkoutProduct(productDetails, productQuantity, new CheckoutCallback() {
            @Override
            public void onCheckoutSuccess(Order order) {
                orderId = order.getOrderId();
                displayCheckoutPriceDetails(order.getBreakdowns());
                displayCheckoutTotalPrice(order.getFinalPrice());
            }

            @Override
            public void onCheckoutFailure() {
                // TODO Display failure dialog
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
        checkoutService.buyProduct(orderId, new PaymentCallback() {
            @Override
            public void onPurchaseSuccess(OrderSummary orderSummary) {
                // TODO Navigate to summary page
            }

            @Override
            public void onPurchaseFailure() {
                // TODO Display failure dialog
            }
        });
    }
}
