package com.rezolve.sdk_sample.services;

import com.rezolve.sdk_sample.BuildConfig;
import com.rezolve.sdk_sample.providers.AuthenticationProvider;
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

    private final AuthenticationProvider mAuthenticationProvider;

    public AuthenticationService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.REZOLVE_SDK_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        mAuthenticationProvider = retrofit.create(AuthenticationProvider.class);
    }

    public void register(AuthenticationInterface authenticationInterface) {
        String token = TokenUtils.createRegistrationToken();
        String userEmail = DeviceUtils.userIdentifier + EXAMPLE_EMAIL_SUFFIX;

        Map<String, Object> body = new HashMap<String, Object>() {{
            put(KEY_REGISTRATION_EMAIL, userEmail);
        }};

        mAuthenticationProvider.registerUser(token, BuildConfig.REZOLVE_SDK_API_KEY, body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> authenticationInterface.onRegistrationSuccess(response),
                           error -> authenticationInterface.onRegistrationFailure());
    }
}
