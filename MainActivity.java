package com.example.soilhealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FieldValue;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements DataReceiver
, BluetoothTestSimulator.TestDataListener{

    // UI Components
    private Button buttonConnectDisconnect, buttonCalculateAverages, buttonClearData;
    private TableLayout tableLayout;
    private TextView textViewAverages;
    private Switch switchTestMode;
    private View buttonUploadData;
    private CheckBox checkBoxIncludeCoordinates;

    // BLE
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;

    // BLE UUIDs (must match Arduino code)
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID DATA_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    // Permissions
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    // Test mode
    private BluetoothTestSimulator bluetoothTestSimulator;
    private boolean isTestMode = false;

    // Data processor
    private final DataProcessor dataProcessor = new DataProcessor();

    // Connection state
    private volatile boolean isConnected = false;
    private final Handler handler = new Handler();

    // BLE Scan
    private boolean isScanning = false;
    private static final long SCAN_PERIOD = 10000;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        buttonConnectDisconnect = findViewById(R.id.buttonConnectDisconnect);
        buttonCalculateAverages = findViewById(R.id.buttonCalculateAverages);
        buttonClearData = findViewById(R.id.buttonClearData);
        buttonUploadData = findViewById(R.id.buttonUploadData);
        tableLayout = findViewById(R.id.tableLayout);
        textViewAverages = findViewById(R.id.textViewAverages);
        switchTestMode = findViewById(R.id.switchTestMode);

        // Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetoothTestSimulator = new BluetoothTestSimulator(this);
        checkAndRequestPermissions();

        switchTestMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            Log.d("TEST_MODE", "Test mode: " + isChecked);
            if (isTestMode) {
                Toast.makeText(this, "Test Mode Enabled", Toast.LENGTH_SHORT).show();
                if (isConnected) disconnectBLE();
            } else {
                Toast.makeText(this, "BLE Mode Enabled", Toast.LENGTH_SHORT).show();
            }
        });

        buttonConnectDisconnect.setOnClickListener(v -> {
            if (isConnected) {
                disconnectBLE();
            } else {
                if (isTestMode) {
                    bluetoothTestSimulator.startTest();
                    isConnected = true;
                    buttonConnectDisconnect.setText("Disconnect");
                } else {
                    checkPermissionsAndStartScan();
                }
            }
        });

        buttonCalculateAverages.setOnClickListener(v -> calculateAverages());
        buttonClearData.setOnClickListener(v -> clearData());
        buttonUploadData.setOnClickListener(v -> showUploadDataDialog());
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted && !isConnected && !isTestMode) {
                checkPermissionsAndStartScan();
            } else if (!allGranted) {
                Toast.makeText(this, "Permissions required for BLE functionality", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkPermissionsAndStartScan() {
        if (hasRequiredPermissions()) {
            scanLeDevice();
        } else {
            checkAndRequestPermissions();
        }
    }

    private boolean hasRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @SuppressLint("MissingPermission")
    private void scanLeDevice() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (bleScanner == null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
        }

        if (isScanning) {
            stopBleScan();
            return;
        }

        Toast.makeText(this, "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
        isScanning = true;
        buttonConnectDisconnect.setText("Stop Scan");

        handler.postDelayed(() -> {
            if (isScanning) {
                stopBleScan();
                Toast.makeText(this, "Scan completed", Toast.LENGTH_SHORT).show();
            }
        }, SCAN_PERIOD);

        bleScanner.startScan(scanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopBleScan() {
        if (bleScanner != null && isScanning) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            buttonConnectDisconnect.setText("Connect");
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String deviceName = device.getName();

            if (deviceName != null && deviceName.equals("SoilHealthMonitor")) {
                stopBleScan();
                bluetoothDevice = device;
                connectToDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("MainActivity", "BLE Scan failed with error code: " + errorCode);
            Toast.makeText(MainActivity.this, "Scan failed", Toast.LENGTH_SHORT).show();
            isScanning = false;
            buttonConnectDisconnect.setText("Connect");
        }
    };

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                runOnUiThread(() -> {
                    buttonConnectDisconnect.setText("Disconnect");
                    Toast.makeText(MainActivity.this, "Connected to device", Toast.LENGTH_SHORT).show();
                });

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSIONS);
                        return;
                    }
                }
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                isConnected = false;
                runOnUiThread(() -> {
                    buttonConnectDisconnect.setText("Connect");
                    Toast.makeText(MainActivity.this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                });
                closeGatt();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic dataCharacteristic = service.getCharacteristic(DATA_UUID);
                    if (dataCharacteristic != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSIONS);
                                return;
                            }
                        }

                        gatt.setCharacteristicNotification(dataCharacteristic, true);
                        BluetoothGattDescriptor descriptor = dataCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(DATA_UUID)) {
                byte[] data = characteristic.getValue();
                String jsonString = new String(data, StandardCharsets.UTF_8);
                processReceivedData(jsonString);
            }
        }
    };


    public void processReceivedData(String jsonString) {
        try {
            JSONObject jsonData = new JSONObject(jsonString);
            float ph = (float) jsonData.getDouble("ph");
            float temperature = (float) jsonData.getDouble("temperature");
            float nitrogen = (float) jsonData.getDouble("nitrogen");
            float phosphorus = (float) jsonData.getDouble("phosphorus");
            float potassium = (float) jsonData.getDouble("potassium");
            float moisture = (float) jsonData.getDouble("moisture");
            float salinity = (float) jsonData.getDouble("salinity");

            String npk = nitrogen + "-" + phosphorus + "-" + potassium;
            dataProcessor.addData(temperature, (int) salinity, ph, (int) moisture, npk);

            runOnUiThread(() -> {
                TableRow row = new TableRow(MainActivity.this);
                row.addView(createTextView(String.valueOf(temperature)));
                row.addView(createTextView(String.valueOf((int) salinity)));
                row.addView(createTextView(String.valueOf(ph)));
                row.addView(createTextView(String.valueOf((int) moisture)));
                row.addView(createTextView(npk));
                tableLayout.addView(row);
            });
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing JSON data", e);
        }
    }
    @Override
    public void onTestDataReceived(String data) {
        // Process test data the same way as BLE data
        Log.d("MainActivity", "Received test data: " + data);
        runOnUiThread(() -> {
            try {
                JSONObject jsonData = new JSONObject(data);
                float ph = (float) jsonData.getDouble("ph");
                float temperature = (float) jsonData.getDouble("temperature");
                int nitrogen = jsonData.getInt("nitrogen");
                int phosphorus = jsonData.getInt("phosphorus");
                int potassium = jsonData.getInt("potassium");
                int moisture = jsonData.getInt("moisture");
                int salinity = jsonData.getInt("salinity");

                String npk = nitrogen + "-" + phosphorus + "-" + potassium;
                dataProcessor.addData(temperature, salinity, ph, moisture, npk);

                TableRow row = new TableRow(this);
                row.addView(createTextView(String.valueOf(temperature)));
                row.addView(createTextView(String.valueOf(salinity)));
                row.addView(createTextView(String.valueOf(ph)));
                row.addView(createTextView(String.valueOf(moisture)));
                row.addView(createTextView(npk));
                tableLayout.addView(row);
            } catch (JSONException e) {
                Log.e("MainActivity", "Error processing test data", e);
            }
        });
    }

    @Override
    public void onDataReceived(String data) {
        Log.d("MainActivity", "Received arduino data: " + data);
        processReceivedData(data);
    }

    @SuppressLint("MissingPermission")
    private void disconnectBLE() {
        if (isTestMode) {
            bluetoothTestSimulator.stopTest();
            isConnected = false;
            buttonConnectDisconnect.setText("Connect");
            return;
        }

        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    private void closeGatt() {
        if (bluetoothGatt != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSIONS);
                    return;
                }
            }
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        return textView;
    }

    private void calculateAverages() {
        float averageTemperature = dataProcessor.getAverageTemperature();
        float averageSalinity = dataProcessor.getAverageSalinity();
        float averagePh = dataProcessor.getAveragepH();
        float averageMoisture = dataProcessor.getAverageMoisture();
        String averageNPK = dataProcessor.getAverageNPK();

        runOnUiThread(() -> textViewAverages.setText(
                "Average Temperature: " + averageTemperature + "\n" +
                        "Average Salinity: " + averageSalinity + "\n" +
                        "Average pH: " + averagePh + "\n" +
                        "Average Moisture: " + averageMoisture + "\n" +
                        "Average NPK: " + averageNPK
        ));
    }

    private void clearData() {
        // Remove all rows except the first one (header row)
        if (tableLayout.getChildCount() > 1) {
            tableLayout.removeViews(1, tableLayout.getChildCount() - 1);
        }

        // Clear the data processor
        dataProcessor.clearData();

        // Clear the averages display
        textViewAverages.setText("");

        Toast.makeText(this, "Measurements cleared", Toast.LENGTH_SHORT).show();
    }
    private void showUploadDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Soil Data Averages");

        View view = getLayoutInflater().inflate(R.layout.dialog_upload_data, null);
        checkBoxIncludeCoordinates = view.findViewById(R.id.checkBoxIncludeCoordinates);
        builder.setView(view);

        float averageSalinity = dataProcessor.getAverageSalinity();
        float averagePh = dataProcessor.getAveragepH();
        float averageMoisture = dataProcessor.getAverageMoisture();
        String averageNPK = dataProcessor.getAverageNPK();

        builder.setPositiveButton("Upload", (dialog, which) -> {
            Map<String, Object> uploadData = new HashMap<>();
            uploadData.put("averageSalinity", averageSalinity);
            uploadData.put("averagePh", averagePh);
            uploadData.put("averageMoisture", averageMoisture);
            uploadData.put("averageNPK", averageNPK);
            uploadData.put("timestamp", FieldValue.serverTimestamp());

            if (checkBoxIncludeCoordinates.isChecked()) {
                uploadData.put("latitude", 0.0);
                uploadData.put("longitude", 0.0);
                uploadData.put("hasCoordinates", true);
            } else {
                uploadData.put("hasCoordinates", false);
            }

            new FirestoreHelper().uploadData(uploadData, new FirestoreHelper.UploadCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Averages uploaded successfully", Toast.LENGTH_SHORT).show()
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBleScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBLE();
        closeGatt();
        if (bluetoothTestSimulator != null) {
            bluetoothTestSimulator.cleanup();
        }
    }
}
