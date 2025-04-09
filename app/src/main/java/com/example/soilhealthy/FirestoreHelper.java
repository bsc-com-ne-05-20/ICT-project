package com.example.soilhealthy;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db;

    public interface UploadCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void uploadData(Map<String, Object> data, UploadCallback callback) {
        db.collection("soilAverages")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreHelper", "DocumentSnapshot added with ID: " + documentReference.getId());
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreHelper", "Error adding document", e);
                    callback.onFailure(e);
                });
    }
}