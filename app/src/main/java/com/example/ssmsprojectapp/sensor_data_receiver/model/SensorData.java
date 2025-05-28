package com.example.ssmsprojectapp.sensor_data_receiver.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model class representing sensor data received from the ESP32.
 * It parses a JSON string and stores values like moisture, temperature, etc.
 */
public class SensorData {
    // Sensor readings
    public int moisture;
    public float temperature;
    public float ec; // Electrical Conductivity
    public float ph;
    public int nitrogen;
    public int phosphorus;
    public int potassium;

    /**
     * Parses a JSON string to extract sensor values and create a SensorData object.
     *
     * Expected JSON format:
     * {
     *   "moisture": 45,
     *   "temperature": 26.5,
     *   "ec": 1.2,
     *   "ph": 6.8,
     *   "nitrogen": 30,
     *   "phosphorus": 15,
     *   "potassium": 20
     * }
     *
     * @param jsonString The JSON string containing sensor values.
     * @return A SensorData object populated with the parsed data.
     * @throws JSONException If parsing fails or keys are missing.
     */
    public static SensorData fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);

        // Create a new SensorData object and fill its fields
        SensorData data = new SensorData();
        data.moisture = json.getInt("moisture");
        data.temperature = (float) json.getDouble("temperature");
        data.ec = (float) json.getDouble("ec");
        data.ph = (float) json.getDouble("ph");
        data.nitrogen = json.getInt("nitrogen");
        data.phosphorus = json.getInt("phosphorus");
        data.potassium = json.getInt("potassium");

        return data;
    }
}
