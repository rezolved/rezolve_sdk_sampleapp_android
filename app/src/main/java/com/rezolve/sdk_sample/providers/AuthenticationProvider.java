package com.rezolve.sdk_sample.providers;

import com.rezolve.sdk_sample.model.RegistrationResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthenticationProvider {
    @Headers("Content-Type: application/json")
    @POST("/api/v1/authentication/register")
    Observable<RegistrationResponse> registerUser(@Header("Authorization") String token,
                                                  @Header("x-rezolve-partner-apikey") String apiKey,
                                                  @Body Map<String, Object> body);
}
