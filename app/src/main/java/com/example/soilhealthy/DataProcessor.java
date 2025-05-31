package com.example.soilhealthy;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {
    // Lists to store logged data
    private final List<Float> temperatureReadings = new ArrayList<>();
    private final List<Float> salinityReadings = new ArrayList<>();
    private final List<Float> phReadings = new ArrayList<>();
    private final List<Float> moistureReadings = new ArrayList<>();
    private final List<String> npkReadings = new ArrayList<>();

    // Add new data to the lists
    public void addData(float temperature, float salinity, float ph, float moisture, String npk) {
        temperatureReadings.add(temperature);
        salinityReadings.add(salinity);
        phReadings.add(ph);
        moistureReadings.add(moisture);
        npkReadings.add(npk);
    }

    // Calculate average for temperature
    public float getAverageTemperature() {
        return calculateAverage(temperatureReadings);
    }

    // Calculate average for salinity
    public float getAverageSalinity() {
        return calculateAverage(salinityReadings);
    }

    // Calculate average for pH
    public float getAveragepH() {
        return calculateAverage(phReadings);
    }

    // Calculate average for moisture
    public float getAverageMoisture() {
        return calculateAverage(moistureReadings);
    }

    // Calculate average for NPK (as a string)
    public String getAverageNPK() {
        if (npkReadings.isEmpty()) {
            return "0-0-0"; // Return default if no data is available
        }

        // Split NPK values into N, P, K components
        float totalN = 0, totalP = 0, totalK = 0;
        for (String npk : npkReadings) {
            String[] parts = npk.split("-");
            if (parts.length == 3) {
                try {
                    totalN += Float.parseFloat(parts[0]);
                    totalP += Float.parseFloat(parts[1]);
                    totalK += Float.parseFloat(parts[2]);
                } catch (NumberFormatException e) {
                    // Handle invalid NPK format
                }
            }
        }

        // Calculate averages
        float avgN = totalN / npkReadings.size();
        float avgP = totalP / npkReadings.size();
        float avgK = totalK / npkReadings.size();

        return String.format("%.1f-%.1f-%.1f", avgN, avgP, avgK);
    }

    // Helper method to calculate average of a list
    private float calculateAverage(List<Float> values) {
        if (values.isEmpty()) {
            return 0; // Return 0 if no data is available
        }
        float sum = 0;
        for (float value : values) {
            sum += value;
        }
        return sum / values.size();
    }

    // Check if any data exists
    public boolean hasData() {
        return !temperatureReadings.isEmpty() ||
                !salinityReadings.isEmpty() ||
                !phReadings.isEmpty() ||
                !moistureReadings.isEmpty() ||
                !npkReadings.isEmpty();
    }

    // Clear all logged data
    public void clearData() {
        temperatureReadings.clear();
        salinityReadings.clear();
        phReadings.clear();
        moistureReadings.clear();
        npkReadings.clear();
    }

    // Getter methods for individual readings (optional)
    public List<Float> getTemperatureReadings() {
        return temperatureReadings;
    }

    public List<Float> getSalinityReadings() {
        return salinityReadings;
    }

    public List<Float> getPhReadings() {
        return phReadings;
    }

    public List<Float> getMoistureReadings() {
        return moistureReadings;
    }

    public List<String> getNPKReadings() {
        return npkReadings;
    }
}