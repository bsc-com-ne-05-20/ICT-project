package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

   private RecyclerViewAdapter adapter;
   private RecyclerView recyclerView;

   private LinearLayout salinity,metals,nitrogen,phosphorous,temperature,moisture,potassium;
   private CardView recommendations, newMeasurement,graphs;

   private Spinner spinner;

    public HomeFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recycler);
        adapter = new RecyclerViewAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext(),LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);

        //init stats
        initStats(view);

        //init graphs
        graphs = view.findViewById(R.id.view_graphs);
        graphs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //navigate to the graphs activity
                startActivity(new Intent(view.getContext(), Graphs.class));
            }
        });
        //init the recommendations
        recommendations = view.findViewById(R.id.recommendations);
        recommendations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(view.getContext(), AIRecommendations.class));
            }
        });

        //init new measurement
        newMeasurement = view.findViewById(R.id.new_measurement);
        newMeasurement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(view.getContext(), MeasurementsPage.class));
            }
        });

        //init the spinner
        spinner = view.findViewById(R.id.spinner);

        // Create a dynamic ArrayList
        ArrayList<String> items = new ArrayList<>();
        items.add("Thondwe 8 acres");
        items.add("Bunda");
        items.add("see all");


        // Create Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set adapter to Spinner
        spinner.setAdapter(adapter);

        // Handle item selection
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                Toast.makeText(view.getContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();

                //load the data of that farm
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        return view;

    }

    private void initStats(View view) {
        salinity = view.findViewById(R.id.salinity);
        salinity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"salinity");
            }
        });
        metals = view.findViewById(R.id.metals);
        metals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"metals");
            }
        });
        nitrogen = view.findViewById(R.id.nitrogen);
        nitrogen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"nitrogen");
            }
        });
        phosphorous = view.findViewById(R.id.phosphorous);
        phosphorous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"phosphorous");
            }
        });
        temperature = view.findViewById(R.id.temperature);
        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"temperature");
            }
        });
        moisture = view.findViewById(R.id.moisture);
        moisture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"moisture");
            }
        });
        potassium = view.findViewById(R.id.potassium);
        potassium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statsDialog(view,"potassium");
            }
        });
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