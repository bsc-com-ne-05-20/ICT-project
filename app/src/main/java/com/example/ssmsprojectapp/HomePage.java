package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity implements HomeFragment.MeasurementsListDataListener{

    //location autoComplete
    private AutoCompleteTextView etLocation;
    private TextView tvCoordinates;
    private PlacesClient placesClient;
    private ArrayAdapter<String> adapter;
    private List<AutocompletePrediction> predictionList;

    private LinearLayout home,profile;

    //database
    private FirestoreRepository repository;
    private String currentFarmerId;
    private String currentUsername;
    private String selectedFarmname;

    private ProgressDialog progressDialog;
    private AlertDialog noFarmsDialog;

    private FloatingActionButton addmeasurement,goToChatbot;

    private List<Measurement> measurements = new ArrayList<>();

    //toolbar
    private Toolbar toolbar;

    public HomePage(){

    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //init toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init all database stuff
        repository = new FirestoreRepository();
        currentFarmerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();


        //init components

        //add the home fragment on successful login
        loadFarms2();

        //init fabs
        addmeasurement = findViewById(R.id.add_fab);
        addmeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, MeasurementsPage.class));
            }
        });


        goToChatbot = findViewById(R.id.go_to_chat);

        goToChatbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //pass some data ofcourse
                startActivity(new Intent(HomePage.this, AgriChatbot.class));
            }
        });

        //init bottom nav
        home = findViewById(R.id.nav_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,new HomeFragment(repository,currentFarmerId))
                        .commit();
            }
        });
        profile = findViewById(R.id.nav_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,new AccountFragment(repository,currentFarmerId))
                        .commit();
            }
        });


    }

    public void loadFarms2(){

        repository.getFarmsByFarmer(currentFarmerId, task -> {


            if (task.isSuccessful()) {
                List<Farm> farms = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    farms.add(repository.snapshotToFarm(document));
                }

                if (farms.isEmpty()){
                    noFarmsDialog();
                }
                else{
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.add(R.id.container,new HomeFragment(repository,currentFarmerId));
                    transaction.commit();
                }

            } else {
                Toast.makeText(this, "Failed to load farms: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    //the method to open the bottom sheet to open the sheet to add new farm


    public void openAccountSettings(){

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.account_settings_layout);

        //init the layout components here

        LinearLayout logout =  dialog.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, Login2.class));
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialoganimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you want to exit the app?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", (dialog, which) -> {
                    // 1. Dismiss dialog first
                    dialog.dismiss();
                    // 2. Then call super
                    super.onBackPressed();
                    // 3. Optional animation
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Simply dismiss on "No"
                    dialog.dismiss();
                })
                .setCancelable(false) // Prevent dismissing by tapping outside
                .show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.notify) {
            // Handle settings action
           getSupportFragmentManager().beginTransaction()
                   .replace(R.id.container,new NoficationsFragment())
                   .commit();
            return true;
        }
        else if (id == R.id.settings) {
            // Handle settings
            openAccountSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void loadMeasurements(String farmId) {
        repository.getMeasurementsByFarm(farmId, task -> {
            if (task.isSuccessful()) {
                List<Measurement> measurements = new ArrayList<>();
                for (DocumentSnapshot document : task.getResult()) {
                    measurements.add(repository.snapshotToMeasurement(document));
                }
                // Update UI with measurements
                //updateMeasurementsUI(measurements);
            } else {
                Log.e("FarmerDashboard", "Error loading measurements", task.getException());
            }
        });
    }


    @Override
    public void onListDataPassed(List<Measurement> list) {

    }

    private void noFarmsDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.no_farms_dialog_layout, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        TextView farmerName = dialogView.findViewById(R.id.farmers_name);
        farmerName.setText(currentUsername);
        Button addfarm = dialogView.findViewById(R.id.button_add_farm);
        addfarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Dismiss the noFarmsDialog before showing the add farm dialog
                if (noFarmsDialog != null && noFarmsDialog.isShowing()) {
                    noFarmsDialog.dismiss();
                }
                //showAddFarmDialog(v);
                onOpenAddnewFarm();
            }
        });

        noFarmsDialog = builder.create();
        noFarmsDialog.show();
    }



    private void onOpenAddnewFarm() {

        Dialog dialog = new Dialog(this);
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
            Places.initialize(getApplicationContext(), "AIzaSyCKbTcYsUUH6wpT8F2N5eUR7rr_hAUE2b8");
        }
        placesClient = Places.createClient(getApplicationContext());

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line);
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
                                Toast.makeText(HomePage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(HomePage.this, "Failed to fetch coordinates", Toast.LENGTH_SHORT).show();
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


                    Farm newFarm = new Farm("", currentFarmerId, la, lo, soil, metals,name,size,primaryCrops,locatioN);
                    addFarm(newFarm);
                    dialog.dismiss();

                    if (take_measurement.isChecked()){
                        startActivity(new Intent(HomePage.this, MeasurementsPage.class));
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
                //open no farms dialog
                noFarmsDialog();
            }
        });


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialoganimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private void addFarm(Farm farm) {
        showProgress("adding farm...");
        repository.addFarm(
                farm,
                documentReference -> {
                    dismissProgress();
                    Toast.makeText(this, "Farm added successfully", Toast.LENGTH_SHORT).show();
                    loadFarms2(); // Refresh the list
                },
                e -> {
                    dismissProgress();
                    Toast.makeText(this, "Error adding farm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void showProgress(String message){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message); // Set message
        progressDialog.setTitle("Please wait"); // Set title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // or STYLE_HORIZONTAL
        progressDialog.setCancelable(false); // Optional - prevent dismissing by tapping outside
        progressDialog.show();
    }
    public void dismissProgress(){
        progressDialog.dismiss();
    }
}