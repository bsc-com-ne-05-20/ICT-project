package com.example.ssmsprojectapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MeasurementsPage extends AppCompatActivity implements DeviceListAdapter.OnDeviceClickListener{

    private static final String TAG = "SoilMonitor";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private static final long SCAN_PERIOD = 10000; // 10 seconds scan

    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private boolean scanning = false;
    private Handler handler = new Handler();

    private Button scanButton;
    private TextView statusText;
    private TextView moistureText;
    private TextView temperatureText;
    private TextView ecText;
    private RecyclerView devicesRecyclerView;
    private DeviceListAdapter deviceListAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();


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
        scanButton = findViewById(R.id.scanButton);
        statusText = findViewById(R.id.statusText);
        moistureText = findViewById(R.id.moistureText);
        temperatureText = findViewById(R.id.temperatureText);
        ecText = findViewById(R.id.ecText);
        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);

        // Setup RecyclerView
        deviceListAdapter = new DeviceListAdapter(deviceList, this);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        devicesRecyclerView.setAdapter(deviceListAdapter);

        // Check permissions
        checkPermissions();

        // Initialize Bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // Set up scan button
        scanButton.setOnClickListener(v -> {
            if (!scanning) {
                startScan();
            } else {
                stopScan();
            }
        });
    }


    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN
                    },
                    PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void startScan() {
        deviceList.clear();
        deviceListAdapter.notifyDataSetChanged();
        scanning = true;
        scanButton.setText("Stop Scan");
        statusText.setText("Scanning...");

        handler.postDelayed(() -> {
            if (scanning) {
                stopScan();
            }
        }, SCAN_PERIOD);

        bluetoothLeScanner.startScan(scanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopScan() {
        scanning = false;
        scanButton.setText("Scan Devices");
        statusText.setText("Select a device to connect");
        bluetoothLeScanner.stopScan(scanCallback);
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device = result.getDevice();
            if (!deviceList.contains(device) ){
                deviceList.add(device);
                deviceListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Scan failed with error: " + errorCode);
            runOnUiThread(() -> {
                statusText.setText("Scan failed: " + errorCode);
                scanButton.setText("Scan Devices");
            });
            scanning = false;
        }
    };

    @Override
    public void onDeviceClick(BluetoothDevice device) {
        stopScan();
        connectToDevice(device);
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

        statusText.setText("Connecting to " + device.getName() + "...");
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                runOnUiThread(() -> statusText.setText("Connected - Discovering services..."));
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                runOnUiThread(() -> {
                    statusText.setText("Disconnected");
                    moistureText.setText("Moisture: --%");
                    temperatureText.setText("Temperature: --°C");
                    ecText.setText("EC: -- µS/cm");
                });
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        boolean success = gatt.setCharacteristicNotification(characteristic, true);
                        Log.d(TAG, "Notification set: " + success);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }

                        runOnUiThread(() -> statusText.setText("Connected to SoilMonitor"));
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            final String data = characteristic.getStringValue(0);
            runOnUiThread(() -> updateUI(data));
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor write successful");
            }
        }
    };

    private void updateUI(String data) {
        try {
            JSONObject json = new JSONObject(data);
            double moisture = json.getDouble("moisture");
            double temperature = json.getDouble("temperature");
            double ec = json.getDouble("ec");

            moistureText.setText(String.format("Moisture: %.1f%%", moisture));
            temperatureText.setText(String.format("Temperature: %.1f°C", temperature));
            ecText.setText(String.format("EC: %.1f µS/cm", ec));

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON", e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanning) {
            stopScan();
        }
    }

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