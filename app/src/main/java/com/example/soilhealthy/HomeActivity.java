package com.example.soilhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnSoilData = findViewById(R.id.btn_soil_data);
        Button btnOtherData = findViewById(R.id.btn_other_data);
        Button btnSoilGrids = findViewById(R.id.btn_soilgrids); // New button

        btnSoilData.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        });

        btnOtherData.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LocalDataActivity.class));
            Toast.makeText(this, "Other data functionality", Toast.LENGTH_SHORT).show();
        });

        // New button click handler
        btnSoilGrids.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SoilDataActivity.class));
            Toast.makeText(this, "soil data fetching activity", Toast.LENGTH_SHORT).show();
        });
    }
}