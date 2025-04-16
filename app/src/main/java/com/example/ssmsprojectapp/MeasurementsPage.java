package com.example.ssmsprojectapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MeasurementsPage extends AppCompatActivity{

    private static final String TAG = "SoilMonitorApp";

    // BLE UUIDs matching the ESP32 code
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final String DEVICE_NAME = "SoilMonitor";

    // UI Elements
    private TextView tvStatus, tvData;
    private Button btnConnect;

    // BLE Variables
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice targetDevice;
    private boolean connected = false;

    // Permission request code
    private static final int REQUEST_ALL_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_measurements_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI
        tvStatus = findViewById(R.id.tvStatus);
        tvData = findViewById(R.id.tvData);
        btnConnect = findViewById(R.id.btnConnect);

        // Check permissions
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Initialize Bluetooth
        initializeBluetooth();

        // Set up button click listener
        btnConnect.setOnClickListener(v -> {
            if (!connected) {
                connectToDevice();
            } else {
                disconnectFromDevice();
            }
        });
    }


    private boolean checkPermissions() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_ALL_PERMISSIONS);
    }

    @SuppressLint("MissingPermission")
    private void initializeBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth is not available or disabled", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Start scanning for devices (you could implement a scan here if needed)
        updateStatus("Ready to connect");
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice() {
        updateStatus("Searching for device...");

        // In a real app, you might want to scan for devices first
        // For simplicity, we'll try to connect directly to the known device name

        // Find the device by name
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            if (DEVICE_NAME.equals(device.getName())) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice == null) {
            updateStatus("Device not found. Make sure it's paired.");
            return;
        }

        updateStatus("Connecting to " + targetDevice.getName() + "...");
        btnConnect.setEnabled(false);

        // Connect to the GATT server
        bluetoothGatt = targetDevice.connectGatt(this, false, gattCallback);
    }

    @SuppressLint("MissingPermission")
    private void disconnectFromDevice() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    private void updateStatus(String message) {
        runOnUiThread(() -> tvStatus.setText(message));
    }

    private void updateDataDisplay(String data) {
        try {
            JSONObject json = new JSONObject(data);
            StringBuilder displayText = new StringBuilder();

            // GPS Data
            String gps = json.getString("gps");
            String[] latLng = gps.split(",");
            displayText.append("GPS Coordinates:\n");
            displayText.append("  Latitude: ").append(latLng[0]).append("\n");
            displayText.append("  Longitude: ").append(latLng[1]).append("\n\n");

            // Soil Parameters
            displayText.append("Soil Parameters:\n");
            displayText.append("  Moisture: ").append(json.getDouble("moisture")).append(" %\n");
            displayText.append("  Temperature: ").append(json.getDouble("temp")).append(" °C\n");
            displayText.append("  Salinity: ").append(json.getDouble("salinity")).append(" dS/m\n");
            displayText.append("  pH: ").append(json.getDouble("ph")).append("\n\n");

            // NPK Values
            displayText.append("Nutrient Levels:\n");
            displayText.append("  Nitrogen: ").append(json.getDouble("nitrogen")).append(" mg/kg\n");
            displayText.append("  Phosphorus: ").append(json.getDouble("phosphorus")).append(" mg/kg\n");
            displayText.append("  Potassium: ").append(json.getDouble("potassium")).append(" mg/kg\n");

            runOnUiThread(() -> tvData.setText(displayText.toString()));

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON data", e);
            runOnUiThread(() -> tvData.setText("Error parsing data: " + e.getMessage()));
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server");
                updateStatus("Connected to " + targetDevice.getName());
                connected = true;

                // Discover services
                runOnUiThread(() -> btnConnect.setText("Disconnect"));
                btnConnect.setEnabled(true);

                // Discover services after a small delay
                new Handler().postDelayed(() -> {
                    if (bluetoothGatt != null) {
                        bluetoothGatt.discoverServices();
                    }
                }, 500);

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server");
                updateStatus("Disconnected");
                connected = false;
                runOnUiThread(() -> {
                    btnConnect.setText("Connect");
                    btnConnect.setEnabled(true);
                });
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered");
                BluetoothGattService service = gatt.getService(SERVICE_UUID);

                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);

                    if (characteristic != null) {
                        // Enable notifications
                        gatt.setCharacteristicNotification(characteristic, true);

                        // Write to descriptor to enable notifications
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")); // Standard CCCD UUID
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }

                        updateStatus("Connected and ready to receive data");
                    } else {
                        updateStatus("Characteristic not found");
                    }
                } else {
                    updateStatus("Service not found");
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                updateStatus("Service discovery failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                String jsonString = new String(data, StandardCharsets.UTF_8);
                Log.i(TAG, "Received data: " + jsonString);

                updateDataDisplay(jsonString);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic read: " + characteristic.getStringValue(0));
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

}