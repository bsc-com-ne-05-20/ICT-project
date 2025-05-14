package com.example.ssmsprojectapp.weather;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static final String API_KEY = "69e70991270c0939e778112041f97d51";
    private static final String UNITS = "metric"; // For Celsius

    private WeatherApiService apiService;

    public WeatherRepository() {
        apiService = ApiClient.getApiService();
    }

    public void getCurrentWeather(double lat, double lon, WeatherCallback callback) {
        apiService.getCurrentWeather(lat, lon, API_KEY, UNITS).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to get weather data");
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public void getWeatherForecast(double lat, double lon, ForecastCallback callback) {
        apiService.getWeatherForecast(lat, lon, API_KEY, UNITS).enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to get forecast data");
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                callback.onFailure(t.getMessage());
            }
        });
    }

    public interface WeatherCallback {
        void onSuccess(WeatherResponse response);
        void onFailure(String message);
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResponse response);
        void onFailure(String message);
    }
}
