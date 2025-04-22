package com.example.ssmsprojectapp.datamodels;

public class Farmer {
    private String id;  // Firestore document ID
    private String name;
    private String email;
    private String phone;
    private String location;
    private String cooperativeId;  // Reference to cooperative instead of name

    // No longer storing farms list directly - we'll query them
    public Farmer(){

    }
    public Farmer(String id, String name, String email, String phone, String location, String cooperativeId) {}  // Required for Firestore

    public Farmer(String name, String email, String phone,
                  String location, String cooperativeId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.location = location;
        this.cooperativeId = cooperativeId;
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

    public String getCooperativeId() {
        return cooperativeId;
    }

    public void setCooperativeId(String cooperativeId) {
        this.cooperativeId = cooperativeId;
    }
}
