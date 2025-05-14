package com.example.ssmsprojectapp.recommendations;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://hehemu-4.onrender.com"; // Replace with your server URL
    private static Retrofit retrofit = null;

    public static AgriApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(AgriApiService.class);
    }
}
