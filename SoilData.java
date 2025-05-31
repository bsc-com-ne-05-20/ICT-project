package com.example.soilhealthy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SoilData {
    private int id;
    private String timestamp;
    private float temperature;
    private float salinity;
    private float ph;
    private float moisture;
    private float nitrogen;
    private float phosphorus;
    private float potassium;
    private String personId;
    private boolean syncedWithFirebase;

    // Default constructor (required for Firebase)
    public SoilData() {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        this.syncedWithFirebase = false;
    }

    // Constructor with all parameters
    public SoilData(float temperature, float salinity, float ph, float moisture,
                    float nitrogen, float phosphorus, float potassium, String personId) {
        this();
        this.temperature = temperature;
        this.salinity = salinity;
        this.ph = ph;
        this.moisture = moisture;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.personId = personId;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public float getSalinity() { return salinity; }
    public void setSalinity(float salinity) { this.salinity = salinity; }

    public float getPh() { return ph; }
    public void setPh(float ph) { this.ph = ph; }

    public float getMoisture() { return moisture; }
    public void setMoisture(float moisture) { this.moisture = moisture; }

    public float getNitrogen() { return nitrogen; }
    public void setNitrogen(float nitrogen) { this.nitrogen = nitrogen; }

    public float getPhosphorus() { return phosphorus; }
    public void setPhosphorus(float phosphorus) { this.phosphorus = phosphorus; }

    public float getPotassium() { return potassium; }
    public void setPotassium(float potassium) { this.potassium = potassium; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public boolean isSyncedWithFirebase() { return syncedWithFirebase; }
    public void setSyncedWithFirebase(boolean synced) { this.syncedWithFirebase = synced; }

    // Helper method to get NPK string
    public String getNPK() {
        return nitrogen + "-" + phosphorus + "-" + potassium;
    }

    // Helper method to update timestamp
    public void updateTimestamp() {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}