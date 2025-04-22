package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ssmsprojectapp.datamodels.Farmer;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterFarmer extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private FirestoreRepository firestoreRepository;
    private Button register;
    private TextInputEditText editName, editEmail, editPhone, editLocation, editPassword,editRepeatePassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_farmer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
       /* mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();*/

        firestoreRepository = new FirestoreRepository();

        //init edittexts
        editName = findViewById(R.id.farmer_name);
        editEmail = findViewById(R.id.email);
        editPhone = findViewById(R.id.phoneNumber);
        editLocation = findViewById(R.id.location);
        editPassword = findViewById(R.id.password);
        editRepeatePassword = findViewById(R.id.repassword);

        ProgressBar progressBar = findViewById(R.id.register_progressbar);

        register = findViewById(R.id.btn_registerFarmer);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //register the coop th the firwebase and then if successful redirect to the login page
                progressBar.setVisibility(View.VISIBLE);
                String name = editName.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String location = editLocation.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String ePassword = editRepeatePassword.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                        TextUtils.isEmpty(phone) || TextUtils.isEmpty(location) ||
                        TextUtils.isEmpty(password)) {
                    Toast.makeText(v.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6){
                    editPassword.setError("Password must be at least 6 characters");
                    return;
                }
                if (!ePassword.matches(password)){
                    editRepeatePassword.setError("password mismatch");
                }

                //disable the input fields
                editName.setEnabled(false);
                editEmail.setEnabled(false);
                editPhone.setEnabled(false);
                editLocation.setEnabled(false);
                editPassword.setEnabled(false);
                Farmer farmer = new Farmer(name,email,phone,location,null);
                firestoreRepository.registerFarmerWithAuth(email, password, farmer, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(RegisterFarmer.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    }
                }, new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        startActivity(new Intent(RegisterFarmer.this, Login2.class));

                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });
    }

    private void registerUser() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(location) ||
                TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            saveUserToFirestore(uid, name, email, phone, location);
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String phone, String location) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("location", location);
        userMap.put("role", "farmer"); // or "cooperative_representative"

        db.collection("farmers").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Move to home screen or dashboard
                    startActivity(new Intent(RegisterFarmer.this, Login2.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
