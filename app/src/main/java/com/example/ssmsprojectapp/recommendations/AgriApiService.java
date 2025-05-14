package com.example.ssmsprojectapp.recommendations;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AgriApiService {
    @POST("recommend") // Replace with your Flask endpoint
    Call<RecommendationResponse> getRecommendations(@Body RecommendationRequest request);
}
