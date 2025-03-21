package com.example.ssmsprojectapp;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

public class Login1 extends AppCompatActivity {
    private CheckBox imAdvisor;
    private CheckBox imFarmer;

    private Button getStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login1);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init components here
        imAdvisor = findViewById(R.id.checkBox1);
        imFarmer = findViewById(R.id.checkBox2);
        getStarted = findViewById(R.id.getStartedBtn);
        getStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //add the logic for advisor or farmer
                if (imAdvisor.isChecked()){
                    startActivity(new Intent(Login1.this, Login2.class));
                } else if (imFarmer.isChecked()) {
                    startActivity(new Intent(Login1.this, LoginFarmer.class));
                } else if (imFarmer.isChecked() && imAdvisor.isChecked()) {
                    Snackbar.make(v,"You can not select both options",Snackbar.LENGTH_SHORT).show();
                }
                else {
                    Snackbar.make(v,"Please select one option",Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}