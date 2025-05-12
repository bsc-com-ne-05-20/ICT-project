package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private LinearLayout linearLayout;
   private MeasurementsAdapter farmMeasurementRAdapter;
   private RecyclerView recyclerView;

   private HomeFarmAdapter homeFarmAdapter;
   private TextView seeAll;
   private Button recommendations;

    private FirestoreRepository repository;
    private String currentFarmerId;

   private TextView farmerName;

   //logged in farmers name
   private String name;
   private  TextView farmName;

   //measurements values textviews
    private TextView salinity_val,moisture_val,ph_val,nitrogen_val,phosphorous_val,potassium_val;
    private String selectedFarmId,selectedFarmName;

    //the progress bar
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    public HomeFragment() {
        // Required empty public constructor
    }

    public HomeFragment(FirestoreRepository repository, String currentFarmerId) {
        this.repository = repository;
        this.currentFarmerId = currentFarmerId;
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        //init the layout
        linearLayout = view.findViewById(R.id.homepage_layout);
        linearLayout.setVisibility(View.INVISIBLE);

        //setting the current user name
        farmerName = view.findViewById(R.id.farmer_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();        // Farmer's email
            name = user.getDisplayName();   // Farmer's name
            String uid = user.getUid();// Unique user ID
            farmerName.setText("Hi " + name);
        }
        TextView greeting = view.findViewById(R.id.greeting_text);
        greeting.setText(getMessage());


        //farm measurements recyclerView
        recyclerView = view.findViewById(R.id.recycler);
        farmMeasurementRAdapter = new MeasurementsAdapter(new ArrayList<>());
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(farmMeasurementRAdapter);

        //init the measurejents value text views
        //these will be updated when the measurements for a farm have been loaded

        salinity_val = view.findViewById(R.id.salinity_value);
        moisture_val = view.findViewById(R.id.moisture_value);
        ph_val = view.findViewById(R.id.ph_value);
        nitrogen_val = view.findViewById(R.id.nitrogen_value);
        phosphorous_val = view.findViewById(R.id.phosphorous_value);
        potassium_val = view.findViewById(R.id.potassium_value);



        //init recommendations button
        recommendations = view.findViewById(R.id.recommendations_button);
        recommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(), AIRecommendations.class));
            }
        });

        //init see all
        seeAll = view.findViewById(R.id.see_all_text);
        seeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(), SeeAllMeasurements.class));
            }
        });


        //init farmer and farm name
        farmerName = view.findViewById(R.id.farmer_name);

        homeFarmAdapter = new HomeFarmAdapter(new ArrayList<>(), this::onFarmSelected);
        //load farms
        loadFarms(view);

        farmName= view.findViewById(R.id.farm_name);
        farmName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFarmsDialog(v);
            }
        });
        return view;

    }

    private void openFarmsDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.home_farms_layout, null);
        builder.setView(dialogView);

        RecyclerView recyclerFarms = dialogView.findViewById(R.id.f_recycler);
        recyclerFarms.setLayoutManager(new LinearLayoutManager(v.getContext(),LinearLayoutManager.VERTICAL,false));
        recyclerFarms.setAdapter(homeFarmAdapter);

        loadFarms(v);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismissAlertDialog();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);

        // Create the AlertDialog object and show it
        alertDialog = builder.create();
        alertDialog.show();
    }

    private void dismissAlertDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void onFarmSelected(Farm farm) {

        selectedFarmId = farm.getId();
        selectedFarmName = farm.getFarmName();


        //update ui

        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                farmName.setText(selectedFarmName);

            }
        });

        loadMeasurements(selectedFarmId);

        //dimiss the dialog
        //dismissAlertDialog();
    }
    private void loadFarms(View view) {

        //show progress dialog
        showProgress(view,"Loading farms...");

        repository.getFarmsByFarmer(currentFarmerId, task -> {

            //dismiss the dialog
            progressDialog.dismiss();

            if (task.isSuccessful()) {
                List<Farm> farms = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    farms.add(repository.snapshotToFarm(document));
                }

                homeFarmAdapter.updateFarmData(farms);

                // Select first farm by default if available
                if (!farms.isEmpty()) {
                    linearLayout.setVisibility(View.VISIBLE);
                    onFarmSelected(farms.get(0));
                }
                else {
                    //show the no farms dialog view
                    linearLayout.setVisibility(View.INVISIBLE);
                    noFarmsDialog(view);
                }
            } else {
                Toast.makeText(view.getContext(), "Failed to load farms: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMeasurements(String farmId) {
        repository.getMeasurementsByFarm(farmId, task -> {
            if (task.isSuccessful()) {
                List<Measurement> measurements = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    measurements.add(repository.snapshotToMeasurement(document));
                }

                //change the recyclerView data here
                farmMeasurementRAdapter.updateData(measurements);
                //new HomePage(measurements,farmName);

                //getting the latest measurement
                if (!measurements.isEmpty()){
                    Measurement latestMeasurement = measurements.get(0);

                    // Update UI with measurements
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            salinity_val.setText(latestMeasurement.getSalinity() +"");
                            moisture_val.setText(latestMeasurement.getMoisture() +"");
                            ph_val.setText(latestMeasurement.getPh() +"");
                            nitrogen_val.setText(latestMeasurement.getNitrogen() +"");
                            phosphorous_val.setText(latestMeasurement.getPhosphorus() +"");
                            potassium_val.setText(latestMeasurement.getPotassium() +"");
                        }
                    });

                    //load the measurements to the visualisation fragments
                    new SalinityFragment(measurements);
                    new TemperatureFragment(measurements);
                    new NutrientsFragment(measurements);
                    new MoistureFragment(measurements);
                    new MetalsFragment(measurements);

                }
                //add else to handle the case where the farm is just created and there is no measurements yet





            } else {
                Log.e("FarmerDashboard", "Error loading measurements", task.getException());
            }
        });
    }
    private void initStats(View view) {

    }

    public void statsDialog(View view,String param){

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Notification");
        builder.setMessage("Data for " + param + " is currently unavailable");
        builder.setCancelable(false);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //the m
    private String getMessage() {
        String message = "good day";
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY);

        if (time >= 0 && time < 12){
            message = "Good morning";
        }
        else if (time >= 12 && time < 16){
            message = "Good afternoon";
        }
        else if (time >= 16 && time < 20){
            message = "Good evening";
        }
        else {
            message = "Good night";
        }

        return message;
    }

    private void showProgress(View view,String message){
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage(message); // Set message
        progressDialog.setTitle("Please wait"); // Set title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // or STYLE_HORIZONTAL
        progressDialog.setCancelable(false); // Optional - prevent dismissing by tapping outside
        progressDialog.show();
    }

    //adding farms and new farmer methods
    @SuppressLint("MissingInflatedId")
    private void noFarmsDialog(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.no_farms_dialog_layout, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView farmerName = dialogView.findViewById(R.id.farmers_name);
        farmerName.setText(name);
        Button addfarm = dialogView.findViewById(R.id.button_add_farm);
        addfarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFarmDialog(v);
            }
        });
        builder.show();
    }
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

                Farm newFarm = new Farm("", currentFarmerId, latitude, longitude, soilType, metals,name,size,crops,location);
                addFarm(newFarm,v);
            } catch (NumberFormatException e) {
                Toast.makeText(v.getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addFarm(Farm farm,View v) {
        showProgress(v,"adding farm...");
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
}