package com.rezolve.sdk_sample.api;

import com.rezolve.sdk_sample.model.AuthenticationResponse;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthenticationRequest {
    @Headers("Content-Type: application/json")
    @POST("/v2/credentials/register")
    Observable<AuthenticationResponse> registerUser(@Header("x-rezolve-partner-apikey") String apiKey,
                                                    @Body Map<String, Object> body);

    @Headers("Content-Type: application/json")
    @POST("/v2/credentials/login")
    Observable<Response<AuthenticationResponse>> loginUser(@Header("x-rezolve-partner-apikey") String apiKey,
                                                          @Body Map<String, Object> body);


}
