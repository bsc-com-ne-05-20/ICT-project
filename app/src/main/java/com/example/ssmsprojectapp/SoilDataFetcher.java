package com.example.ssmsprojectapp;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoilDataFetcher {

    // API Configuration
    private static final String BASE_URL = "https://api.isda-africa.com/v1/soilproperty";
    private static final String API_KEY = "AIzaSyCruMPt43aekqITCooCNWGombhbcor3cf4";
    private static final int TIMEOUT_MS = 5000;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface SoilDataCallback {
        void onSuccess(Double value);  // Changed to return Double directly
        void onFailure(String error);
    }

    // Main fetch method for soil properties
    public static void fetchSoilProperties(double latitude, double longitude,
                                           String property, String depthLayer,
                                           SoilDataCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                // 1. Validate coordinates
                if (!isValidCoordinate(latitude, longitude)) {
                    throw new IllegalArgumentException("Invalid coordinates");
                }

                // 2. Build secure API URL with API key and parameters
                String apiUrl = String.format("%s?key=%s&lat=%.6f&lon=%.6f&property=%s&depth=%s",
                        BASE_URL,
                        URLEncoder.encode(API_KEY, "UTF-8"),
                        latitude,
                        longitude,
                        URLEncoder.encode(property, "UTF-8"),
                        URLEncoder.encode(depthLayer, "UTF-8"));
                URL url = new URL(apiUrl);

                // 3. Configure HTTP connection
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);

                // 4. Check response code
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 5. Parse successful response
                    try (InputStream is = conn.getInputStream()) {
                        String jsonString = streamToString(is);
                        Double value = parseSoilPropertyValue(jsonString, property);
                        callback.onSuccess(value);
                    }
                } else {
                    String errorMsg = readErrorStream(conn);
                    throw new IOException("API Error " + responseCode + ": " + errorMsg);
                }

            } catch (Exception e) {
                Log.e("ISDA_FETCHER", "Fetch failed: " + e.getMessage(), e);
                callback.onFailure(cleanErrorMessage(e));
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    // Helper: Validate coordinates
    private static boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    // Helper: Convert InputStream to String
    private static String streamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    // Helper: Read error stream
    private static String readErrorStream(HttpURLConnection conn) {
        try (InputStream es = conn.getErrorStream()) {
            if (es != null) {
                return streamToString(es);
            }
        } catch (IOException e) {
            Log.w("ISDA_FETCHER", "Failed to read error stream", e);
        }
        return "No error details";
    }

    // Updated Helper: Parse JSON response and extract just the numeric value
    private static Double parseSoilPropertyValue(String jsonString, String property) throws JSONException {
        try {
            JSONObject json = new JSONObject(jsonString);

            // Navigate the nested JSON structure
            JSONObject propertyObj = json.getJSONObject("property");
            JSONArray propertyArray = propertyObj.getJSONArray(property);

            if (propertyArray.length() > 0) {
                JSONObject firstItem = propertyArray.getJSONObject(0);
                JSONObject valueObj = firstItem.getJSONObject("value");
                return valueObj.getDouble("value");
            }
            return Double.NaN;
        } catch (JSONException e) {
            Log.e("JSON_PARSE", "Error parsing JSON for property: " + property, e);
            return Double.NaN;
        }
    }

    // Helper: Clean error messages for UI
    private static String cleanErrorMessage(Exception e) {
        if (e instanceof IOException) {
            return "Network error. Please check your connection.";
        } else if (e instanceof JSONException) {
            return "Data format error. Please try again later.";
        } else if (e.getMessage() != null && e.getMessage().contains("403")) {
            return "Authentication failed. Please check API configuration.";
        }
        return "An error occurred: " + e.getMessage();
    }

    // Clean up executor when done
    public static void shutdown() {
        executor.shutdown();
    }
}
