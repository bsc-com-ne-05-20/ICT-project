package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

public class AIRecommendations extends AppCompatActivity {

    private Button cropsRec,actionsRec,proceedButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_airecommendations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container,new CropRecommendations());
        transaction.commit();


        //init buttons
        cropsRec = findViewById(R.id.crops);
        cropsRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsRec.setTextColor(getResources().getColor(R.color.white));
                cropsRec.setTextColor(getResources().getColor(R.color.yellow));
                getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,new CropRecommendations())
                                .commit();

            }
        });
        actionsRec =findViewById(R.id.Actions_recommendations);
        actionsRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsRec.setTextColor(getResources().getColor(R.color.yellow));
                cropsRec.setTextColor(getResources().getColor(R.color.white));

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,new ActionsRecommendations())
                        .commit();

            }
        });
        proceedButton = findViewById(R.id.proceed_to_chat);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AIRecommendations.this, AgriChatbot.class));
            }
        });
    }
}