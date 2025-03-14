package com.example.soilhealthy;

import java.util.ArrayList;
import java.util.List;

public class DataProcessor {

    // Lists to store logged data
    private List<Float> pHValues = new ArrayList<>();
    private List<Float> metalsValues = new ArrayList<>();
    private List<Float> salinityValues = new ArrayList<>();

    // Add new data to the lists
    public void addData(float pH, float metals, float salinity) {
        pHValues.add(pH);
        metalsValues.add(metals);
        salinityValues.add(salinity);
    }

    // Calculate average for pH
    public float getAveragepH() {
        return calculateAverage(pHValues);
    }

    // Calculate average for heavy metals
    public float getAverageMetals() {
        return calculateAverage(metalsValues);
    }

    // Calculate average for salinity
    public float getAverageSalinity() {
        return calculateAverage(salinityValues);
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

    // Clear all logged data
    public void clearData() {
        pHValues.clear();
        metalsValues.clear();
        salinityValues.clear();
    }
}