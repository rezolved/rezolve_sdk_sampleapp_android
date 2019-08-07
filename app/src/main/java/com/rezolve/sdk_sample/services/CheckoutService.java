package com.rezolve.sdk_sample.services;

import android.support.annotation.NonNull;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.callbacks.AddressbookCallback;
import com.rezolve.sdk.core.callbacks.CheckoutV2Callback;
import com.rezolve.sdk.core.callbacks.PaymentOptionCallback;
import com.rezolve.sdk.core.callbacks.PhonebookCallback;
import com.rezolve.sdk.core.callbacks.WalletCallback;
import com.rezolve.sdk.core.managers.AddressbookManager;
import com.rezolve.sdk.core.managers.CheckoutManagerV2;
import com.rezolve.sdk.core.managers.PaymentOptionManager;
import com.rezolve.sdk.core.managers.PhonebookManager;
import com.rezolve.sdk.core.managers.WalletManager;
import com.rezolve.sdk.model.cart.CheckoutBundleV2;
import com.rezolve.sdk.model.cart.CheckoutProduct;
import com.rezolve.sdk.model.cart.CustomConfigurableOption;
import com.rezolve.sdk.model.cart.Order;
import com.rezolve.sdk.model.cart.PaymentRequest;
import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.customer.Phone;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk.model.shop.DeliveryUnit;
import com.rezolve.sdk.model.shop.OrderSummary;
import com.rezolve.sdk.model.shop.PaymentOption;
import com.rezolve.sdk.model.shop.Product;
import com.rezolve.sdk.model.shop.SupportedPaymentMethod;
import com.rezolve.sdk_sample.services.callbacks.CheckoutCallback;
import com.rezolve.sdk_sample.services.callbacks.PaymentCallback;
import com.rezolve.sdk_sample.utils.CheckoutUtils;
import com.rezolve.sdk_sample.utils.CustomerUtils;

import java.util.List;

public class CheckoutService {

    private AddressbookManager addressbookManager;
    private PhonebookManager phonebookManager;
    private WalletManager walletManager;
    private CheckoutManagerV2 checkoutManager;
    private PaymentOptionManager paymentOptionManager;

    private Product product;
    private CheckoutProduct checkout;
    private PaymentOption payment;

    private Address deliveryAddress;
    private Phone customerPhone;
    private PaymentCard customerPaymentCard;
    private CheckoutBundleV2 checkoutBundle;

    private CheckoutCallback checkoutCallback;
    private PaymentCallback paymentCallback;

    private static CheckoutService instance;

    public static CheckoutService getInstance() {
        if (instance == null) {
            instance = new CheckoutService();
        }
        return instance;
    }

    public CheckoutService() {
        RezolveSession rezolveSession = RezolveSDK.peekInstance().getRezolveSession();
        paymentOptionManager = rezolveSession.getPaymentOptionManager();
        addressbookManager = rezolveSession.getAddressbookManager();
        phonebookManager = rezolveSession.getPhonebookManager();
        walletManager = rezolveSession.getWalletManager();
        checkoutManager = rezolveSession.getCheckoutManagerV2();
    }

    public void checkoutProduct(Product product, int quantity, List<CustomConfigurableOption> customConfigurableOptionList, CheckoutCallback checkoutCallback) {
        this.product = product;
        this.checkoutCallback = checkoutCallback;

        checkout = new CheckoutProduct();
        checkout.setId(Integer.parseInt(product.getId()));
        checkout.setQty(quantity);
        if (customConfigurableOptionList != null) {
            for (CustomConfigurableOption customConfigurableOption : customConfigurableOptionList) {
                CheckoutUtils.addCustomConfigurableOption(checkout, customConfigurableOption);
            }
        }

        String merchantId = product.getMerchantId();
        paymentOptionManager.getProductOptions(checkout, merchantId, new PaymentOptionCallback() {
            @Override
            public void onProductOptionsSuccess(PaymentOption paymentOption) {
                payment = paymentOption;
                createDeliveryAddress();
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                checkoutCallback.onCheckoutFailure(rezolveError.getMessage());
            }
        });
    }

    public void buyProduct(String orderId, PaymentCallback callback) {
        paymentCallback = callback;

        String ccv = CustomerUtils.getCustomerPaymentCardCCV();
        PaymentRequest paymentRequest = checkoutManager.createPaymentRequest(customerPaymentCard, ccv);

        checkoutManager.buyProduct(paymentRequest, checkoutBundle, orderId, null, new CheckoutV2Callback() {
            @Override
            public void onProductOptionBuySuccess(OrderSummary orderSummary) {
                paymentCallback.onPurchaseSuccess(orderSummary);
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                paymentCallback.onPurchaseFailure(rezolveError.getMessage());
            }
        });
    }

    private void createDeliveryAddress() {
        if (deliveryAddress != null) {
            createCustomerPhone();
        }

        Address address = CustomerUtils.getCustomerAddress();
        addressbookManager.create(address, new AddressbookCallback() {
            @Override
            public void onAddressbookCreateSuccess(Address address) {
                deliveryAddress = address;
                createCustomerPhone();
            }
        });
    }

    private void createCustomerPhone() {
        if (customerPhone != null) {
            createCustomerPaymentCard();
        }

        Phone phone = CustomerUtils.getCustomerPhone();
        phonebookManager.create(phone, new PhonebookCallback() {
            @Override
            public void onPhonebookCreateSuccess(Phone phone) {
                customerPhone = phone;
                createCustomerPaymentCard();
            }
        });
    }

    private void createCustomerPaymentCard() {
        if (customerPaymentCard != null) {
            addProductToCheckout();
        }

        PaymentCard paymentCard = CustomerUtils.getCustomerPaymentCard(deliveryAddress.getId());
        walletManager.create(paymentCard, new WalletCallback() {
            @Override
            public void onWalletCreateSuccess(PaymentCard paymentCard) {
                customerPaymentCard = paymentCard;
                addProductToCheckout();
            }
        });
    }

    private void addProductToCheckout() {
        if (payment.getSupportedPaymentMethods().size() == 0) {
            checkoutCallback.onCheckoutFailure("Missing supported payment methods");
            return;
        }
        // Gets first supported payment method
        SupportedPaymentMethod paymentMethod = payment.getSupportedPaymentMethods().get(0);
        DeliveryUnit deliveryUnit = new DeliveryUnit(paymentMethod, deliveryAddress.getId());

        checkoutBundle = CheckoutBundleV2.createProductCheckoutBundleV2(product.getMerchantId(), payment.getId(),
                checkout, customerPhone.getId(), paymentMethod, deliveryUnit);

        checkoutManager.checkoutProductOption(checkoutBundle, new CheckoutV2Callback() {
            @Override
            public void onProductOptionCheckoutSuccess(Order order) {
                checkoutCallback.onCheckoutSuccess(order);
            }

            @Override
            public void onError(@NonNull RezolveError rezolveError) {
                checkoutCallback.onCheckoutFailure(rezolveError.getMessage());
            }
        });
    }


}
