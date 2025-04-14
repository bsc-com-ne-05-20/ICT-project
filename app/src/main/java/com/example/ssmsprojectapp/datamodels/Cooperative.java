package com.example.ssmsprojectapp.datamodels;

import java.util.List;

public class Cooperative {
    private String id;  // Firestore document ID
    private String name;
    private String location;
    private String representative;
    private String email;
    private String phone;

    // No longer storing farmers list directly - we'll query them
    public Cooperative() {}  // Required for Firestore

    public Cooperative(String id, String name, String location, String representative,
                       String email, String phone) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.representative = representative;
        this.email = email;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
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
}
