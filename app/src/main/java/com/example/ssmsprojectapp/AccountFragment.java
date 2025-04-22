package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;


public class AccountFragment extends Fragment {

    private RecyclerView farmsRecycler;

    private FarmAdapter farmAdapter;
    private Button addfarm;

    private FirestoreRepository repository;
    private String currUserID;

    private TextView name,email,phone;
    private String selectedFarmId;


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


        farmAdapter = new FarmAdapter(new ArrayList<>());
        farmsRecycler = view.findViewById(R.id.farms_recycler);
        farmsRecycler.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        farmsRecycler.setAdapter(farmAdapter);
        //load data from firestore
        loadFarms(view);


        addfarm = view.findViewById(R.id.add_farm);
        addfarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFarmDialog(v);
            }
        });

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
                    //showProgress(false);
                    Toast.makeText(v.getContext(), "Farm added successfully", Toast.LENGTH_SHORT).show();
                    loadFarms(v); // Refresh the list
                },
                e -> {
                    //showProgress(false);
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
                farmAdapter.updateData(farms);

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
        /*measurementsLabel.setVisibility(View.VISIBLE);
        addMeasurementButton.setVisibility(View.VISIBLE);
        loadMeasurements(selectedFarmId);*/
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

        builder.setPositiveButton("Add", (dialog, which) -> {
            try {
                double latitude = Double.parseDouble(latitudeEditText.getText().toString());
                double longitude = Double.parseDouble(longitudeEditText.getText().toString());
                String soilType = soilTypeEditText.getText().toString();
                String metals = metalsEditText.getText().toString();

                Farm newFarm = new Farm("", currUserID, latitude, longitude, soilType, metals);
                addFarm(newFarm,v);
            } catch (NumberFormatException e) {
                Toast.makeText(v.getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}