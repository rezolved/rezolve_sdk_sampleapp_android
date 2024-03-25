package com.rezolve.sdk_sample.utils;

import com.rezolve.sdk_sample.BuildConfig;
import com.rezolve.shared.utils.DateUtils;
import com.rezolve.shared.utils.DeviceUtils;

import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public final class TokenUtils {

    private static final String KEY_AUTH = "v2";
    private static final String KEY_ALG = "alg";
    private static final String KEY_TYPE = "typ";
    private static final String KEY_REZOLVE_ENTITY_ID = "rezolve_entity_id";
    private static final String KEY_PARTNER_ENTITY_ID = "partner_entity_id";
    private static final String KEY_EXPIRATION_TIME = "exp";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String TOKEN_PREFIX_BEARER = "Bearer ";

    private static final int EXPIRATION_TIME_MIN = 30;

    public static String createRegistrationToken() {
        Map<String, Object> header = new HashMap<String, Object>() {{
            put(KEY_ALG, "HS512");
            put(KEY_TYPE, "JWT");
        }};

        long currentTime = DateUtils.getCurrentTimestampInSeconds();
        long expirationTime = DateUtils.addMinutesToTimestamp(currentTime, EXPIRATION_TIME_MIN);

        String userIdentifier = DeviceUtils.generateUserIdentifier();

        Map<String, Object> claims = new HashMap<String, Object>() {{
            put(KEY_REZOLVE_ENTITY_ID, ":NONE:");
            put(KEY_PARTNER_ENTITY_ID, userIdentifier);
            put(KEY_EXPIRATION_TIME, expirationTime);
        }};

        return TOKEN_PREFIX_BEARER + createSignedToken(header, claims);
    }

    public static String createAccessToken(String entityId, String partnerId, String deviceId) {
        Map<String, Object> header = new HashMap<String, Object>() {{
            put(KEY_AUTH, "v2");
            put(KEY_ALG, "HS512");
            put(KEY_TYPE, "JWT");
        }};

        long currentTime = DateUtils.getCurrentTimestampInSeconds();
        long expirationTime = DateUtils.addMinutesToTimestamp(currentTime, EXPIRATION_TIME_MIN);

        Map<String, Object> claims = new HashMap<String, Object>() {{
            put(KEY_REZOLVE_ENTITY_ID, entityId);
            put(KEY_PARTNER_ENTITY_ID, partnerId);
            put(KEY_EXPIRATION_TIME, expirationTime);
            put(KEY_DEVICE_ID, deviceId);
        }};

        return TokenUtils.createSignedToken(header, claims);
    }

    private static String createSignedToken(Map<String, Object> header, Map<String, Object> claims) {
        SecretKey secretKey = Keys.hmacShaKeyFor(BuildConfig.REZOLVE_SDK_JWT_SECRET.getBytes());

        return Jwts.builder()
                .setHeader(header)
                .setClaims(claims)
                .signWith(secretKey)
                .compact();
    }
}
