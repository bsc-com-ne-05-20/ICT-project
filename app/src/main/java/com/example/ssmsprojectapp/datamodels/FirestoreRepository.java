package com.example.ssmsprojectapp.datamodels;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreRepository {

    private FirebaseFirestore db;
    private static final String COOPERATIVES_COL = "cooperatives";
    private static final String FARMERS_COL = "farmers";
    private static final String FARMS_COL = "farms";
    private static final String MEASUREMENTS_COL = "measurements";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // 1. Get all farmers under a cooperative
    public void getFarmersByCooperative(String cooperativeId,
                                        OnCompleteListener<QuerySnapshot> listener) {
        db.collection(FARMERS_COL)
                .whereEqualTo("cooperativeId", cooperativeId)
                .get()
                .addOnCompleteListener(listener);
    }

    // 2. Get all farms for a farmer
    public void getFarmsByFarmer(String farmerId,
                                 OnCompleteListener<QuerySnapshot> listener) {
        db.collection(FARMS_COL)
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnCompleteListener(listener);
    }

    // 3. Get all measurements for a farm
    public void getMeasurementsByFarm(String farmId,
                                      OnCompleteListener<QuerySnapshot> listener) {
        db.collection(MEASUREMENTS_COL)
                .whereEqualTo("farmId", farmId)
                .orderBy("timestamp", Query.Direction.DESCENDING)  // Newest first
                .get()
                .addOnCompleteListener(listener);
    }

    // Helper method to convert snapshot to Farmer object
    public Farmer snapshotToFarmer(DocumentSnapshot snapshot) {
        return new Farmer(
                snapshot.getId(),
                snapshot.getString("name"),
                snapshot.getString("email"),
                snapshot.getString("phone"),
                snapshot.getString("location"),
                snapshot.getString("cooperativeId")
        );
    }

    public Farm snapshotToFarm(DocumentSnapshot snapshot) {
        return new Farm(
                snapshot.getId(),
                snapshot.getString("name"),
                snapshot.getDouble("latitude"),
                snapshot.getDouble("phone"),
                snapshot.getString("location"),
                snapshot.getString("cooperativeId")
        );
    }

    public Measurement snapshotToMeasurement(DocumentSnapshot snapshot) {
        return new Measurement(
                snapshot.getId(),
                snapshot.getString("farmID"),
                snapshot.getDouble("salinity"),
                snapshot.getDouble("moisture"),
                snapshot.getDouble("tenperature"),
                snapshot.getDouble("ph"),
                snapshot.getDouble("nitrogen"),
                snapshot.getDouble("phosphorus"),
                snapshot.getDouble("potassium"),
                snapshot.getString("metals")
        );
    }
}
