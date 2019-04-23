package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.model.cart.Order;
import com.rezolve.sdk.model.cart.PriceBreakdown;
import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.shop.OrderSummary;
import com.rezolve.sdk_sample.model.ProductDetails;
import com.rezolve.sdk_sample.services.callbacks.CheckoutCallback;
import com.rezolve.sdk_sample.services.CheckoutService;
import com.rezolve.sdk_sample.services.callbacks.PaymentCallback;
import com.rezolve.sdk_sample.utils.CustomerUtils;
import com.rezolve.sdk_sample.utils.DialogUtils;
import com.synnapps.carouselview.CarouselView;
import static com.rezolve.sdk_sample.utils.PriceConstants.*;

import org.parceler.Parcels;

import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

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
        productDetails = Parcels.unwrap(getIntent().getParcelableExtra("product_details"));

        displayProductDetails();
        displayCustomerDetails();
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
        checkoutService.checkoutProduct(productDetails, productQuantity, new CheckoutCallback() {
            @Override
            public void onCheckoutSuccess(Order order) {
                orderId = order.getOrderId();
                displayCheckoutPriceDetails(order.getBreakdowns());
                displayCheckoutTotalPrice(order.getFinalPrice());
            }

            @Override
            public void onCheckoutFailure(String message) {
                DialogUtils.showError(getApplicationContext(), message);
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
                navigateToOrderSummaryView(orderSummary);
            }

            @Override
            public void onPurchaseFailure(String message) {
                DialogUtils.showError(ProductDetailsActivity.this, message);
            }
        });
    }

    private void navigateToOrderSummaryView(OrderSummary orderSummary) {
        Intent intent = new Intent(ProductDetailsActivity.this, OrderSummaryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Bundle bundle = new Bundle();
        bundle.putString("order_id", orderSummary.getOrderId());
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
