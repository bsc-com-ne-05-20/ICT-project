package com.example.ssmsprojectapp.weather;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("main")
    private Main main;
    @SerializedName("weather")
    private Weather[] weather;
    @SerializedName("name")
    private String cityName;
    @SerializedName("dt")
    private long timestamp;
    @SerializedName("sys")
    private Sys sys;
    @SerializedName("wind")
    private Wind wind;

    public Main getMain() { return main; }
    public Weather[] getWeather() { return weather; }
    public String getCityName() { return cityName; }
    public long getTimestamp() { return timestamp; }
    public Sys getSys() { return sys; }
    public Wind getWind() { return wind; }

    public static class Main {
        @SerializedName("temp")
        private double temp;
        @SerializedName("feels_like")
        private double feelsLike;
        @SerializedName("temp_min")
        private double tempMin;
        @SerializedName("temp_max")
        private double tempMax;
        @SerializedName("pressure")
        private int pressure;
        @SerializedName("humidity")
        private int humidity;

        public double getTemp() { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public double getTempMin() { return tempMin; }
        public double getTempMax() { return tempMax; }
        public int getPressure() { return pressure; }
        public int getHumidity() { return humidity; }
    }

    public static class Weather {
        @SerializedName("id")
        private int id;
        @SerializedName("main")
        private String main;
        @SerializedName("description")
        private String description;
        @SerializedName("icon")
        private String icon;

        public int getId() { return id; }
        public String getMain() { return main; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }

    public static class Sys {
        @SerializedName("country")
        private String country;
        @SerializedName("sunrise")
        private long sunrise;
        @SerializedName("sunset")
        private long sunset;

        public String getCountry() { return country; }
        public long getSunrise() { return sunrise; }
        public long getSunset() { return sunset; }
    }

    public static class Wind {
        @SerializedName("speed")
        private double speed;
        @SerializedName("deg")
        private int deg;

        public double getSpeed() { return speed; }
        public int getDeg() { return deg; }
    }
}
