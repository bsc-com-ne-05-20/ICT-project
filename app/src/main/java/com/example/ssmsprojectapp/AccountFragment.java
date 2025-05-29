package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.Farmer;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AccountFragment extends Fragment {

    private AutoCompleteTextView etLocation;
    private TextView tvCoordinates;
    private PlacesClient placesClient;
    private ArrayAdapter<String> adapter;
    private List<AutocompletePrediction> predictionList;

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
        measurementsAdapter = new MeasurementsAdapter(new ArrayList<>(),this::onMeasurementSelected);
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
                onOpenAddnewFarm(v);
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


    @SuppressLint("MissingInflatedId")
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
        salinity.setText(measurement.getSalinity()+" dS/m");
        moisture.setText(measurement.getMoisture()+" %");
        nitrogen.setText(measurement.getNitrogen()+" ppm");
        phosphorous.setText(measurement.getPh()+" ppm");
        potassium.setText(measurement.getPotassium()+" ppm");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss the dialog
            }
        });
        builder.show();
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
                35.0, // moisture
                22.0, // temperature
                6.5, // ph
                13.0, // nitrogen
                17.0, // phosphorus
                26.0, // potassium
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
    private void onOpenAddnewFarm(View v) {

        Dialog dialog = new Dialog(v.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_new_farm_layout);

        //init the layout components here

        CheckBox take_measurement = dialog.findViewById(R.id.take_coordinates);

        //edittexts
        TextInputEditText farmName = dialog.findViewById(R.id.farm_name);
        TextInputEditText farmSize = dialog.findViewById(R.id.farm_size);
        TextInputEditText crops = dialog.findViewById(R.id.primaryCrops);
        TextInputEditText soilType = dialog.findViewById(R.id.soil_type);
        etLocation = dialog.findViewById(R.id.et_location);
        TextInputEditText latitude = dialog.findViewById(R.id.latitude);
        TextInputEditText longitude = dialog.findViewById(R.id.longitude);


        //autoComplete code starts here

        // Initialize Places
        if (!Places.isInitialized()) {
            Places.initialize(v.getContext(), "AIzaSyCKbTcYsUUH6wpT8F2N5eUR7rr_hAUE2b8");
        }
        placesClient = Places.createClient(v.getContext());

        adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_dropdown_item_1line);
        etLocation.setAdapter(adapter);

        // Listen for text input
        etLocation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setQuery(s.toString())
                                    .build();

                    placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener(response -> {
                                predictionList = response.getAutocompletePredictions();
                                List<String> suggestions = new ArrayList<>();
                                for (AutocompletePrediction prediction : predictionList) {
                                    suggestions.add(prediction.getFullText(null).toString());
                                }
                                adapter.clear();
                                adapter.addAll(suggestions);
                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etLocation.setOnItemClickListener((parent, view, position, id) -> {
            if (predictionList != null && position < predictionList.size()) {
                String placeId = predictionList.get(position).getPlaceId();
                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);

                FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            if (place.getLatLng() != null) {
                                double lati = place.getLatLng().latitude;
                                double lng = place.getLatLng().longitude;
                                latitude.setText(lati + "");
                                longitude.setText(lng + "");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to fetch coordinates", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        //ends here

        Button proceed = dialog.findViewById(R.id.proceed_button);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //more logic to follow

                try {
                    double la = Double.parseDouble(latitude.getText().toString());
                    double lo = Double.parseDouble(longitude.getText().toString());
                    String soil = soilType.getText().toString();
                    String metals = "";
                    String name = farmName.getText().toString();
                    String locatioN = etLocation.getText().toString();
                    String size = farmSize.getText().toString();
                    String primaryCrops = crops .getText().toString();


                    Farm newFarm = new Farm("", currUserID, la, lo, soil, metals,name,size,primaryCrops,locatioN);
                    addFarm(newFarm,v);
                    dialog.dismiss();

                    if (take_measurement.isChecked()){
                        startActivity(new Intent(getContext(), MeasurementsPage.class));
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(v.getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
                }

            }
        });
        Button cancel = dialog.findViewById(R.id.cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialoganimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}