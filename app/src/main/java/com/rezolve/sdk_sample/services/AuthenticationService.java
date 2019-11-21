package com.rezolve.sdk_sample.services;

import com.rezolve.sdk_sample.BuildConfig;
import com.rezolve.sdk_sample.api.AuthenticationRequest;
import com.rezolve.sdk_sample.services.callbacks.AuthenticationCallback;
import com.rezolve.sdk_sample.utils.DeviceUtils;
import com.rezolve.sdk_sample.utils.TokenUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthenticationService {

    private final String KEY_DEVICE_ID = "deviceId";
    private final String KEY_EMAIL = "email";
    private final String KEY_PASSWORD = "password";
    private final String KEY_HEADER_TOKEN = "authentication";

    private final AuthenticationRequest authenticationRequest;

    public AuthenticationService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.DEMO_AUTH_SERVER)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        authenticationRequest = retrofit.create(AuthenticationRequest.class);
    }

    public void register(String email, String password, AuthenticationCallback authenticationCallback) {
        Map<String, Object> body = new HashMap<String, Object>() {{
            put(KEY_EMAIL, email);
            put(KEY_PASSWORD, password);
            put("phone", "+48791668815");
        }};

        authenticationRequest.registerUser(BuildConfig.REZOLVE_SDK_API_KEY, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> authenticationCallback.onLoginSuccess(response),
                        error -> authenticationCallback.onLoginFailure(error.getMessage()));
    }

    public void login(String email, String password, String deviceId, AuthenticationCallback callback) {
        Map<String, Object> body = new HashMap<String, Object>() {{
            put(KEY_EMAIL, email);
            put(KEY_PASSWORD, password);
            put(KEY_DEVICE_ID, deviceId);
        }};

        authenticationRequest.loginUser(BuildConfig.REZOLVE_SDK_API_KEY, body)
                .map(response -> {
                    String token = response.headers().get(KEY_HEADER_TOKEN);
                    response.body().setToken(token);
                    return response.body();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> callback.onLoginSuccess(response),
                        error -> callback.onLoginFailure(error.getMessage()));
    }
}
