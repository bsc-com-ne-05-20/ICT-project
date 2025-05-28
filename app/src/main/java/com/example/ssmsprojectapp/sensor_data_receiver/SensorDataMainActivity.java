package com.example.ssmsprojectapp.sensor_data_receiver;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ssmsprojectapp.sensor_data_receiver.controller.MainController;
import com.example.ssmsprojectapp.sensor_data_receiver.view.MainView;

/**
 * Main Activity that initializes the MVC components.
 */
public class SensorDataMainActivity extends AppCompatActivity {

    private MainController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize view and controller
        MainView view = new MainView(this);
        controller = new MainController(this, view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up controller resources
        if (controller != null) {
            controller.onDestroy();
        }
    }
}
