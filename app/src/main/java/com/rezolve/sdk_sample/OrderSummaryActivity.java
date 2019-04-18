package com.rezolve.sdk_sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.rezolve.sdk.core.callbacks.UserActivityCallback;
import com.rezolve.sdk.core.managers.UserActivityManager;
import com.rezolve.sdk.model.history.OrderDetails;
import com.rezolve.sdk.model.history.OrderHistoryObject;
import com.rezolve.sdk_sample.providers.SdkProvider;

public class OrderSummaryActivity extends AppCompatActivity {

    TextView orderIdTextView;
    TextView statusTextView;
    TextView titleTextView;
    TextView priceTextView;
    Button continueShoppingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        // Bind views
        orderIdTextView = findViewById(R.id.orderIdTextView);
        statusTextView = findViewById(R.id.statusTextView);
        titleTextView = findViewById(R.id.titleTextView);
        priceTextView = findViewById(R.id.priceTextView);
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

                for(OrderDetails order : orderHistoryObject.getOrders()) {
                    if(order.getOrderId().equals(orderId)){
                        displayOrderDetails(order);
                    }
                }
            }
        }, null, null);
    }

    private void displayOrderDetails(OrderDetails orderDetails) {
        String totalPrice = String.valueOf(orderDetails.getPrice());

        orderIdTextView.setText(orderDetails.getOrderId());
        statusTextView.setText(orderDetails.getStatus());
        titleTextView.setText(orderDetails.getMerchantName());
        priceTextView.setText(totalPrice);
    }

    private void navigateToScannerView() {
        Intent intent = new Intent(OrderSummaryActivity.this, ScanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
