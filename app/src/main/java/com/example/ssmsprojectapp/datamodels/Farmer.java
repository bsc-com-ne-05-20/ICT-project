package com.example.ssmsprojectapp.datamodels;

import java.util.List;

public class Farmer {
    private String name;
    private String email;
    private String phone;
    private String location;

    private List<Farm> farms;

    //constructor
    public Farmer(String name, String email, String phone, String location, List<Farm> farms) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.farms = farms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Farm> getFarms() {
        return farms;
    }

    public void setFarms(List<Farm> farms) {
        this.farms = farms;
    }

}
