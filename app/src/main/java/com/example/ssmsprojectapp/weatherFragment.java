package com.example.ssmsprojectapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ssmsprojectapp.weather.ForecastResponse;
import com.example.ssmsprojectapp.weather.WeatherRepository;
import com.example.ssmsprojectapp.weather.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class weatherFragment extends Fragment {

    private static final String ICON_URL = "https://openweathermap.org/img/wn/%s@4x.png";
    private TextView tvCity, tvTemp, tvDescription, tvHumidity, tvWind, tvSunrise, tvSunset;
    private ImageView ivWeatherIcon;
    private SwipeRefreshLayout swipeRefreshLayout;

    private WeatherRepository weatherRepository;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    public weatherFragment() {
        // Required empty public constructor
    }

    public weatherFragment(double lat,double lon){
        this.currentLatitude = lat;
        this.currentLongitude = lon;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        initViews(view);
        weatherRepository = new WeatherRepository();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentLatitude != 0.0 && currentLongitude != 0.0) {
                fetchWeatherData(currentLatitude, currentLongitude);
            } else {
                Toast.makeText(getContext(), "coordinates cannot be 0.0,0.0", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        return  view;
    }


    private void initViews(View v) {
        tvCity = v.findViewById(R.id.tvCity);
        tvTemp = v.findViewById(R.id.tvTemp);
        tvDescription = v.findViewById(R.id.tvDescription);
        tvHumidity = v.findViewById(R.id.tvHumidity);
        tvWind = v.findViewById(R.id.tvWind);
        tvSunrise = v.findViewById(R.id.tvSunrise);
        tvSunset = v.findViewById(R.id.tvSunset);
        ivWeatherIcon = v.findViewById(R.id.ivWeatherIcon);
        swipeRefreshLayout = v.findViewById(R.id.swiperefreshlayout);

    }

    private void fetchWeatherData(double lat, double lon) {
        swipeRefreshLayout.setRefreshing(true);
        weatherRepository.getCurrentWeather(lat, lon, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherResponse response) {
                requireActivity().runOnUiThread(() -> {
                    updateUI(response);
                    fetchForecastData(lat, lon);
                });
            }

            @Override
            public void onFailure(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }


    private void fetchForecastData(double lat, double lon) {
        weatherRepository.getWeatherForecast(lat, lon, new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResponse response) {
                requireActivity().runOnUiThread(() -> {
                    // Here you can update forecast UI if needed
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onFailure(String message) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Forecast: " + message, Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });
    }

    private void updateUI(WeatherResponse weather) {
        if (weather.getCityName() != null && weather.getSys() != null) {
            tvCity.setText(String.format("%s, %s", weather.getCityName(), weather.getSys().getCountry()));
        } else {
            tvCity.setText("Unknown Location");
        }

        if (weather.getMain() != null) {
            tvTemp.setText(String.format(Locale.getDefault(), "%.1f°C", weather.getMain().getTemp()));
            tvHumidity.setText(String.format(Locale.getDefault(), "Humidity: %d%%", weather.getMain().getHumidity()));
        }

        if (weather.getWeather() != null && weather.getWeather().length > 0) {
            tvDescription.setText(weather.getWeather()[0].getDescription());
            String iconUrl = String.format(ICON_URL, weather.getWeather()[0].getIcon());
            Glide.with(this).load(iconUrl).into(ivWeatherIcon);
        }

        if (weather.getWind() != null) {
            tvWind.setText(String.format(Locale.getDefault(), "Wind: %.1f m/s", weather.getWind().getSpeed()));
        }

        if (weather.getSys() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            tvSunrise.setText(timeFormat.format(new Date(weather.getSys().getSunrise() * 1000)));
            tvSunset.setText(timeFormat.format(new Date(weather.getSys().getSunset() * 1000)));
        }
    }
}