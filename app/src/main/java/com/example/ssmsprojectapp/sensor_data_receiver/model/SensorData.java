package com.example.ssmsprojectapp.sensor_data_receiver.model;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorData {
    public int moisture;
    public float temperature;
    public float ec;
    public float ph;
    public int nitrogen;
    public int phosphorus;
    public int potassium;

    public static SensorData fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
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
