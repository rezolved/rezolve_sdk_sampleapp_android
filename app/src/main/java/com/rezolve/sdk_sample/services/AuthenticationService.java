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

    private final String KEY_REGISTRATION_EMAIL = "email";
    private final String EXAMPLE_EMAIL_SUFFIX = "@example.com";

    private final AuthenticationRequest authenticationRequest;

    public AuthenticationService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.REZOLVE_SDK_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        authenticationRequest = retrofit.create(AuthenticationRequest.class);
    }

    public void register(AuthenticationCallback authenticationCallback) {
        String token = TokenUtils.createRegistrationToken();
        String userEmail = DeviceUtils.userIdentifier + EXAMPLE_EMAIL_SUFFIX;

        Map<String, Object> body = new HashMap<String, Object>() {{
            put(KEY_REGISTRATION_EMAIL, userEmail);
        }};

        authenticationRequest.registerUser(token, BuildConfig.REZOLVE_SDK_API_KEY, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> authenticationCallback.onRegistrationSuccess(response),
                           error -> authenticationCallback.onRegistrationFailure(error.getMessage()));
    }
}
