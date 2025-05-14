package com.example.ssmsprojectapp.recommendations;

public class RecommendationRequest {

    private SoilData soil_data;
    private WeatherData weather_data;

    public RecommendationRequest(SoilData soil_data, WeatherData weather_data) {
        this.soil_data = soil_data;
        this.weather_data = weather_data;
    }

    // Getters and setters
    public static class SoilData {
        private double ph;
        private double nitrogen;
        private double phosphorus;
        private double potassium;
        private double organic_matter;
        // Getters and setters


        public SoilData(double ph, double nitrogen, double phosphorus, double potassium, double organic_matter) {
            this.ph = ph;
            this.nitrogen = nitrogen;
            this.phosphorus = phosphorus;
            this.potassium = potassium;
            this.organic_matter = organic_matter;
        }
    }

    public static class WeatherData {
        private double temperature;
        private double rainfall;
        private double humidity;
        private String forecast;
        // Getters and setters


        public WeatherData(double temperature, double rainfall, double humidity, String forecast) {
            this.temperature = temperature;
            this.rainfall = rainfall;
            this.humidity = humidity;
            this.forecast = forecast;
        }
    }

}
