package com.rezolve.shared.authentication;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This sample code comes configured to use a Rezolve-hosted authentication server, referred to by Rezolve as a RUA server (Rezolve User Authentication).
 * You SHOULD NOT use this server for production apps, it is for testing and Sandbox use only.
 * This sample auth configuration is provided so that:
 * 1) you may compile and test the sample code immediately upon receipt, without having to configure your own auth server, and
 * 2) so that the partner developer may see an example of how the SDK will utilize an external auth server to obtain permission to talk with the Rezolve APIs.
 * If you have an existing app with an existing authenticating user base, you will want to utilize YOUR auth server to issue JWT tokens, which the Rezolve API will accept.
 * Details on this process are available <a href="https://github.com/rezolved/rezolve_sdk_sampleapp_android/wiki/JWT-Authentication">here</a>.
 * If you do not have an existing app, or do not have an existing app server, you have the option
 * to either implement your own auth server and use JWT authentication as described above,
 * or to have Rezolve install a RUA server for you (the same type auth server this sample code is configured to use).
 * Please discuss authentication options with your project lead and/or your Rezolve representative.
 */
public class AuthenticationService {

    private final String KEY_DEVICE_ID = "deviceId";
    private final String KEY_EMAIL = "username";
    private final String KEY_PASSWORD = "password";
    private final String KEY_HEADER_TOKEN = "authorization";

    private AuthenticationRequest authenticationRequest;

    private String lastToken;
    private String entityId;

    private String apiKey;

    public void init(String authServer, String apiKey) {
        this.apiKey = apiKey;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(authServer)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        authenticationRequest = retrofit.create(AuthenticationRequest.class);
    }

    @SuppressLint("CheckResult")
    public void login(String email, String password, String deviceId, AuthenticationCallback callback) {
        Map<String, Object> body = new HashMap<String, Object>() {{
            put(KEY_EMAIL, email);
            put(KEY_PASSWORD, password);
            put(KEY_DEVICE_ID, deviceId);
        }};

        authenticationRequest.loginUser(apiKey, body)
                .map(response -> {
                    if(response.isSuccessful()) {
                        String token = response.headers().get(KEY_HEADER_TOKEN);
                        if (response.body() != null) {
                            response.body().setToken(token);
                        }
                        lastToken = token;
                        entityId = response.body().getEntityId();
                    }
                    return response.body();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> callback.onLoginSuccess(response),
                        error -> callback.onLoginFailure(error.getMessage()));
    }

    public String ping() {
        final Response<PingResponse> response = authenticationRequest.ping(lastToken).blockingFirst();
        if(response.isSuccessful()) {
            final PingResponse pingResponse = response.body();
            return pingResponse != null ? pingResponse.getAccessToken() : "";
        }
        return "";
    }

    public String getEntityId() {
        return entityId;
    }
}
