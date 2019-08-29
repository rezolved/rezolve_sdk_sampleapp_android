package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.rezolve.sdk.core.callbacks.UserActivityCallback;
import com.rezolve.sdk.core.managers.UserActivityManager;
import com.rezolve.sdk.model.cart.PriceBreakdown;
import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.history.OrderDetails;
import com.rezolve.sdk.model.history.OrderHistoryObject;
import com.rezolve.sdk.model.shop.OrderProduct;
import com.rezolve.sdk_sample.providers.SdkProvider;
import com.rezolve.sdk_sample.utils.CustomerUtils;
import static com.rezolve.sdk_sample.utils.PriceConstants.*;

import java.util.List;


public class OrderSummaryActivity extends AppCompatActivity {

    private TextView orderIdTextView;
    private TextView productNameTextView;
    private TextView productQuantityTextView;
    private TextView paymentDetailsTextView;
    private TextView deliveryDetailsTextView;
    private TextView subtotalPriceTextView;
    private TextView taxTextView;
    private TextView shippingTextView;
    private TextView totalPriceTextView;
    private TextView merchantEmailTextView;
    private TextView merchantNameTextView;
    private TextView merchantPhoneTextView;
    private Button continueShoppingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        // Bind views
        orderIdTextView = findViewById(R.id.orderIdTextView);
        productNameTextView = findViewById(R.id.productNameTextView);
        productQuantityTextView = findViewById(R.id.productQuantityTextView);
        paymentDetailsTextView = findViewById(R.id.paymentDetailsTextView);
        deliveryDetailsTextView = findViewById(R.id.deliveryDetailsTextView);
        subtotalPriceTextView = findViewById(R.id.subtotalPriceTextView);
        taxTextView = findViewById(R.id.taxTextView);
        shippingTextView = findViewById(R.id.shippingTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        merchantEmailTextView = findViewById(R.id.merchantEmailTextView);
        merchantNameTextView = findViewById(R.id.merchantNameTextView);
        merchantPhoneTextView = findViewById(R.id.merchantPhoneTextView);
        continueShoppingButton = findViewById(R.id.continueShoppingButton);

        continueShoppingButton.setOnClickListener(view -> navigateToScannerView());

        String orderId = getIntent().getStringExtra("order_id");
        getOrderDetails(orderId);
    }

    private void getOrderDetails(String orderId) {
        UserActivityManager userActivityManager = SdkProvider.getInstance()
                .getSDK()
                .getRezolveSession()
                .getUserActivityManager();

        userActivityManager.getOrdersV3(new UserActivityCallback() {
            @Override
            public void onGetOrdersSuccess(OrderHistoryObject orderHistoryObject) {
                super.onGetOrdersSuccess(orderHistoryObject);

                for (OrderDetails order : orderHistoryObject.getOrders()) {
                    if (order.getOrderId().equals(orderId)) {
                        displayOrderDetails(order);
                        break;
                    }
                }
            }
        }, null, null);
    }

    private void displayOrderDetails(OrderDetails orderDetails) {
        String totalPrice = PRICE_PREFIX + String.valueOf(orderDetails.getPrice().getFinalPrice());
        OrderProduct product = orderDetails.getItems().get(0);
        Address customerAddress = CustomerUtils.getCustomerAddress();
        PaymentCard customerPaymentCard = CustomerUtils.getCustomerPaymentCard(customerAddress.getId());

        // displays ordered product details
        orderIdTextView.setText(orderDetails.getOrderId());
        productNameTextView.setText(product.getTitle());
        paymentDetailsTextView.setText(customerPaymentCard.getBrand() + " " + CustomerUtils.getCustomerPaymentCardPan());
        deliveryDetailsTextView.setText(customerAddress.getLine1() + " " + customerAddress.getCity());

        // displays prices
        displayOrderPriceDetails(orderDetails.getPrice().getBreakdowns());
        totalPriceTextView.setText(totalPrice);

        // displays merchant details
        merchantEmailTextView.setText(orderDetails.getMerchantEmail());
        merchantNameTextView.setText(orderDetails.getMerchantName());
        merchantPhoneTextView.setText(orderDetails.getMerchantPhone());
    }

    private void displayOrderPriceDetails(List<PriceBreakdown> priceBreakdowns) {
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
            }
        }
    }

    private void navigateToScannerView() {
        Intent intent = new Intent(OrderSummaryActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
