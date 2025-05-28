package com.example.ssmsprojectapp.sensor_data_receiver.view;

import android.content.Context;
import android.widget.*;

import com.example.ssmsprojectapp.R;
import com.example.ssmsprojectapp.sensor_data_receiver.model.SensorData;

import java.util.List;

/**
 * Handles all UI elements and updates in the MainActivity.
 * Responsible for displaying sensor data and device list to the user.
 */
public class MainView {
    private TextView statusText, moistureText, tempText, ecText, phText, nitrogenText, phosphorusText, potassiumText;
    private Button scanButton, clearButton, disconnectButton;
    private ListView deviceListView;
    private ArrayAdapter<String> deviceAdapter;

    public MainView(Context context) {
        // Initialize UI components by finding them from the Activity layout
        statusText = ((android.app.Activity) context).findViewById(R.id.statusText);
        moistureText = ((android.app.Activity) context).findViewById(R.id.moistureText);
        tempText = ((android.app.Activity) context).findViewById(R.id.tempText);
        ecText = ((android.app.Activity) context).findViewById(R.id.ecText);
        phText = ((android.app.Activity) context).findViewById(R.id.phText);
        nitrogenText = ((android.app.Activity) context).findViewById(R.id.nitrogenText);
        phosphorusText = ((android.app.Activity) context).findViewById(R.id.phosphorusText);
        potassiumText = ((android.app.Activity) context).findViewById(R.id.potassiumText);

        scanButton = ((android.app.Activity) context).findViewById(R.id.scanButton);
        clearButton = ((android.app.Activity) context).findViewById(R.id.clearButton);
        disconnectButton = ((android.app.Activity) context).findViewById(R.id.disconnectButton);

        deviceListView = ((android.app.Activity) context).findViewById(R.id.deviceList);
        deviceAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
        deviceListView.setAdapter(deviceAdapter);
    }

    /** Updates sensor data UI elements */
    public void updateSensorData(SensorData data) {
        moistureText.setText("Moisture: " + data.moisture + " %");
        tempText.setText("Temperature: " + data.temperature + " °C");
        ecText.setText("EC: " + data.ec + " uS/cm");
        phText.setText("pH: " + data.ph);
        nitrogenText.setText("Nitrogen: " + data.nitrogen);
        phosphorusText.setText("Phosphorus: " + data.phosphorus);
        potassiumText.setText("Potassium: " + data.potassium);
    }

    /** Clears the sensor data display */
    public void clearSensorData() {
        moistureText.setText("Moisture: --%");
        tempText.setText("Temperature: --°C");
        ecText.setText("EC: --");
        phText.setText("pH: --");
        nitrogenText.setText("Nitrogen: --");
        phosphorusText.setText("Phosphorus: --");
        potassiumText.setText("Potassium: --");
    }

    /** Updates the status text shown to the user */
    public void setStatus(String status) {
        statusText.setText(status);
    }

    /** Adds a Bluetooth device name and address to the list view */
    public void addDevice(String deviceNameAndAddress) {
        deviceAdapter.add(deviceNameAndAddress);
        deviceAdapter.notifyDataSetChanged();
    }

    /** Clears the device list UI */
    public void clearDeviceList() {
        deviceAdapter.clear();
        deviceAdapter.notifyDataSetChanged();
    }

    /** Returns the device list view, so controller can attach click listeners */
    public ListView getDeviceListView() {
        return deviceListView;
    }

    /** Returns references to buttons for controller to attach listeners */
    public Button getScanButton() {
        return scanButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getDisconnectButton() {
        return disconnectButton;
    }
}
