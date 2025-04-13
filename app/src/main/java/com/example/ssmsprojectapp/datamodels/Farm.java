package com.example.ssmsprojectapp.datamodels;

import java.util.List;

public class Farm {
    private String owrner;
    private long latitude;
    private long longitude;
    private String SoilType;
    private String metals;
    private List<Measurements> measurements;

    public Farm(String owrner, long latitude, long longitude, String soilType, String metals, List<Measurements> measurements) {
        this.owrner = owrner;
        this.latitude = latitude;
        this.longitude = longitude;
        SoilType = soilType;
        this.metals = metals;
        this.measurements = measurements;
    }

    public String getOwrner() {
        return owrner;
    }

    public void setOwrner(String owrner) {
        this.owrner = owrner;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public String getSoilType() {
        return SoilType;
    }

    public void setSoilType(String soilType) {
        SoilType = soilType;
    }

    public String getMetals() {
        return metals;
    }

    public void setMetals(String metals) {
        this.metals = metals;
    }

    public List<Measurements> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurements> measurements) {
        this.measurements = measurements;
    }
}
