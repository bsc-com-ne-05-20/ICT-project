package com.example.soilhealthy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationHelper {

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationFailed(String error);
    }

    public static void getCurrentLocation(Context context, LocationCallback callback) {
        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        // Check permission
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationFailed("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(
                                location.getLatitude(),
                                location.getLongitude()
                        );
                    } else {
                        callback.onLocationFailed("Unable to get current location");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onLocationFailed("Location error: " + e.getMessage());
                });
    }

    public static boolean checkLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
