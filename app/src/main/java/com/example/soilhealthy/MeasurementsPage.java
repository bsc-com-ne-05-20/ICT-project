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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.ssmsprojectapp.datamodels.FirestoreRepository;
import com.example.ssmsprojectapp.datamodels.Measurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MeasurementsPage extends AppCompatActivity {

    private FirestoreRepository repository;
    private  String selectedFarmId;

    // Soil Data Constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String[] HEAVY_METALS = {
            "zinc_extractable",
            "iron_extractable",
            "aluminium_extractable",
            "magnesium_extractable",
            "ph",
            "nitrogen_total",
            "phosphorous_extractable",
            "potassium_extractable",
            "bulk_density",
            "calcium_extractable",
            "sulphur_extractable"
    };
    private static final String DEPTH_LAYER = "0-20";

    // BLE Constants
    private static final String TAG = "SoilMonitorApp";
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID CHARACTERISTIC_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final String DEVICE_NAME = "SoilMonitor";
    private static final int REQUEST_ALL_PERMISSIONS = 1;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    // UI Elements
    private TextView tvResults, tvStatus, tvData;
    private Button btnFetch, btnConnect;
    private ProgressBar progressBar;

    // Soil Data Variables
    private boolean soilDataFetched = false;

    // BLE Variables
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice targetDevice;
    private boolean connected = false;

    //initializing soil property variables
    private double bulk_density;
    private double zinc;
    private double calcium;
    private double potassium;
    private double nitrogen;
    private double ph;
    private double sulphur;
    private double aluminium;
    private double magnesium;
    private double iron;
    private double phosphorous;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurements_page);

        repository = new FirestoreRepository();

        // Initialize UI
        tvResults = findViewById(R.id.tv_results);
        tvStatus = findViewById(R.id.tvStatus);
        tvData = findViewById(R.id.tvData);
        btnFetch = findViewById(R.id.btn_fetch);
        btnConnect = findViewById(R.id.btnConnect);
        progressBar = findViewById(R.id.progress_bar);

        // Check permissions
        if (!checkPermissions()) {
            requestPermissions();
        }

        // Initialize Bluetooth
        initializeBluetooth();

        // Set up button click listeners
        btnFetch.setOnClickListener(v -> {
            if (LocationHelper.checkLocationPermission(this)) {
                fetchData();
            } else {
                requestLocationPermission();
            }
        });

        btnConnect.setOnClickListener(v -> {
            if (!connected) {
                connectToDevice();
            } else {
                disconnectFromDevice();
            }
        });
    }

    // Soil Data Methods
    private void fetchData() {
        btnFetch.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                fetchHeavyMetals(latitude, longitude);
            }

            @Override
            public void onLocationFailed(String error) {
                runOnUiThread(() -> {
                    btnFetch.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MeasurementsPage.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchHeavyMetals(double lat, double lng) {
        Map<String, Double> allResults = new ConcurrentHashMap<>();
        AtomicInteger completedRequests = new AtomicInteger(0);

        for (String metal : HEAVY_METALS) {
            SoilDataFetcher.fetchSoilProperties(lat, lng, metal, DEPTH_LAYER,
                    new SoilDataFetcher.SoilDataCallback() {
                        @Override
                        public void onSuccess(Double value) {
                            // Assign values to variables based on property name
                            switch (metal) {
                                case "zinc_extractable":
                                    zinc = value;
                                    break;
                                case "iron_extractable":
                                    iron = value;
                                    break;
                                case "aluminium_extractable":
                                    aluminium = value;
                                    break;
                                case "magnesium_extractable":
                                    magnesium = value;
                                    break;
                                case "ph":
                                    ph = value;
                                    break;
                                case "nitrogen_total":
                                    nitrogen = value;
                                    break;
                                case "phosphorous_extractable":
                                    phosphorous = value;
                                    break;
                                case "potassium_extractable":
                                    potassium = value;
                                    break;
                                case "bulk_density":
                                    bulk_density = value;
                                    break;
                                case "calcium_extractable":
                                    calcium = value;
                                    break;
                                case "sulphur_extractable":
                                    sulphur = value;
                                    break;
                            }

                            allResults.put(metal, value);

                            if (completedRequests.incrementAndGet() == HEAVY_METALS.length) {
                                runOnUiThread(() -> {
                                    String result = formatMetalResults(allResults);
                                    tvResults.setText(result);
                                    btnFetch.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                    soilDataFetched = true;
                                });
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.e("FETCH_ERROR", "Failed to fetch " + metal + ": " + error);
                            allResults.put(metal, Double.NaN);

                            if (completedRequests.incrementAndGet() == HEAVY_METALS.length) {
                                runOnUiThread(() -> {
                                    String result = formatMetalResults(allResults);
                                    tvResults.setText(result);
                                    btnFetch.setEnabled(true);
                                    progressBar.setVisibility(View.GONE);
                                });
                            }
                        }
                    });
        }
    }

    private String formatMetalResults(Map<String, Double> metals) {
        StringBuilder sb = new StringBuilder();

        // Create a map of property names to their units
        Map<String, String> propertyUnits = new HashMap<String, String>() {{
            put("zinc_extractable", "ppm");
            put("iron_extractable", "ppm");
            put("aluminium_extractable", "ppm");
            put("magnesium_extractable", "ppm");
            put("ph", "");  // pH is unitless
            put("nitrogen_total", "g/kg");
            put("phosphorous_extractable", "ppm");
            put("potassium_extractable", "ppm");
            put("bulk_density", "g/cm³");
            put("calcium_extractable", "ppm");
            put("sulphur_extractable", "ppm");
        }};

        for (Map.Entry<String, Double> entry : metals.entrySet()) {
            String property = entry.getKey();
            double value = entry.getValue();
            String displayName = property.replace("_extractable", "")
                    .replace("_total", "")
                    .replace("_fraction", "");

            // Get the appropriate unit
            String unit = propertyUnits.get(property);
            if (unit.isEmpty()) {
                sb.append(String.format(Locale.US, "• %s: %.2f\n",
                        capitalize(displayName),
                        value));
            } else {
                sb.append(String.format(Locale.US, "• %s: %.2f %s\n",
                        capitalize(displayName),
                        value,
                        unit));
            }
        }
        return sb.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    // BLE Methods
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Toast.makeText(this, "Bluetooth is not available or disabled", Toast.LENGTH_LONG).show();
                return;
            }
        }

        updateStatus("Ready to connect");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("MissingPermission")
    private void connectToDevice() {
        updateStatus("Searching for device...");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (DEVICE_NAME.equals(device.getName())) {
                    targetDevice = device;
                    break;
                }
            }
        }

        if (targetDevice == null) {
            updateStatus("Device not found. Make sure it's paired.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            updateStatus("Connecting to " + targetDevice.getName() + "...");
        }
        btnConnect.setEnabled(false);

        bluetoothGatt = targetDevice.connectGatt(this, false, gattCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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
            displayText.append("Sensor Measurements:\n\n");
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

                runOnUiThread(() -> btnConnect.setText("Disconnect"));
                btnConnect.setEnabled(true);

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
                        gatt.setCharacteristicNotification(characteristic, true);

                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchData();
            } else {
                Toast.makeText(this,
                        "Location permission required for soil analysis",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_ALL_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this,
                        "Bluetooth permissions are required for device connection",
                        Toast.LENGTH_LONG).show();
            }
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

    //new starts here

    private void addNewMeasurement(View view) {
        if (selectedFarmId == null || selectedFarmId.isEmpty()) {
            Toast.makeText(view.getContext(), "Please select a farm first", Toast.LENGTH_SHORT).show();
            return;
        }

        Measurement newMeasurement = new Measurement(
                "", // ID will be generated by Firestore
                selectedFarmId,
                0.5, // salinity
                35.0, // moisture
                22.0, // temperature
                ph, // ph
                nitrogen, // nitrogen
                phosphorous, // phosphorus
                potassium, // potassium
                iron,
                zinc,
                aluminium,
                magnesium,
                bulk_density,
                sulphur,
                calcium,
                "None" // metals
        );

        repository.addMeasurement(
                newMeasurement,
                documentReference -> {
                    // Measurement added successfully
                    Toast.makeText(view.getContext(), "Measurement added successfully", Toast.LENGTH_SHORT).show();
                   // loadMeasurements(selectedFarmId); // Refresh measurements
                },
                e -> {
                    // Error adding measurement
                    Toast.makeText(view.getContext(), "Error adding measurement: ", Toast.LENGTH_SHORT).show();
                }
        );
    }
}