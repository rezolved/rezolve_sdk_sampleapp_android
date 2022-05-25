package com.rezolve.shared.utils;

import com.rezolve.sdk.model.customer.Address;
import com.rezolve.sdk.model.customer.PaymentCard;
import com.rezolve.sdk.model.customer.Phone;

public final class CustomerUtils {

    private static final String EMPTY_FIELD = "";

    private static final String CUSTOMER_NAME = "Harrison Brady";
    private static final String CUSTOMER_PHONE = "078 4104 7080";

    private static final String CUSTOMER_ADDRESS_FIRST_LINE = "68 Guild Street";
    private static final String CUSTOMER_ADDRESS_ZIP = "NW5 1BD";
    private static final String CUSTOMER_ADDRESS_CITY = "LONDON";
    private static final String CUSTOMER_ADDRESS_COUNTRY = "GB";

    private static final String CUSTOMER_CARD_PAN = "4000 2211 1111 1111";
    private static final String CUSTOMER_CARD_VALID_FROM = "0819";
    private static final String CUSTOMER_CARD_EXPIRATION = "0821";
    private static final String CUSTOMER_CARD_BRAND = "Visa";
    private static final String CUSTOMER_CARD_TYPE = "Debit";
    private static final String CUSTOMER_CARD_CCV = "154";

    public static Phone getCustomerPhone() {
        Phone phone = new Phone();
        phone.setName(CUSTOMER_NAME);
        phone.setPhone(CUSTOMER_PHONE);

        return phone;
    }

    public static Address getCustomerAddress() {
        Address address = new Address();
        address.setLine1(CUSTOMER_ADDRESS_FIRST_LINE);
        address.setZip(CUSTOMER_ADDRESS_ZIP);
        address.setCity(CUSTOMER_ADDRESS_CITY);
        address.setShortName(EMPTY_FIELD);
        address.setState(EMPTY_FIELD);
        address.setLine2(EMPTY_FIELD);
        address.setCountry(CUSTOMER_ADDRESS_COUNTRY);

        return address;
    }

    public static PaymentCard getCustomerPaymentCard(String addressId) {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setPan(CUSTOMER_CARD_PAN);
        paymentCard.setExpiresOn(CUSTOMER_CARD_EXPIRATION);
        paymentCard.setValidFrom(CUSTOMER_CARD_VALID_FROM);
        paymentCard.setAddressId(addressId);
        paymentCard.setBrand(CUSTOMER_CARD_BRAND);
        paymentCard.setNameOnCard(CUSTOMER_NAME);
        paymentCard.setShortName(CUSTOMER_NAME);
        paymentCard.setType(CUSTOMER_CARD_TYPE);

        return paymentCard;
    }

    public static String getCustomerPaymentCardPan() {
        return CUSTOMER_CARD_PAN;
    }

    public static String getCustomerPaymentCardCCV() {
        return CUSTOMER_CARD_CCV;
    }
}
