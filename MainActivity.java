package com.example.soilhealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
    private Button buttonConnectDisconnect, buttonCalculateAverages, buttonClearData;
    private TableLayout tableLayout;
    private TextView textViewAverages;
    private Switch switchTestMode;

    // Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private OutputStream outputStream;

    // Thread for Bluetooth communication
    private Thread receiveDataThread;

    // UUID for Serial Port Profile (SPP)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Permissions
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;

    // Test mode
    private BluetoothTestSimulator bluetoothTestSimulator;
    private boolean isTestMode = true; // Default to test mode

    // Data processor
    private DataProcessor dataProcessor = new DataProcessor();

    // Flag to control the receiveDataThread
    private volatile boolean isReceivingData = false; // Use volatile for thread safety

    // Flag to track connection state
    private volatile boolean isConnected = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI Components
        buttonConnectDisconnect = findViewById(R.id.buttonConnectDisconnect);
        buttonCalculateAverages = findViewById(R.id.buttonCalculateAverages);
        buttonClearData = findViewById(R.id.buttonClearData); // New button for clearing data
        tableLayout = findViewById(R.id.tableLayout);
        textViewAverages = findViewById(R.id.textViewAverages);
        switchTestMode = findViewById(R.id.switchTestMode);

        // Initialize Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initialize the Bluetooth test simulator
        bluetoothTestSimulator = new BluetoothTestSimulator(this);

        // Check and request permissions
        checkAndRequestPermissions();

        // Handle Test Mode Switch
        switchTestMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTestMode = isChecked;
            if (isTestMode) {
                Toast.makeText(this, "Test Mode Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth Mode Enabled", Toast.LENGTH_SHORT).show();
            }
        });

        // Button Click Listener for Connect/Disconnect
        buttonConnectDisconnect.setOnClickListener(v -> {
            if (isConnected) {
                // If connected, disconnect
                disconnectBluetooth();
            } else {
                // If not connected, connect
                connectToDevice("ESP32_1"); // Replace with your device name
            }
        });

        // Button Click Listener for Calculate Averages
        buttonCalculateAverages.setOnClickListener(v -> calculateAverages());

        // Button Click Listener for Clear Data
        buttonClearData.setOnClickListener(v -> clearData());
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

    // Connect to a specific Bluetooth device
    @SuppressLint("MissingPermission")
    private void connectToDevice(String deviceName) {
        if (isTestMode) {
            // Start generating test data
            bluetoothTestSimulator.startTest();
            Toast.makeText(this, "Test mode started", Toast.LENGTH_SHORT).show();
        } else {
            // Proceed with Bluetooth connection
            if (checkPermissions()) {
                if (!bluetoothAdapter.isEnabled()) {
                    // Request to enable Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    findBluetoothDevice(deviceName);
                }
            } else {
                requestPermissions();
            }
        }
    }

    // Find and connect to the Bluetooth device
    @SuppressLint("MissingPermission")
    private void findBluetoothDevice(String deviceName) {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    bluetoothDevice = device;
                    Toast.makeText(this, "Found " + deviceName, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Connected to " + bluetoothDevice.getName(), Toast.LENGTH_SHORT).show();

            // Stop test mode if it was running
            if (isTestMode) {
                bluetoothTestSimulator.stopTest();
                isTestMode = false; // Switch to Bluetooth mode
            }

            // Update connection state and button text
            isConnected = true;
            runOnUiThread(() -> buttonConnectDisconnect.setText("Disconnect"));

            // Start receiving data
            isReceivingData = true;
            startReceiveDataThread();
            Log.d("MainActivity", "Started receiving data");
        } catch (IOException e) {
            Toast.makeText(this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Connection failed", e);
        }
    }

    // Start the thread to receive data
    private void startReceiveDataThread() {
        receiveDataThread = new Thread(() -> {
            while (isReceivingData && inputStream != null) {
                try {
                    byte[] buffer = new byte[1024];
                    int bytes = inputStream.read(buffer); // Blocking call
                    if (bytes > 0) {
                        String receivedData = new String(buffer, 0, bytes);
                        processReceivedData(receivedData);
                    }
                } catch (IOException e) {
                    Log.e("MainActivity", "Error in receiveDataThread: " + e.getMessage());
                    break;
                }
            }
            Log.d("MainActivity", "Receive data thread stopped");
        });
        receiveDataThread.start();
    }

    // Disconnect Bluetooth
    private void disconnectBluetooth() {
        try {
            if (bluetoothSocket != null) {
                Log.d("MainActivity", "Disconnecting Bluetooth...");

                // Stop receiving data
                isReceivingData = false;

                // Stop test mode if it was running
                if (isTestMode) {
                    bluetoothTestSimulator.stopTest();
                }

                // Close the input stream to unblock the read() call
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }

                // Close the Bluetooth socket
                bluetoothSocket.close();
                bluetoothSocket = null;

                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

                // Update connection state and button text
                isConnected = false;
                runOnUiThread(() -> buttonConnectDisconnect.setText("Connect"));

                // Switch back to test mode
                isTestMode = true;
                bluetoothTestSimulator.startTest(); // Start generating test data
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error disconnecting: " + e.getMessage());
            Toast.makeText(this, "Error disconnecting", Toast.LENGTH_SHORT).show();
        }
    }

    // Process received data
    void processReceivedData(String data) {
        // Expected format: "Temperature:25.5,Salinity:1200,pH:6.5,Moisture:50,NPK:10-20-30"
        String[] parts = data.split(",");
        if (parts.length == 5) {
            float temperature = Float.parseFloat(parts[0].split(":")[1]); // Parse Temperature value
            int salinity = Integer.parseInt(parts[1].split(":")[1]); // Parse Salinity value
            float ph = Float.parseFloat(parts[2].split(":")[1]); // Parse pH value
            int moisture = Integer.parseInt(parts[3].split(":")[1]); // Parse Moisture value
            String npk = parts[4].split(":")[1]; // Parse NPK value

            // Add data to the DataProcessor
            dataProcessor.addData(temperature, salinity, ph, moisture, npk);

            // Update UI
            runOnUiThread(() -> {
                // Add a new row to the table
                TableRow row = new TableRow(MainActivity.this);
                row.addView(createTextView(String.valueOf(temperature)));
                row.addView(createTextView(String.valueOf(salinity)));
                row.addView(createTextView(String.valueOf(ph)));
                row.addView(createTextView(String.valueOf(moisture)));
                row.addView(createTextView(npk));
                tableLayout.addView(row);
            });
        }
    }

    // Create a TextView for table cells
    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        return textView;
    }

    // Calculate averages and display them
    private void calculateAverages() {
        float averageTemperature = dataProcessor.getAverageTemperature();
        float averageSalinity = dataProcessor.getAverageSalinity();
        float averagePh = dataProcessor.getAveragepH();
        float averageMoisture = dataProcessor.getAverageMoisture();
        String averageNPK = dataProcessor.getAverageNPK();

        // Display averages in the UI
        runOnUiThread(() -> {
            textViewAverages.setText(
                    "Average Temperature: " + averageTemperature + "\n" +
                            "Average Salinity: " + averageSalinity + "\n" +
                            "Average pH: " + averagePh + "\n" +
                            "Average Moisture: " + averageMoisture + "\n" +
                            "Average NPK: " + averageNPK
            );
        });
    }

    // Clear all data from the table and reset the data processor
    private void clearData() {
        // Clear the table
        tableLayout.removeAllViews();
        // Reset the data processor
        dataProcessor.clearData();
        // Clear the averages text view
        textViewAverages.setText("");
        Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop receiving data when the app is paused
        isReceivingData = false;
        if (receiveDataThread != null) {
            receiveDataThread.interrupt();
            receiveDataThread = null;
        }
        Log.d("MainActivity", "App paused, stopped receiving data");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop receiving data when the app is destroyed
        isReceivingData = false;
        if (receiveDataThread != null) {
            receiveDataThread.interrupt();
            receiveDataThread = null;
        }
        disconnectBluetooth(); // Ensure Bluetooth is disconnected
        bluetoothTestSimulator.cleanup(); // Clean up the test simulator
        Log.d("MainActivity", "App destroyed, stopped receiving data");
    }
}
