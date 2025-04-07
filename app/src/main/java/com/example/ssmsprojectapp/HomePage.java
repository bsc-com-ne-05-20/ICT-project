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
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomePage extends AppCompatActivity {

    private FloatingActionButton fab;
    private BottomNavigationView bottomNavigationView;

    //database
    //private FirebaseFirestore database;

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

        //init all database stuff
        //database = FirebaseFirestore.getInstance();


        //init components

        //add the home fragment on successful login
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container,new HomeFragment());
        transaction.commit();

        //init fab
        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the bottom sheet

                onOpenAddnewFarm();
            }
        });


        //bottom navigation view inits
        bottomNavigationView = findViewById(R.id.bottom_nav_view);

        //display barges on the icons
        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.notifications);
        badgeDrawable.setNumber(32);
        badgeDrawable.setVisible(true);

        BadgeDrawable badgeDrawable2 = bottomNavigationView.getOrCreateBadge(R.id.inventory);
        badgeDrawable2.setNumber(202);
        badgeDrawable2.setVisible(true);

        BadgeDrawable badgeDrawable3 = bottomNavigationView.getOrCreateBadge(R.id.account);
        badgeDrawable3.setNumber(1);
        badgeDrawable3.setVisible(true);
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
        super.onBackPressed();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do want to exit the app?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No",null)
                .setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    //database methods

   /* public void write2Firestore(FirebaseFirestore db){
        Map<String, Object> farm = new HashMap<>();
        farm.put("owner", "John Doe");
        farm.put("location", "Lilongwe, Malawi");
        farm.put("soilPH", 6.5);

        db.collection("farms").document("Farm1")
                .set(farm)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Data added"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error adding data", e));

    }*/

    /*public void readData(FirebaseFirestore db){
        db.collection("farms").document("Farm1")
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String owner = document.getString("owner");
                        Log.d("Firestore", "Owner: " + owner);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching data", e));

    }*/

    /*public void updateData(FirebaseFirestore db){
        db.collection("farms").document("Farm1")
                .update("soilPH", 7.2)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Data updated"));

    }*/

    /*public void deleteData(FirebaseFirestore db){
        db.collection("farms").document("Farm1")
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Data deleted"));

    }*/
}