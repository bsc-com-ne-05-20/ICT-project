package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private FloatingActionButton fab;
    private LinearLayout home,chat,analytics,profile;

    //database
    private FirestoreRepository repository;
    private String currentFarmerId;
    private String currentUsername;
    private String selectedFarmname;

    private List<Measurement> measurements;

    //toolbar
    private Toolbar toolbar;
    private static final int NOTIFICATIONS = R.id.notify;
    private static final int ADD_FARM = R.id.add;

    public HomePage(){

    }
    public HomePage(List<Measurement> measurements,String farmName){
        this.measurements = measurements;
        this.selectedFarmname = farmName;
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


        //init components

        //add the home fragment on successful login
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container,new HomeFragment(repository,currentFarmerId));
        transaction.commit();

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
        chat = findViewById(R.id.nav_chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(HomePage.this, AgriChatbot.class));
            }
        });
        analytics = findViewById(R.id.nav_analytics);
        analytics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, Graphs.class));
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


    //the method to open the bottom sheet to open the sheet to add new farm
    private void onOpenAddnewFarm() {

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_new_farm_layout);

        //init the layout components here

        CheckBox take_coordinates = dialog.findViewById(R.id.take_coordinates);

        Button proceed = dialog.findViewById(R.id.proceed_button);
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //more logic to follow
                if (take_coordinates.isChecked()){
                    startActivity(new Intent(HomePage.this, TakeCoordinates.class));
                }
                else {
                    startActivity(new Intent(HomePage.this, MeasurementsPage.class));
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

    @Override
    public void onBackPressed() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Do want to exit the app?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
        });
        builder.setNegativeButton("No",null);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();

        super.onBackPressed();
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
        } else if (id == R.id.add) {
            // Handle search action
            startActivity(new Intent(HomePage.this, MeasurementsPage.class));
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


}