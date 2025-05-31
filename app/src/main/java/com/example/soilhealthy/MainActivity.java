package com.example.soilhealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private Button buttonConnect, buttonDisconnect;
    private TextView textViewPh, textViewMetals, textViewSalinity, textViewLog;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    // Handler for Bluetooth communication
    private Handler handler = new Handler();

    // UUID for Serial Port Profile (SPP)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Permissions
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonDisconnect = findViewById(R.id.buttonDisconnect);
        textViewPh = findViewById(R.id.textViewPh);
        textViewMetals = findViewById(R.id.textViewMetals);
        textViewSalinity = findViewById(R.id.textViewSalinity);
        textViewLog = findViewById(R.id.textViewLog);

        // Disable Disconnect button initially
        buttonDisconnect.setEnabled(false);

        // Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check and request permissions
        checkAndRequestPermissions();

        // Button Click Listeners
        buttonConnect.setOnClickListener(v -> {
            if (checkPermissions()) {
                enableBluetooth();
            } else {
                requestPermissions();
            }
        });

        buttonDisconnect.setOnClickListener(v -> disconnectBluetooth());
    }

    // Check and request permissions
    private void checkAndRequestPermissions() {
        if (!checkPermissions()) {
            requestPermissions();
        }
    }

    // Check if permissions are granted
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Request permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, REQUEST_LOCATION_PERMISSION);
    }

    // Enable Bluetooth
    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            findBluetoothDevice();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            findBluetoothDevice();
        } else {
            Toast.makeText(this, "Bluetooth must be enabled", Toast.LENGTH_SHORT).show();
        }
    }

    // Find and connect to the Bluetooth device
    @SuppressLint("MissingPermission")
    private void findBluetoothDevice() {
        @SuppressLint("MissingPermission") Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("ESP32")) { // Replace with your ESP32's Bluetooth name
                    bluetoothDevice = device;
                    Toast.makeText(this, "Found ESP32", Toast.LENGTH_SHORT).show();
                    connectToBluetoothDevice();
                    break;
                }
            }
        } else {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show();
        }
    }

    // Connect to the Bluetooth device
    @SuppressLint("MissingPermission")
    private void connectToBluetoothDevice() {
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show();

            // Enable Disconnect button
            buttonDisconnect.setEnabled(true);
            buttonConnect.setEnabled(false);

            // Start receiving data
            handler.post(receiveDataRunnable);
        } catch (IOException e) {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Disconnect Bluetooth
    private void disconnectBluetooth() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                inputStream = null;
                outputStream = null;
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

                // Disable Disconnect button
                buttonDisconnect.setEnabled(false);
                buttonConnect.setEnabled(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Runnable to receive data
    private final Runnable receiveDataRunnable = new Runnable() {
        @Override
        public void run() {
            if (inputStream != null) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytes = inputStream.read(buffer);
                    String receivedData = new String(buffer, 0, bytes);
                    processReceivedData(receivedData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handler.postDelayed(this, 1000); // Poll every second
        }
    };

    // Process received data
    private void processReceivedData(String data) {
        // Expected format: "pH:6.5,Metals:1,Salinity:1200"
        String[] parts = data.split(",");
        if (parts.length == 3) {
            String ph = parts[0].split(":")[1];
            String metals = parts[1].split(":")[1];
            String salinity = parts[2].split(":")[1];

            // Update UI
            runOnUiThread(() -> {
                textViewPh.setText("Soil pH: " + ph);
                textViewMetals.setText("Heavy Metals: " + metals);
                textViewSalinity.setText("Soil Salinity: " + salinity);

                // Log data
                textViewLog.append("pH: " + ph + ", Metals: " + metals + ", Salinity: " + salinity + "\n");
            });
        }
    }
}