package com.example.ssmsprojectapp.datamodels;

import java.util.List;

public class Farm {
    private String id;  // Firestore document ID
    private String farmerId;  // Reference to owner
    private double latitude;  // Changed from long to double
    private double longitude;
    private String soilType;
    private String metals;

    // No longer storing measurements list directly - we'll query them
    public Farm() {}  // Required for Firestore

    public Farm(String id, String farmerId, double latitude, double longitude,
                String soilType, String metals) {
        this.id = id;
        this.farmerId = farmerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.soilType = soilType;
        this.metals = metals;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public String getMetals() {
        return metals;
    }

    public void setMetals(String metals) {
        this.metals = metals;
    }
}
