package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

   private RecyclerViewAdapter rAdapter;
   private RecyclerView recyclerView;

   private TextView seeAll;
   private Button recommendations;

    private FirestoreRepository repository;
    private String currentFarmerId;

   private TextView farmerName;
   private  TextView farmName;

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
            farmerName.setText(name);
        }

        //init and set up the spinner
        //farmsnamesSpinner = view.findViewById(R.id.farm_name);
        List<String> categories = new ArrayList<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, categories);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        //farmsnamesSpinner.setAdapter(adapter);


        recyclerView = view.findViewById(R.id.recycler);
        rAdapter = new RecyclerViewAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(rAdapter);


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
        builder.setTitle("Add New Farm");

        View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.home_farms_layout, null);
        builder.setView(dialogView);

        RecyclerView recyclerFarms = dialogView.findViewById(R.id.f_recycler);


        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(true);
        builder.show();
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
}