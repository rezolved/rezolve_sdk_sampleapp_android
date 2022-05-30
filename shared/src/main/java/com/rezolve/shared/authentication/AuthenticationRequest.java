package com.rezolve.shared.authentication;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthenticationRequest {
    @Headers("Content-Type: application/json")
    @POST("/v2/credentials/login")
    Observable<Response<AuthenticationResponse>> loginUser(@Header("x-rezolve-partner-apikey") String apiKey,
                                                          @Body Map<String, Object> body);

    @Headers("Content-Type: application/json")
    @GET("/v2/credentials/ping")
    Observable<Response<PingResponse>> ping(@Header("authorization") String authorization);
}
