package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterCooperative extends AppCompatActivity {

    private Button register;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextInputEditText name, location, number_of_members, repName,email,repphone, editPassword,editRepeatePassword;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_cooperative);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //init edittexts
        name= findViewById(R.id.coop_name);
        location = findViewById(R.id.coop_location);
        number_of_members = findViewById(R.id.number_of_members);
        repName = findViewById(R.id.rep_name);
        email = findViewById(R.id.rep_email);
        repphone = findViewById(R.id.rep_phone);
        editPassword= findViewById(R.id.rep_password);
        editRepeatePassword = findViewById(R.id.rep_reppassword);


        //init register button
        register = findViewById(R.id.btn_register);
        register.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //register the coop th the firwebase and then if successful redirect to the login page
                        //startActivity(new Intent(RegisterCooperative.this, Login2.class));
                        registerUser();
                    }
                });
    }

    private void registerUser() {
        String coop_name = name.getText().toString().trim();
        String number_of_member = number_of_members.getText().toString().trim();
        String name = repName.getText().toString().trim();
        String emelo = email.getText().toString().trim();
        String phone = repphone.getText().toString().trim();
        String coop_location = location.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(emelo) ||
                TextUtils.isEmpty(phone) || TextUtils.isEmpty(coop_location)||
                TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emelo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            saveUserToFirestore(uid,coop_name,number_of_member, name, emelo, phone, coop_location);
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String cName, String numOfMemebers,String name, String email, String phone, String location) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Cooperative_name", cName);
        userMap.put("num_of_members", numOfMemebers);
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("location", location);
        userMap.put("role", "cooperative"); // or "cooperative_representative"

        db.collection("Cooperatives").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    // Move to home screen or dashboard
                    startActivity(new Intent(RegisterCooperative.this, Login2.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}