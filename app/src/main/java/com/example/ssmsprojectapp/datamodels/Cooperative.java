package com.example.ssmsprojectapp.datamodels;

import java.util.List;

public class Cooperative {
    private String name;
    private String location;

    private String represetative;

    private String email;

    private String phone;
    private List<Farmer> farmers;

    public Cooperative(String name, String location, String represetative, String email, String phone, List<Farmer> farmers) {
        this.name = name;
        this.location = location;
        this.represetative = represetative;
        this.email = email;
        this.phone = phone;
        this.farmers = farmers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRepresetative() {
        return represetative;
    }

    public void setRepresetative(String represetative) {
        this.represetative = represetative;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<Farmer> getFarmers() {
        return farmers;
    }

    public void setFarmers(List<Farmer> farmers) {
        this.farmers = farmers;
    }
}
