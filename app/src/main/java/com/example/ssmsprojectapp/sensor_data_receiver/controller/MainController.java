package com.example.ssmsprojectapp.sensor_data_receiver.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.ssmsprojectapp.sensor_data_receiver.model.SensorData;
import com.example.ssmsprojectapp.sensor_data_receiver.view.MainView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainController {

    private final Context context;
    private final MainView view;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;

    private final List<BluetoothDevice> deviceList = new ArrayList<>();

    private ConnectedThread connectedThread;

    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public MainController(Context context, MainView view) {
        this.context = context;
        this.view = view;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        checkPermissions();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filter);

        view.getScanButton().setOnClickListener(v -> startDiscovery());
        view.getClearButton().setOnClickListener(v -> view.clearSensorData());
        view.getDisconnectButton().setOnClickListener(v -> disconnect());
    }
}