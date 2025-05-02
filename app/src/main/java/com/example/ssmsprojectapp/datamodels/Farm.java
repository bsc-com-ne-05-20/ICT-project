package com.example.ssmsprojectapp.datamodels;

public class Farm {
    private String id;  // Firestore document ID
    private String farmerId;  // Reference to owner
    private double latitude;  // Changed from long to double
    private double longitude;
    private String soilType,farmName,farmSize,crops,location;




    // No longer storing measurements list directly - we'll query them
    public Farm(String id, String farmerId, Double latitude, Double longitude, String soilType, String metals, String farmName, String farmSize, String crops, String location) {

        this.id = id;
        this.farmerId = farmerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.soilType = soilType;
        this.farmName = farmName;
        this.farmSize = farmSize;
        this.crops = crops;
        this.location = location;
    }  // Required for Firestore



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

    public String getFarmName() {
        return farmName;
    }

    public void setFarmName(String farmName) {
        this.farmName = farmName;
    }

    public String getFarmSize() {
        return farmSize;
    }

    public void setFarmSize(String farmSize) {
        this.farmSize = farmSize;
    }

    public String getCrops() {
        return crops;
    }

    public void setCrops(String crops) {
        this.crops = crops;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
