package com.example.ssmsprojectapp.datamodels;

public class Measurements {
    private String farm;
    private double salinity;

    private double moisture;
    private double temperature;
    private double ph;
    private double nitrogen;
    private double phosphorous;
    private double potassium;
    private String metals;

    public Measurements(String farm, double salinity, double moisture, double temperature, double ph, double nitrogen, double phosphorous, double potassium, String metals) {
        this.farm = farm;
        this.salinity = salinity;
        this.moisture = moisture;
        this.temperature = temperature;
        this.ph = ph;
        this.nitrogen = nitrogen;
        this.phosphorous = phosphorous;
        this.potassium = potassium;
        this.metals = metals;
    }

    public String getFarm() {
        return farm;
    }

    public void setFarm(String farm) {
        this.farm = farm;
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

    public double getPhosphorous() {
        return phosphorous;
    }

    public void setPhosphorous(double phosphorous) {
        this.phosphorous = phosphorous;
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
}
