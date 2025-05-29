package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssmsprojectapp.datamodels.Measurement;

import java.util.ArrayList;
import java.util.List;

public class SeeAllMeasurements extends AppCompatActivity {

    private TextView status;
    private RecyclerView recyclerView;

    private AllMeasurementsAdapter adapter;

    private List<Measurement> measurements;
    private String farmName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_see_all_measurements);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent intent = getIntent();

        measurements = new ArrayList<>();
        measurements = intent.getParcelableArrayListExtra("ALL_MEASUREMENTS");
        farmName = intent.getStringExtra("FARM_NAME");

        //iint the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setSubtitle(farmName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //init components
        status = findViewById(R.id.status);
        recyclerView = findViewById(R.id.recycler);

        if (!measurements.isEmpty()){
            status.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new AllMeasurementsAdapter(measurements,this::onMeasurementSelected);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }


    }

    public void onMeasurementSelected(View v,Measurement measurement){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.measurement_info_dialog_layout, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView date = dialogView.findViewById(R.id.date);
        TextView ph = dialogView.findViewById(R.id.tvph);
        TextView salinity = dialogView.findViewById(R.id.tvsalinity);
        TextView moisture = dialogView.findViewById(R.id.tvmoisture);
        TextView nitrogen = dialogView.findViewById(R.id.tvnitrogen);
        TextView phosphorous = dialogView.findViewById(R.id.tvphosphorous);
        TextView potassium = dialogView.findViewById(R.id.tvpotassium);

        date.setText(measurement.getTimestamp().toLocaleString());
        ph.setText(measurement.getPh()+"");
        salinity.setText(measurement.getSalinity()+"dS/m");
        moisture.setText(measurement.getMoisture()+"%");
        nitrogen.setText(measurement.getNitrogen()+"ppm");
        phosphorous.setText(measurement.getPh()+"ppm");
        potassium.setText(measurement.getPotassium()+"ppm");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog
            }
        });
        builder.show();
    }
}