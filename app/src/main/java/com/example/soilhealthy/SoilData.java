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
    private double latitude;
    private double longitude;
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
        this.latitude = 0.0;
        this.longitude = 0.0;
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

    // Constructor with location data
    public SoilData(float temperature, float salinity, float ph, float moisture,
                    float nitrogen, float phosphorus, float potassium, String personId,
                    double latitude, double longitude) {
        this(temperature, salinity, ph, moisture, nitrogen, phosphorus, potassium, personId);
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // Helper method to get NPK string
    public String getNPK() {
        return String.format(Locale.getDefault(), "%.1f-%.1f-%.1f", nitrogen, phosphorus, potassium);
    }

    // Helper method to update timestamp
    public void updateTimestamp() {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    // Helper method to check if location data is available
    public boolean hasLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    @Override
    public String toString() {
        return "SoilData{" +
                "id=" + id +
                ", timestamp='" + timestamp + '\'' +
                ", temperature=" + temperature +
                ", salinity=" + salinity +
                ", ph=" + ph +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", moisture=" + moisture +
                ", nitrogen=" + nitrogen +
                ", phosphorus=" + phosphorus +
                ", potassium=" + potassium +
                ", personId='" + personId + '\'' +
                ", syncedWithFirebase=" + syncedWithFirebase +
                '}';
    }
}