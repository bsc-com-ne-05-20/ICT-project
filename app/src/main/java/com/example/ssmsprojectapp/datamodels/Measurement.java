package com.example.ssmsprojectapp.datamodels;

import java.util.Date;

public class Measurement {
    private String id;  // Firestore document ID
    private String farmId;  // Reference to farm
    private double salinity;
    private double moisture;
    private double temperature;
    private double ph;
    private double nitrogen;
    private double phosphorus;  // Fixed spelling
    private double potassium;
    private String metals;
    private Date timestamp;  // Added timestamp

    public Measurement() {}  // Required for Firestore

    public Measurement(String id, String farmId, double salinity, double moisture,
                       double temperature, double ph, double nitrogen,
                       double phosphorus, double potassium, String metals) {
        this.id = id;
        this.farmId = farmId;
        this.salinity = salinity;
        this.moisture = moisture;
        this.temperature = temperature;
        this.ph = ph;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.metals = metals;
        this.timestamp = new Date();  // Auto-set current time
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFarmId() {
        return farmId;
    }

    public void setFarmId(String farmId) {
        this.farmId = farmId;
    }

    public double getSalinity() {
        return salinity;
    }

    public void setSalinity(double salinity) {
        this.salinity = salinity;
    }

    public double getMoisture() {
        return moisture;
    }

    public void setMoisture(double moisture) {
        this.moisture = moisture;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPh() {
        return ph;
    }

    public void setPh(double ph) {
        this.ph = ph;
    }

    public double getNitrogen() {
        return nitrogen;
    }

    public void setNitrogen(double nitrogen) {
        this.nitrogen = nitrogen;
    }

    public double getPhosphorus() {
        return phosphorus;
    }

    public void setPhosphorus(double phosphorus) {
        this.phosphorus = phosphorus;
    }

    public double getPotassium() {
        return potassium;
    }

    public void setPotassium(double potassium) {
        this.potassium = potassium;
    }

    public String getMetals() {
        return metals;
    }

    public void setMetals(String metals) {
        this.metals = metals;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
