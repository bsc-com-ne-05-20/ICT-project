package com.example.soilhealthy;

import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoilDataFetcher {

    // API Configuration
    private static final String BASE_URL = "https://api.isdaoil.org/v1/metals";
    private static final int TIMEOUT_MS = 5000;

    public interface SoilDataCallback {
        void onSuccess(Map<String, Double> metalData);
        void onFailure(String error);
    }

    // Main fetch method
    public static void fetchHeavyMetals(double latitude, double longitude, SoilDataCallback callback) {
        new Thread(() -> {
            try {
                // 1. Build API URL with coordinates
                String apiUrl = BASE_URL + "?lat=" + latitude + "&lon=" + longitude;
                URL url = new URL(apiUrl);

                // 2. Configure HTTP connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);

                // 3. Check response code
                if (conn.getResponseCode() == 200) {
                    // 4. Parse successful response
                    InputStream is = conn.getInputStream();
                    String jsonString = streamToString(is);
                    Map<String, Double> metalData = parseMetalData(jsonString);
                    callback.onSuccess(metalData);
                } else {
                    callback.onFailure("API Error: " + conn.getResponseCode());
                }
                conn.disconnect();

            } catch (Exception e) {
                Log.e("ISDA_FETCHER", "Fetch failed", e);
                callback.onFailure(e.getMessage());
            }
        }).start();
    }

    // Helper: Convert InputStream to String
    private static String streamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    // Helper: Parse JSON response
    private static Map<String, Double> parseMetalData(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);
        Map<String, Double> metals = new HashMap<>();

        // Extract heavy metals (adjust keys according to ISDA's actual response)
        metals.put("zinc", json.getDouble("zn_ppm"));
        metals.put("lead", json.getDouble("pb_ppm"));
        metals.put("cadmium", json.getDouble("cd_ppm"));
        metals.put("copper", json.getDouble("cu_ppm"));

        return metals;
    }
}
