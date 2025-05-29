package com.example.ssmsprojectapp.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Measurement implements Parcelable {
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
        this.timestamp = Timestamp.now().toDate();  // Auto-set current time
    }

    public Measurement(String id, String farmId, Double salinity, Double moisture, Double temperature, Double ph, Double nitrogen, Double phosphorus, Double potassium, String metals, Date timestamp) {
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
        this.timestamp = timestamp;
    }

    protected Measurement(Parcel in) {
        id = in.readString();
        farmId = in.readString();
        salinity = in.readDouble();
        moisture = in.readDouble();
        temperature = in.readDouble();
        ph = in.readDouble();
        nitrogen = in.readDouble();
        phosphorus = in.readDouble();
        potassium = in.readDouble();
        metals = in.readString();
        // Add timestamp reading
        long time = in.readLong();
        this.timestamp = time == -1 ? null : new Date(time);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(farmId);
        dest.writeDouble(salinity);
        dest.writeDouble(moisture);
        dest.writeDouble(temperature);
        dest.writeDouble(ph);
        dest.writeDouble(nitrogen);
        dest.writeDouble(phosphorus);
        dest.writeDouble(potassium);
        dest.writeString(metals);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Measurement> CREATOR = new Creator<Measurement>() {
        @Override
        public Measurement createFromParcel(Parcel in) {
            return new Measurement(in);
        }

        @Override
        public Measurement[] newArray(int size) {
            return new Measurement[size];
        }
    };

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
