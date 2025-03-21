package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFarmer extends AppCompatActivity {

    private Button signin;

    private FirebaseAuth firebaseAuth;
    private TextInputEditText email;
    private TextInputEditText password;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_farmer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init components
        //init components and firebase

        firebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_address);
        password = findViewById(R.id.password);

        signin = findViewById(R.id.signin_button);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();

            }
        });
    }

    private void loginUser() {

        String emailUser = email.getText().toString().trim();
        String pass = password.getText().toString().toString();

        if (!emailUser.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()){
            if (!pass.isEmpty()){
                firebaseAuth.signInWithEmailAndPassword(emailUser,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        startActivity(new Intent(LoginFarmer.this, HomePage.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(LoginFarmer.this, "Login failed please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                password.setError("Please provided password for your account");
            }
        }
        else if(emailUser.isEmpty()) {
            email.setError("Please provided email address for your account");
        }
        else {
            email.setError("please provide a valid email address");
        }
    }
}