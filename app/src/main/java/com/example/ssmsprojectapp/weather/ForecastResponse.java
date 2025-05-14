package com.example.ssmsprojectapp.weather;

import com.google.gson.annotations.SerializedName;

public class ForecastResponse {
    @SerializedName("list")
    private ForecastItem[] list;
    @SerializedName("city")
    private City city;

    public ForecastItem[] getList() { return list; }
    public City getCity() { return city; }

    public static class ForecastItem {
        @SerializedName("dt")
        private long timestamp;
        @SerializedName("main")
        private WeatherResponse.Main main;
        @SerializedName("weather")
        private WeatherResponse.Weather[] weather;
        @SerializedName("dt_txt")
        private String dateText;

        public long getTimestamp() { return timestamp; }
        public WeatherResponse.Main getMain() { return main; }
        public WeatherResponse.Weather[] getWeather() { return weather; }
        public String getDateText() { return dateText; }
    }

    public static class City {
        @SerializedName("name")
        private String name;
        @SerializedName("country")
        private String country;

        public String getName() { return name; }
        public String getCountry() { return country; }
    }
}
