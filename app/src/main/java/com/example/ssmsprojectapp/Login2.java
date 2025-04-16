package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class Login2 extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private TextInputEditText email;
    private TextInputEditText password;

    private Button signin, register;

    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init components and firebase

        progressBar = findViewById(R.id.progress);

        firebaseAuth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email_address);
        password = findViewById(R.id.password);

        signin = findViewById(R.id.signin_button);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //loginUser();

                startActivity(new Intent(Login2.this, HomePage.class));

            }
        });

        register = findViewById(R.id.registerBtn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login2.this, Login1.class));
            }
        });
    }


    private void loginUser() {
        String emailUser = email.getText().toString().trim();
        String pass = password.getText().toString().toString();

       if (!emailUser.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailUser).matches()){
           if (!pass.isEmpty()){
               progressBar.setVisibility(View.VISIBLE);
               firebaseAuth.signInWithEmailAndPassword(emailUser,pass).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                   @Override
                   public void onSuccess(AuthResult authResult) {
                       startActivity(new Intent(Login2.this, HomePage.class));
                       finish();
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {

                       progressBar.setVisibility(View.GONE);

                       Toast.makeText(Login2.this, "Login failed please try again", Toast.LENGTH_SHORT).show();
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