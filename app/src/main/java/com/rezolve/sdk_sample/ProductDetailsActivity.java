package com.rezolve.sdk_sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.callbacks.AddressbookCallback;
import com.rezolve.sdk.core.callbacks.CheckoutV2Callback;
import com.rezolve.sdk.core.callbacks.PhonebookCallback;
import com.rezolve.sdk.core.callbacks.WalletCallback;
import com.rezolve.sdk.core.interfaces.CheckoutV2Interface;
import com.rezolve.sdk.core.interfaces.PaymentOptionInterface;
import com.rezolve.sdk.core.interfaces.PhonebookInterface;
import com.rezolve.sdk.core.managers.AddressbookManager;
import com.rezolve.sdk.core.managers.CheckoutManagerV2;
import com.rezolve.sdk.core.managers.PaymentOptionManager;
import com.rezolve.sdk.core.managers.PhonebookManager;
import com.rezolve.sdk.core.managers.WalletManager;
import com.rezolve.sdk.model.cart.CheckoutBundleV2;
import com.rezolve.sdk.model.cart.CheckoutProduct;
import com.rezolve.sdk.model.cart.Order;
import com.rezolve.sdk.model.cart.PaymentRequest;
import com.rezolve.sdk.model.cart.PriceBreakdown;
import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.customer.Phone;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.DeliveryUnit;
import com.rezolve.sdk.model.shop.OrderSummary;
import com.rezolve.sdk.model.shop.PaymentOption;
import com.rezolve.sdk.model.shop.SupportedDeliveryMethod;
import com.rezolve.sdk.model.shop.SupportedPaymentMethod;
import com.rezolve.sdk_sample.model.ProductDetails;
import com.rezolve.sdk_sample.utils.CustomerUtils;
import com.synnapps.carouselview.CarouselView;

import org.parceler.Parcels;

import java.util.List;

public class ProductDetailsActivity extends AppCompatActivity {

    private final String PRICE_PREFIX = "$";

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

    private RezolveSession rezolveSession;

    private AddressbookManager addressbookManager;
    private PhonebookManager phonebookManager;
    private CheckoutManagerV2 checkoutManager;

    private ProductDetails productDetails;
    private CheckoutProduct checkout;
    private PaymentOption payment;

    private Address deliveryAddress;
    private Phone customerPhone;
    private WalletManager walletManager;
    private PaymentCard customerPaymentCard;
    private CheckoutBundleV2 checkoutBundle;

    private int productQuantity = 1;

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

        quantityIncreaseButton.setOnClickListener(view -> increaseQuantity());
        quantityDecreaseButton.setOnClickListener(view -> decreaseQuantity());
    }

    @Override
    protected void onResume() {
        super.onResume();

        rezolveSession = RezolveSDK.peekInstance().getRezolveSession();
        addressbookManager = rezolveSession.getAddressbookManager();
        phonebookManager = rezolveSession.getPhonebookManager();
        walletManager = rezolveSession.getWalletManager();
        checkoutManager = rezolveSession.getCheckoutManagerV2();

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

        getPaymentOptionManager();
    }

    private void getPaymentOptionManager() {
        PaymentOptionManager paymentOptionManager = RezolveSDK.peekInstance().getRezolveSession().getPaymentOptionManager();

        checkout = new CheckoutProduct();
        checkout.setId(Integer.parseInt(productDetails.getId()));
        checkout.setQty(1);

        String merchantId = productDetails.getMerchantId();
        paymentOptionManager.getProductOptions(checkout, merchantId, new PaymentOptionInterface() {
            @Override
            public void onProductOptionsSuccess(PaymentOption paymentOption) {
                payment = paymentOption;
                initializeCheckout();
            }

            @Override
            public void onCartOptionsSuccess(List<PaymentOption> list) {

            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {

            }
        });
    }

    private void increaseQuantity() {
        productQuantity++;
        displayQuantity();
    }

    private void decreaseQuantity() {
        if (productQuantity == 1) {
            return;
        }

        productQuantity--;
        displayQuantity();
    }

    private void displayQuantity() {
        String quantityPrefix = getResources().getString(R.string.quantity);
        quantityTextView.setText(quantityPrefix + " " + productQuantity);
    }

    private void initializeCheckout() {
        setDeliveryAddress();
    }

    private void setDeliveryAddress() {
        if (deliveryAddress != null) {
            setCustomerPhone();
        }

        Address address = CustomerUtils.getCustomerAddress();
        addressbookManager.create(address, new AddressbookCallback() {
            @Override
            public void onAddressbookCreateSuccess(Address address) {
                deliveryAddress = address;
                setCustomerPhone();
            }
        });
    }

    private void setCustomerPhone() {
        if (customerPhone != null) {
            setCustomerPaymentCard();
        }

        Phone phone = CustomerUtils.getCustomerPhone();
        phonebookManager.create(phone, new PhonebookCallback() {
            @Override
            public void onPhonebookCreateSuccess(Phone phone) {
                customerPhone = phone;
                setCustomerPaymentCard();
            }
        });
    }

    private void setCustomerPaymentCard() {
        if (customerPaymentCard != null) {
            checkoutProduct();
        }

        PaymentCard paymentCard = CustomerUtils.getCustomerPaymentCard();
        walletManager.create(paymentCard, new WalletCallback() {
            @Override
            public void onWalletCreateSuccess(PaymentCard paymentCard) {
                customerPaymentCard = paymentCard;
                checkoutProduct();
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                super.onError(rezolveError);
            }
        });
    }

    private void checkoutProduct() {
        // Gets first supported payment method
        SupportedPaymentMethod paymentMethod = payment.getSupportedPaymentMethods().get(0);

        DeliveryUnit deliveryUnit = new DeliveryUnit(paymentMethod, deliveryAddress.getId());
        CheckoutBundleV2 checkoutBundle = CheckoutBundleV2.createProductCheckoutBundleV2(productDetails.getMerchantId(), payment.getId(), checkout, customerPhone.getId(), paymentMethod, deliveryUnit);

        checkoutManager.checkoutProductOption(checkoutBundle, new CheckoutV2Callback() {
            @Override
            public void onProductOptionCheckoutSuccess(Order order) {
                super.onProductOptionCheckoutSuccess(order);

                updateCheckoutPriceDetails(order.getBreakdowns());
                updateCheckoutTotalPrice(order.getFinalPrice());
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                super.onError(rezolveError);
            }
        });
    }

    private void updateCheckoutPriceDetails(List<PriceBreakdown> priceBreakdowns) {
        for (PriceBreakdown priceBreakdown : priceBreakdowns) {
            String price = PRICE_PREFIX + String.valueOf(priceBreakdown.getAmount());

            switch (priceBreakdown.getType()) {
                case "unit":
                    subtotalPriceTextView.setText(price);
                    break;
                case "tax":
                    taxTextView.setText(price);
                    break;
                case "shipping":
                    shippingTextView.setText(price);
                    break;
                case "discount":
                    discountTextView.setText(price);
                    break;
            }
        }
    }

    private void updateCheckoutTotalPrice(Float totalPrice) {
        String price = PRICE_PREFIX + String.valueOf(totalPrice);
        totalPriceTextView.setText(price);
    }

    private void buyProduct() {
        String ccv = CustomerUtils.getCustomerPaymentCardCCV();
        PaymentRequest paymentRequest = checkoutManager.createPaymentRequest(customerPaymentCard, ccv);

        checkoutManager.buyProduct(paymentRequest, checkoutBundle, null, null, new CheckoutV2Callback() {
            @Override
            public void onProductOptionBuySuccess(OrderSummary order) {
                // TODO Navigate to summary page
            }
        });
    }
}
