package com.example.soilhealthy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SoilDataActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TextView tvCoordinates, tvResults;
    private Button btnFetch;
    private ProgressBar progressBar;

    // Heavy metals to analyze
    private static final String[] HEAVY_METALS = {
            "zinc_extractable",
            "iron_extractable",
            "aluminium_extractable",
            "magnesium_extractable",
    };
    private static final String DEPTH_LAYER = "0-20"; // Standard depth layer

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soil_data);

        tvCoordinates = findViewById(R.id.tv_coordinates);
        tvResults = findViewById(R.id.tv_results);
        btnFetch = findViewById(R.id.btn_fetch);
        progressBar = findViewById(R.id.progress_bar);

        btnFetch.setOnClickListener(v -> {
            if (LocationHelper.checkLocationPermission(this)) {
                fetchData();
            } else {
                requestLocationPermission();
            }
        });
    }

    private void fetchData() {
        tvCoordinates.setText("Getting location...");
        tvResults.setText("");
        btnFetch.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                runOnUiThread(() -> {
                    tvCoordinates.setText(String.format(Locale.US,
                            "📍 Coordinates: %.6f, %.6f", latitude, longitude));
                });
                fetchHeavyMetals(latitude, longitude);
            }

            @Override
            public void onLocationFailed(String error) {
                runOnUiThread(() -> {
                    tvCoordinates.setText("Location Error");
                    tvResults.setText("Error: " + error + "\nTap to retry");
                    btnFetch.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(SoilDataActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchHeavyMetals(double lat, double lng) {
        tvResults.setText("Analyzing heavy metals...");

        // Use ConcurrentHashMap for thread safety
        Map<String, Double> allResults = new ConcurrentHashMap<>();
        AtomicInteger completedRequests = new AtomicInteger(0);

        for (String metal : HEAVY_METALS) {
            SoilDataFetcher.fetchSoilProperties(lat, lng, metal, DEPTH_LAYER,
                    new SoilDataFetcher.SoilDataCallback() {
                        @Override
                        public void onSuccess(Double value) {
                            allResults.put(metal, value);

                            if (completedRequests.incrementAndGet() == HEAVY_METALS.length) {
                                runOnUiThread(() -> {
                                    String result = formatMetalResults(allResults);
                                    tvResults.setText(result);
                                    btnFetch.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("FETCH_ERROR", "Failed to fetch " + metal + ": " + error);
                            allResults.put(metal, Double.NaN);

                            if (completedRequests.incrementAndGet() == HEAVY_METALS.length) {
                                runOnUiThread(() -> {
                                    String result = formatMetalResults(allResults);
                                    tvResults.setText(result);
                                    btnFetch.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        }
                    });
        }
    }

    private String formatMetalResults(Map<String, Double> metals) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚠️ Heavy Metal Analysis (Depth: ").append(DEPTH_LAYER).append("cm)\n\n");

        // Format each metal with proper units and safety info
        for (Map.Entry<String, Double> entry : metals.entrySet()) {
            String metal = entry.getKey().replace("_extractable", "").replace("_", " ");
            double value = entry.getValue();

            sb.append(String.format(Locale.US, "• %s: %.2f ppm %s\n",
                    capitalize(metal),
                    value,
                    getSafetyIndicator(metal, value)));
        }

        // Add safety interpretation
        sb.append("\n🚦 Safety Interpretation:\n");
        sb.append(interpretMetalResults(metals));

        return sb.toString();
    }

    private String getSafetyIndicator(String metal, double value) {
        // First check for NaN (failed measurement)
        if (Double.isNaN(value)) {
            return "❌(Error)";
        }

        // Remove "_extractable" for cleaner matching
        String baseMetal = metal.replace("_extractable", "");

        // These thresholds should be based on actual regulatory limits
        switch (baseMetal.toLowerCase()) {
            case "iron":
                return value > 100 ? "🚨(Unsafe)" : value > 50 ? "⚠️(Moderate)" : "✅(Safe)";
            case "aluminium":
                return value > 3 ? "🚨(Unsafe)" : value > 1 ? "⚠️(Moderate)" : "✅(Safe)";
            case "magnesium":
                return value > 20 ? "🚨(Unsafe)" : value > 10 ? "⚠️(Moderate)" : "✅(Safe)";
            case "zinc":
                return value > 500 ? "🚨(High)" : value > 200 ? "⚠️(Moderate)" : "✅(Normal)";
            default:
                return "�";
        }
    }

    private String interpretMetalResults(Map<String, Double> metals) {
        StringBuilder interpretation = new StringBuilder();
        boolean anyDanger = false;

        if (!Double.isNaN(metals.getOrDefault("iron_extractable", Double.NaN))
                && metals.get("iron_extractable") > 100) {
            interpretation.append("- High iron levels! May affect plant growth\n");
            anyDanger = true;
        }

        if (!Double.isNaN(metals.getOrDefault("aluminium_extractable", Double.NaN))
                && metals.get("aluminium_extractable") > 3) {
            interpretation.append("- Excessive aluminum! Toxic to plants\n");
            anyDanger = true;
        }

        if (!Double.isNaN(metals.getOrDefault("magnesium_extractable", Double.NaN))
                && metals.get("magnesium_extractable") > 20) {
            interpretation.append("- Magnesium levels above recommended\n");
            anyDanger = true;
        }

        if (!Double.isNaN(metals.getOrDefault("zinc_extractable", Double.NaN))
                && metals.get("zinc_extractable") > 500) {
            interpretation.append("- Zinc concentration potentially harmful\n");
            anyDanger = true;
        }

        if (!anyDanger) {
            interpretation.append("- All metals within safe limits for agriculture\n");
        } else {
            interpretation.append("\n❗ Consult soil experts for remediation advice");
        }

        return interpretation.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchData();
            } else {
                Toast.makeText(this,
                        "Location permission required for soil analysis",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}