package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.Farmer;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class AccountFragment extends Fragment {

    private RecyclerView farmsRecycler, measurementsRecycler;

    private MeasurementsAdapter measurementsAdapter;

    private FarmAdapter farmAdapter;
    private Button addfarm;

    private FirestoreRepository repository;
    private String currUserID;

    private TextView name,email,phone;

    private TextView farm_name,farm_location,farm_size,farm_crops;
    private String selectedFarmId,farmName,farmSize,crops,location;


    public AccountFragment() {
        // Required empty public constructor
    }

    public AccountFragment(FirestoreRepository repository, String currUserID) {
        this.repository = repository;
        this.currUserID = currUserID;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        //init firestore repository
        repository = new FirestoreRepository();
        //init user info
        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        phone = view.findViewById(R.id.phone);

        loadFarmerData(currUserID);


        farmAdapter = new FarmAdapter(new ArrayList<>(), this::onFarmSelected);
        farmsRecycler = view.findViewById(R.id.farms_recycler);
        farmsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        farmsRecycler.setAdapter(farmAdapter);
        //load data from firestore
        loadFarms(view);

        //init the measurements recycler
        measurementsRecycler = view.findViewById(R.id.measurements_recycler);
        measurementsAdapter = new MeasurementsAdapter(new ArrayList<>());
        measurementsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        measurementsRecycler.setAdapter(measurementsAdapter);

        Button addMeasurement = view.findViewById(R.id.btn_take_measurement);
        addMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewMeasurement(view);
            }
        });

        addfarm = view.findViewById(R.id.add_farm);
        addfarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFarmDialog(v);
            }
        });

        //init currengt farm info views
        farm_name = view.findViewById(R.id.farm_nam);
        farm_location = view.findViewById(R.id.farm_location);
        farm_size = view.findViewById(R.id.size);
        farm_crops = view.findViewById(R.id.farm_crops);
        return view;
    }


    private void loadFarmerData(String userId) {
        FirebaseFirestore.getInstance()
                .collection("farmers")
                .whereEqualTo("userId",userId)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        Farmer farmer = document.toObject(Farmer.class);

                        // Access farmer details
                        String fullName = farmer.getName();
                        String uPhone = farmer.getPhone();
                        String uEmail = farmer.getLocation();
                        String cooperativeId = farmer.getCooperativeId();

                        name.setText(fullName);
                        email.setText(uEmail);
                        phone.setText(uPhone);
                    }
                });
    }

    private void addFarm(Farm farm,View v) {
        //showProgress(true);
        repository.addFarm(
                farm,
                documentReference -> {
                    Toast.makeText(v.getContext(), "Farm added successfully", Toast.LENGTH_SHORT).show();
                    loadFarms(v); // Refresh the list
                },
                e -> {
                    Toast.makeText(v.getContext(), "Error adding farm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void loadFarms(View view) {
        //showProgress(true);
        repository.getFarmsByFarmer(currUserID, task -> {
            //showProgress(false);
            if (task.isSuccessful()) {
                List<Farm> farms = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    farms.add(repository.snapshotToFarm(document));
                }

                farmAdapter.updateFarmData(farms);
                farmAdapter.notifyDataSetChanged();

                // Select first farm by default if available
                if (!farms.isEmpty()) {
                    onFarmSelected(farms.get(0));
                }
            } else {
                Toast.makeText(view.getContext(), "Failed to load farms: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onFarmSelected(Farm farm) {
        selectedFarmId = farm.getId();
        farmName = farm.getFarmName();
        farmSize = farm.getFarmSize();
        location = farm.getLocation();
        crops = farm.getCrops();

        //update ui

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                farm_name.setText(farmName);
                farm_size.setText(farmSize);
                farm_location.setText(location);
                farm_crops.setText(crops);
            }
        });

        loadMeasurements(selectedFarmId);
    }

    private void loadMeasurements(String farmId) {
        repository.getMeasurementsByFarm(farmId, task -> {
            if (task.isSuccessful()) {
                List<Measurement> measurements = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    measurements.add(repository.snapshotToMeasurement(document));
                }
                // Update UI with measurements
                //change the recyclerView data here
                measurementsAdapter.updateData(measurements);
                //new HomePage(measurements,farmName);


            } else {
                Log.e("FarmerDashboard", "Error loading measurements", task.getException());
            }
        });
    }

    private void addNewMeasurement(View view) {
        if (selectedFarmId == null || selectedFarmId.isEmpty()) {
            Toast.makeText(view.getContext(), "Please select a farm first", Toast.LENGTH_SHORT).show();
            return;
        }

        Measurement newMeasurement = new Measurement(
                "", // ID will be generated by Firestore
                selectedFarmId,
                0.5, // salinity
                25.0, // moisture
                22.0, // temperature
                6.5, // ph
                15.0, // nitrogen
                10.0, // phosphorus
                20.0, // potassium
                "None" // metals
        );

        repository.addMeasurement(
                newMeasurement,
                documentReference -> {
                    // Measurement added successfully
                    Toast.makeText(view.getContext(), "Measurement added successfully", Toast.LENGTH_SHORT).show();
                    loadMeasurements(selectedFarmId); // Refresh measurements
                },
                e -> {
                    // Error adding measurement
                    Toast.makeText(view.getContext(), "Error adding measurement: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    @SuppressLint("MissingInflatedId")
    private void showAddFarmDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setTitle("Add New Farm");

        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.add_farm_dialog, null);
        builder.setView(dialogView);

        EditText latitudeEditText = dialogView.findViewById(R.id.latitude_edit_text);
        EditText longitudeEditText = dialogView.findViewById(R.id.longitude_edit_text);
        EditText soilTypeEditText = dialogView.findViewById(R.id.soil_type_edit_text);
        EditText metalsEditText = dialogView.findViewById(R.id.metals_edit_text);
        EditText nameEditText = dialogView.findViewById(R.id.name_edit_text);
        EditText locationEditText = dialogView.findViewById(R.id.location_edit_text);
        EditText sizeEditText = dialogView.findViewById(R.id.size_edit_text);
        EditText cropsEditText = dialogView.findViewById(R.id.primaryCrops_edit_text);

        builder.setPositiveButton("Add", (dialog, which) -> {
            try {
                double latitude = Double.parseDouble(latitudeEditText.getText().toString());
                double longitude = Double.parseDouble(longitudeEditText.getText().toString());
                String soilType = soilTypeEditText.getText().toString();
                String metals = metalsEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String location = locationEditText.getText().toString();
                String size = sizeEditText.getText().toString();
                String crops = cropsEditText.getText().toString();

                Farm newFarm = new Farm("", currUserID, latitude, longitude, soilType, metals,name,size,crops,location);
                addFarm(newFarm,v);
            } catch (NumberFormatException e) {
                Toast.makeText(v.getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}