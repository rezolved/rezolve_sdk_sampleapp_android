package com.rezolve.sdk_sample.model;

import com.google.gson.annotations.SerializedName;

public class RegistrationResponse {
    @SerializedName("entity_id")
    private String entityId;

    @SerializedName("partner_id")
    private String partnerId;

    public String getEntityId() {
        return entityId;
    }

    public String getPartnerId() {
        return partnerId;
    }
}
