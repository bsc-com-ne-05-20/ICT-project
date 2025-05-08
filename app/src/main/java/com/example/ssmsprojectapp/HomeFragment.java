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

   private MeasurementsAdapter farmMeasurementRAdapter;
   private RecyclerView recyclerView;

   private HomeFarmAdapter homeFarmAdapter;
   private TextView seeAll;
   private Button recommendations;

    private FirestoreRepository repository;
    private String currentFarmerId;

   private TextView farmerName;
   private  TextView farmName;

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

        //setting the current user name
        farmerName = view.findViewById(R.id.farmer_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String email = user.getEmail();        // Farmer's email
            String name = user.getDisplayName();   // Farmer's name
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
        showProgress(view);

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
                    onFarmSelected(farms.get(0));
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
                // Update UI with measurements
                //change the recyclerView data here
                farmMeasurementRAdapter.updateData(measurements);
                //new HomePage(measurements,farmName);


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

    private void showProgress(View view){
        progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Loading farms..."); // Set message
        progressDialog.setTitle("Please wait"); // Set title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // or STYLE_HORIZONTAL
        progressDialog.setCancelable(false); // Optional - prevent dismissing by tapping outside
        progressDialog.show();
    }
}