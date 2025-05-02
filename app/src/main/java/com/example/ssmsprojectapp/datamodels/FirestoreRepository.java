package com.example.ssmsprojectapp.datamodels;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String COOPERATIVES_COL = "cooperatives";
    private static final String FARMERS_COL = "farmers";
    private static final String FARMS_COL = "farms";
    private static final String MEASUREMENTS_COL = "measurements";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void registerFarmerWithAuth(String email, String password, Farmer farmer,
                                       OnCompleteListener<AuthResult> authListener,
                                       OnSuccessListener<DocumentReference> firestoreSuccess,
                                       OnFailureListener firestoreFailure) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authListener)
                .addOnSuccessListener(authResult -> {
                    // After auth success, create Firestore record
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(farmer.getName())
                            .build();

                    authResult.getUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                Map<String, Object> farmerData = new HashMap<>();
                                farmerData.put("name", farmer.getName());
                                farmerData.put("email", farmer.getEmail());
                                farmerData.put("phone", farmer.getPhone());
                                farmerData.put("location", farmer.getLocation());
                                //null for now
                                farmerData.put("cooperativeId", null);
                                farmerData.put("userId", authResult.getUser().getUid()); // Link auth UID

                                db.collection(FARMERS_COL)
                                        .add(farmerData)
                                        .addOnSuccessListener(firestoreSuccess)
                                        .addOnFailureListener(firestoreFailure);
                            });
                });
    }

    // Register cooperative with authentication
    public void registerCooperativeWithAuth(String email, String password, Cooperative cooperative,
                                            OnCompleteListener<AuthResult> authListener,
                                            OnSuccessListener<DocumentReference> firestoreSuccess,
                                            OnFailureListener firestoreFailure) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authListener)
                .addOnSuccessListener(authResult -> {
                    // After auth success, create Firestore record
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(cooperative.getName())
                            .build();

                    authResult.getUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(task -> {
                                Map<String, Object> coopData = new HashMap<>();
                                coopData.put("name", cooperative.getName());
                                coopData.put("location", cooperative.getLocation());
                                coopData.put("representative", cooperative.getRepresentative());
                                coopData.put("email", cooperative.getEmail());
                                coopData.put("phone", cooperative.getPhone());
                                coopData.put("userId", authResult.getUser().getUid()); // Link auth UID

                                db.collection(COOPERATIVES_COL)
                                        .add(coopData)
                                        .addOnSuccessListener(firestoreSuccess)
                                        .addOnFailureListener(firestoreFailure);
                            });
                });
    }

    // Add a new farm for the signed-in farmer
    public void addFarm(Farm farm,
                        OnSuccessListener<DocumentReference> successListener,
                        OnFailureListener failureListener) {
        db.collection(FARMS_COL)
                .add(farm)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // Add a new measurement for the selected farm
    public void addMeasurement(Measurement measurement,
                               OnSuccessListener<DocumentReference> successListener,
                               OnFailureListener failureListener) {
        db.collection(MEASUREMENTS_COL)
                .add(measurement)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
    }

    // Get all farms for the signed-in farmer
    public void getFarmsByFarmer(String farmerId,
                                 OnCompleteListener<QuerySnapshot> listener) {
        db.collection(FARMS_COL)
                .whereEqualTo("farmerId", farmerId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Get measurements for the currently selected farm
    public void getMeasurementsByFarm(String farmId,
                                      OnCompleteListener<QuerySnapshot> listener) {
        db.collection(MEASUREMENTS_COL)
                .whereEqualTo("farmId", farmId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(listener);
    }

    // 1. Get all farmers under a cooperative
    public void getFarmersByCooperative(String cooperativeId,
                                        OnCompleteListener<QuerySnapshot> listener) {
        db.collection(FARMERS_COL)
                .whereEqualTo("cooperativeId", cooperativeId)
                .get()
                .addOnCompleteListener(listener);
    }

    // Add method to get a single farm
    public void getFarm(String farmId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(FARMS_COL).document(farmId).get().addOnCompleteListener(listener);
    }

    public void getFarmerName(String farmerId, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection(FARMERS_COL).document(farmerId)
                .get()
                .addOnCompleteListener(listener);
    }

    public void getCurrentFarmer(OnCompleteListener<QuerySnapshot> listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection(FARMERS_COL)
                    .whereEqualTo("userId", user.getUid())
                    .limit(1)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            listener.onComplete(task);
                        } else {
                            listener.onComplete(null);
                        }
                    });
        } else {
            listener.onComplete(null);
        }
    }

    // Add method to update a farm
    public void updateFarm(Farm farm, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        Map<String, Object> farmData = new HashMap<>();
        farmData.put("farmerId", farm.getFarmerId());
        farmData.put("latitude", farm.getLatitude());
        farmData.put("longitude", farm.getLongitude());
        farmData.put("soilType", farm.getSoilType());
        //farmData.put("metals", farm.getMetals());

        db.collection(FARMS_COL).document(farm.getId())
                .update(farmData)
                .addOnSuccessListener(successListener)
                .addOnFailureListener(failureListener);
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

    // Fixed the Farm conversion method (there were incorrect field mappings)
    public Farm snapshotToFarm(DocumentSnapshot snapshot) {
        return new Farm(
                snapshot.getId(),
                snapshot.getString("farmerId"),
                snapshot.getDouble("latitude"),
                snapshot.getDouble("longitude"),
                snapshot.getString("soilType"),
                snapshot.getString("metals"),
                snapshot.getString("farmName"),
                snapshot.getString("farmSize"),
                snapshot.getString("crops"),
                snapshot.getString("location")
        );
    }

    // Fixed the Measurement conversion method (typo in field names)
    public Measurement snapshotToMeasurement(DocumentSnapshot snapshot) {
        return new Measurement(
                snapshot.getId(),
                snapshot.getString("farmId"),
                snapshot.getDouble("salinity"),
                snapshot.getDouble("moisture"),
                snapshot.getDouble("temperature"),
                snapshot.getDouble("ph"),
                snapshot.getDouble("nitrogen"),
                snapshot.getDouble("phosphorus"),
                snapshot.getDouble("potassium"),
                snapshot.getString("metals")
        );
    }
}