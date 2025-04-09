package com.example.soilhealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity implements BluetoothHandler.BluetoothCallback, DataReceiver {
    // UI Components
    private Button buttonConnectDisconnect, buttonCalculateAverages, buttonClearData;
    private TableLayout tableLayout;
    private TextView textViewAverages, textViewConnectionStatus;
    private View buttonUploadData;
    private ScrollView scrollView;
    private CheckBox checkBoxIncludeCoordinates;
    private EditText etPersonId;
    private ProgressBar progressBar;

    // Bluetooth
    private BluetoothHandler bluetoothHandler;
    private List<BluetoothDevice> discoveredDevices = new ArrayList<>();
    private ArrayAdapter<String> devicesAdapter;
    private AlertDialog deviceSelectionDialog;
    private BluetoothDevice lastConnectedDevice;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private int retryCount = 0;

    // Database
    private UnifiedDatabaseManager dbManager;
    private final DataProcessor dataProcessor = new DataProcessor();

    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupBluetooth();
        setupButtonListeners();
    }

    private void initializeViews() {
        etPersonId = findViewById(R.id.etPersonId);
        buttonConnectDisconnect = findViewById(R.id.buttonConnectDisconnect);
        buttonCalculateAverages = findViewById(R.id.buttonCalculateAverages);
        buttonClearData = findViewById(R.id.buttonClearData);
        buttonUploadData = findViewById(R.id.buttonUploadData);
        tableLayout = findViewById(R.id.tableLayout);
        textViewAverages = findViewById(R.id.textViewAverages);
        textViewConnectionStatus = findViewById(R.id.textViewConnectionStatus);
        scrollView = findViewById(R.id.scrollView);
        checkBoxIncludeCoordinates = findViewById(R.id.checkBoxIncludeCoordinates);
        progressBar = findViewById(R.id.progressBar);
        dbManager = new UnifiedDatabaseManager(this);
    }

    private void setupBluetooth() {
        bluetoothHandler = new BluetoothHandler(this, this);
    }

    private void setupButtonListeners() {
        buttonConnectDisconnect.setOnClickListener(v -> {
            if (bluetoothHandler.isConnected()) {
                bluetoothHandler.disconnectBLE();
                retryCount = 0; // Reset retry counter on manual disconnect
            } else {
                if (checkBluetoothPermissions()) {
                    startBluetoothOperation();
                } else {
                    requestBluetoothPermissions();
                }
            }
        });

        buttonCalculateAverages.setOnClickListener(v -> calculateAverages());
        buttonClearData.setOnClickListener(v -> clearData());
        buttonUploadData.setOnClickListener(v -> showUploadDataDialog());
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, PERMISSION_REQUEST_BLUETOOTH);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_BLUETOOTH);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBluetoothOperation();
            } else {
                showToast("Bluetooth permissions are required");
            }
        }
    }

    private void startBluetoothOperation() {
        if (bluetoothHandler.isBluetoothEnabled()) {
            showDeviceSelectionDialog();
        } else {
            bluetoothHandler.requestEnableBluetooth(this);
        }
    }

    // BluetoothCallback implementations
    @Override
    public void onDeviceDiscovered(BluetoothDevice device, int rssi) {
        runOnUiThread(() -> {
            if (!containsDevice(discoveredDevices, device)) {
                discoveredDevices.add(device);
                if (devicesAdapter != null) {
                    String deviceName = getDeviceNameWithPermissionCheck(device);
                    devicesAdapter.add(deviceName + "\n" + device.getAddress() + " (RSSI: " + rssi + "dBm)");
                    devicesAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private String getDeviceNameWithPermissionCheck(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                return "Unknown Device";
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return "Unknown Device";
            }
        }

        try {
            return device.getName() != null ? device.getName() : "Unknown Device";
        } catch (SecurityException e) {
            Log.e("Bluetooth", "Permission denied while getting device name", e);
            return "Unknown Device";
        }
    }

    @Override
    public void onConnectionStateChanged(boolean connected, String message) {
        runOnUiThread(() -> {
            buttonConnectDisconnect.setText(connected ? "DISCONNECT" : "CONNECT");
            textViewConnectionStatus.setText(message);
            textViewConnectionStatus.setTextColor(connected ? Color.GREEN : Color.RED);
            progressBar.setVisibility(View.GONE);

            if (connected) {
                retryCount = 0; // Reset retry counter on successful connection
                handler.removeCallbacksAndMessages(null); // Cancel any pending retries
            } else {
                // Only attempt reconnect if this was an unexpected disconnect
                if (retryCount < MAX_RETRY_ATTEMPTS) {
                    retryCount++;
                    progressBar.setVisibility(View.VISIBLE);
                    handler.postDelayed(() -> {
                        if (!bluetoothHandler.isConnected() && lastConnectedDevice != null) {
                            bluetoothHandler.connectToDevice(lastConnectedDevice);
                        }
                    }, 2000); // 2 second delay before reconnect attempt
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }

            showToast(message);
        });
    }

    @Override
    public void onDataReceived(String data) {
        try {
            Log.d("BLE_DATA", "Raw data received: " + data);
            JSONObject jsonData = new JSONObject(data);

            float ph = (float) jsonData.optDouble("ph", 0);
            float temperature = (float) jsonData.optDouble("temperature", 0);
            float nitrogen = (float) jsonData.optDouble("nitrogen", 0);
            float phosphorus = (float) jsonData.optDouble("phosphorus", 0);
            float potassium = (float) jsonData.optDouble("potassium", 0);
            float moisture = (float) jsonData.optDouble("moisture", 0);
            float salinity = (float) jsonData.optDouble("salinity", 0);

            double latitude = 0, longitude = 0;
            if (jsonData.has("latitude") && jsonData.has("longitude")) {
                latitude = jsonData.optDouble("latitude", 0);
                longitude = jsonData.optDouble("longitude", 0);
            } else if (jsonData.has("location") && jsonData.optString("location").equals("unavailable")) {
                Log.d("GPS", "Location data unavailable");
            }

            String personId = etPersonId.getText().toString().trim();
            if (personId.isEmpty()) {
                runOnUiThread(() -> {
                    showToast("Please enter Location/Person ID");
                    etPersonId.requestFocus();
                });
                return;
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());

            SoilData soilData = new SoilData(
                    temperature, salinity, ph, moisture,
                    nitrogen, phosphorus, potassium, personId
            );
            soilData.setTimestamp(timestamp);

            if (latitude != 0 && longitude != 0) {
                soilData.setLatitude(latitude);
                soilData.setLongitude(longitude);
            }

            saveToBothDatabases(soilData);

            String npk = String.format(Locale.getDefault(), "%.1f-%.1f-%.1f",
                    nitrogen, phosphorus, potassium);
            dataProcessor.addData(temperature, (int) salinity, ph, (int) moisture, npk);

            updateUI(soilData);

        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing JSON", e);
            runOnUiThread(() -> showToast("Invalid data format: " + e.getMessage()));
        } catch (Exception e) {
            Log.e("MainActivity", "Error processing data", e);
            runOnUiThread(() -> showToast("Error processing data"));
        }
    }

    @Override
    public void onError(String message) {
        runOnUiThread(() -> {
            showToast(message);
            textViewConnectionStatus.setText(message);
            textViewConnectionStatus.setTextColor(Color.RED);
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public void onScanStatusChanged(boolean scanning) {
        runOnUiThread(() -> {
            progressBar.setVisibility(scanning ? View.VISIBLE : View.GONE);
            if (deviceSelectionDialog != null && deviceSelectionDialog.isShowing()) {
                deviceSelectionDialog.setTitle(scanning ? "Scanning for devices..." : "Select a device");
            }
        });
    }

    private void showDeviceSelectionDialog() {
        discoveredDevices.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scanning for devices...");

        ListView devicesListView = new ListView(this);
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        devicesListView.setAdapter(devicesAdapter);

        devicesListView.setOnItemClickListener((parent, view, position, id) -> {
            if (position < discoveredDevices.size()) {
                lastConnectedDevice = discoveredDevices.get(position);
                bluetoothHandler.connectToDevice(lastConnectedDevice);
                deviceSelectionDialog.dismiss();
            }
        });

        builder.setView(devicesListView);
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            bluetoothHandler.stopBleScan();
            dialog.dismiss();
        });

        deviceSelectionDialog = builder.create();
        deviceSelectionDialog.setOnDismissListener(dialog -> bluetoothHandler.stopBleScan());
        deviceSelectionDialog.show();

        bluetoothHandler.startBleScan();
    }

    private boolean containsDevice(List<BluetoothDevice> devices, BluetoothDevice device) {
        for (BluetoothDevice d : devices) {
            if (d.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private void saveToBothDatabases(SoilData data) {
        try {
            DatabaseReference firebaseRef = FirebaseDatabase.getInstance().getReference("soil_readings");
            String firebaseKey = firebaseRef.push().getKey();
            if (firebaseKey != null) {
                firebaseRef.child(firebaseKey).setValue(data)
                        .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data saved"))
                        .addOnFailureListener(e -> Log.e("Firebase", "Save failed", e));
            }

            dbManager.addSoilData(data);
        } catch (Exception e) {
            Log.e("Database", "Error saving data", e);
        }
    }

    private void updateUI(SoilData data) {
        runOnUiThread(() -> {
            String npk = String.format(Locale.getDefault(), "%.1f-%.1f-%.1f",
                    data.getNitrogen(), data.getPhosphorus(), data.getPotassium());

            TableRow row = new TableRow(this);
            row.addView(createTextView(data.getPersonId()));
            row.addView(createTextView(String.format("%.1f", data.getTemperature())));
            row.addView(createTextView(String.format("%.1f", data.getSalinity())));
            row.addView(createTextView(String.format("%.1f", data.getPh())));
            row.addView(createTextView(String.format("%.1f", data.getMoisture())));
            row.addView(createTextView(npk));
            tableLayout.addView(row);

            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextSize(14);
        return textView;
    }

    private void calculateAverages() {
        List<SoilData> allData = dbManager.getAllSoilData();

        if (allData.isEmpty()) {
            showToast("No data available");
            return;
        }

        float sumTemp = 0, sumSalinity = 0, sumPh = 0, sumMoisture = 0;
        float sumNitrogen = 0, sumPhosphorus = 0, sumPotassium = 0;

        for (SoilData data : allData) {
            sumTemp += data.getTemperature();
            sumSalinity += data.getSalinity();
            sumPh += data.getPh();
            sumMoisture += data.getMoisture();
            sumNitrogen += data.getNitrogen();
            sumPhosphorus += data.getPhosphorus();
            sumPotassium += data.getPotassium();
        }

        int count = allData.size();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        SoilData averages = new SoilData(
                sumTemp / count,
                sumSalinity / count,
                sumPh / count,
                sumMoisture / count,
                sumNitrogen / count,
                sumPhosphorus / count,
                sumPotassium / count,
                "AVERAGES_" + timestamp
        );
        averages.setTimestamp(timestamp);

        dbManager.addSoilData(averages);

        runOnUiThread(() -> {
            textViewAverages.setText(String.format(
                    "Averages:\nTemp: %.1f°C\nSalinity: %.1f\npH: %.1f\nMoisture: %.1f\nNPK: %.1f-%.1f-%.1f",
                    averages.getTemperature(),
                    averages.getSalinity(),
                    averages.getPh(),
                    averages.getMoisture(),
                    averages.getNitrogen(),
                    averages.getPhosphorus(),
                    averages.getPotassium()
            ));

            TableRow row = new TableRow(this);
            row.addView(createTextView("AVG: " + timestamp));
            row.addView(createTextView(String.format("%.1f", averages.getTemperature())));
            row.addView(createTextView(String.format("%.1f", averages.getSalinity())));
            row.addView(createTextView(String.format("%.1f", averages.getPh())));
            row.addView(createTextView(String.format("%.1f", averages.getMoisture())));
            row.addView(createTextView(String.format("%.1f-%.1f-%.1f",
                    averages.getNitrogen(), averages.getPhosphorus(), averages.getPotassium())));
            tableLayout.addView(row);
        });
    }

    private void clearData() {
        runOnUiThread(() -> {
            while (tableLayout.getChildCount() > 1) {
                tableLayout.removeViewAt(1);
            }
            textViewAverages.setText("");
            dataProcessor.clearData();
            showToast("Data cleared");
        });
    }

    private void showUploadDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Soil Data Averages");

        // Inflate custom dialog layout
        View view = getLayoutInflater().inflate(R.layout.dialog_upload_data, null);
        checkBoxIncludeCoordinates = view.findViewById(R.id.checkBoxIncludeCoordinates);
        EditText etPersonId = view.findViewById(R.id.etPersonId); // Add this to your dialog_upload_data.xml
        CheckBox checkBoxConfirmUpload = view.findViewById(R.id.checkBoxConfirmUpload); // Add this to your dialog_upload_data.xml

        builder.setView(view);

        // Get average values
        float averageSalinity = dataProcessor.getAverageSalinity();
        float averagePh = dataProcessor.getAveragepH();
        float averageMoisture = dataProcessor.getAverageMoisture();
        String averageNPK = dataProcessor.getAverageNPK();

        builder.setPositiveButton("Upload", (dialog, which) -> {
            String personId = etPersonId.getText().toString().trim();

            // Validate inputs
            if (personId.isEmpty()) {
                showToast("Please enter Person ID");
                return;
            }

            if (!checkBoxConfirmUpload.isChecked()) {
                showToast("Please confirm the upload");
                return;
            }

            // Prepare upload data
            Map<String, Object> uploadData = new HashMap<>();
            uploadData.put("personId", personId);  // Added person ID
            uploadData.put("averageSalinity", averageSalinity);
            uploadData.put("averagePh", averagePh);
            uploadData.put("averageMoisture", averageMoisture);
            uploadData.put("averageNPK", averageNPK);
            uploadData.put("timestamp", FieldValue.serverTimestamp());

            // Handle coordinates
            if (checkBoxIncludeCoordinates.isChecked()) {
                uploadData.put("latitude", 0.0);
                uploadData.put("longitude", 0.0);
                uploadData.put("hasCoordinates", true);
            } else {
                uploadData.put("hasCoordinates", false);
            }


            // Upload data
            new FirestoreHelper().uploadData(uploadData, new FirestoreHelper.UploadCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        showToast("Averages uploaded successfully");
                        etPersonId.setText(""); // Clear person ID after upload
                        checkBoxConfirmUpload.setChecked(false); // Reset confirmation
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() -> showToast("Upload failed: " + e.getMessage()));
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Initially disable upload button until conditions are met
        Button uploadButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        uploadButton.setEnabled(false);

        // Add text watcher and checkbox listener to enable/disable upload button
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUploadButtonState(uploadButton, etPersonId, checkBoxConfirmUpload);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPersonId.addTextChangedListener(textWatcher);
        checkBoxConfirmUpload.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateUploadButtonState(uploadButton, etPersonId, checkBoxConfirmUpload);
        });
    }

    private void updateUploadButtonState(Button uploadButton, EditText etPersonId, CheckBox confirmCheckbox) {
        boolean isValid = !etPersonId.getText().toString().trim().isEmpty() &&
                confirmCheckbox.isChecked();
        uploadButton.setEnabled(isValid);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (bluetoothHandler != null) {
            bluetoothHandler.disconnectBLE();
        }
    }
}
