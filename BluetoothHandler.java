package com.example.soilhealthy;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BluetoothHandler {
    public interface BluetoothCallback {
        void onDeviceDiscovered(BluetoothDevice device, int rssi);
        void onConnectionStateChanged(boolean connected, String message);
        void onDataReceived(String data);
        void onError(String message);
        void onScanStatusChanged(boolean scanning);
    }

    // UUIDs matching ESP32 code
    private static final UUID SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final UUID DATA_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long SCAN_PERIOD = 10000;
    private static final long RECONNECT_DELAY = 2000;
    private static final long CONNECTION_TIMEOUT = 15000;

    private final Context context;
    private final Handler handler;
    private final BluetoothCallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bleScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice connectedDevice;

    private boolean isScanning = false;
    private boolean isConnecting = false;
    private boolean isConnected = false;
    private int retryCount = 0;
    private final Map<String, BluetoothDevice> discoveredDevices = new HashMap<>();

    public BluetoothHandler(Context context, BluetoothCallback callback) {
        this.context = context;
        this.callback = callback;
        this.handler = new Handler(Looper.getMainLooper());
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @SuppressLint("MissingPermission")
    public void startBleScan() {
        if (!checkPermissions()) {
            callback.onError("Bluetooth permissions not granted");
            return;
        }

        if (bluetoothAdapter == null) {
            callback.onError("Bluetooth not supported");
            return;
        }

        if (isScanning) {
            stopBleScan();
            return;
        }

        if (bleScanner == null) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bleScanner == null) {
                callback.onError("Failed to initialize BLE scanner");
                return;
            }
        }

        discoveredDevices.clear();
        isScanning = true;
        callback.onScanStatusChanged(true);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bleScanner.startScan(null, settings, scanCallback);
        handler.postDelayed(this::stopBleScan, SCAN_PERIOD);
    }

    @SuppressLint("MissingPermission")
    public void stopBleScan() {
        if (bleScanner != null && isScanning) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            callback.onScanStatusChanged(false);
        }
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        if (isConnecting || isConnected) return;

        isConnecting = true;
        retryCount = 0;
        connectedDevice = device;

        // Clear any existing connection
        disconnectBLE();

        // Start connection timeout
        handler.postDelayed(connectionTimeoutRunnable, CONNECTION_TIMEOUT);

        Log.d(TAG, "Connecting to device: " + device.getAddress());
        bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
    }

    private final Runnable connectionTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnecting) {
                Log.w(TAG, "Connection timeout reached");
                disconnectBLE();
                callback.onError("Connection timeout");
            }
        }
    };

    @SuppressLint("MissingPermission")
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    @SuppressLint("MissingPermission")
    public void requestEnableBluetooth(Activity activity) {
        if (bluetoothAdapter == null) {
            callback.onError("Bluetooth not supported");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnectBLE() {
        handler.removeCallbacks(connectionTimeoutRunnable);
        handler.removeCallbacks(reconnectRunnable);

        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.disconnect();
                bluetoothGatt.close();
            } catch (Exception e) {
                Log.e(TAG, "Error disconnecting", e);
            }
            bluetoothGatt = null;
        }

        isConnected = false;
        isConnecting = false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isScanning() {
        return isScanning;
    }

    @SuppressLint("MissingPermission")
    public String getConnectedDeviceName() {
        if (!isConnected || connectedDevice == null) {
            return null;
        }
        try {
            return connectedDevice.getName();
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied while getting device name", e);
            return null;
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();

            if (!discoveredDevices.containsKey(deviceAddress)) {
                discoveredDevices.put(deviceAddress, device);
                callback.onDeviceDiscovered(device, result.getRssi());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            callback.onError("Scan failed with error: " + errorCode);
            isScanning = false;
            callback.onScanStatusChanged(false);
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            handler.removeCallbacks(connectionTimeoutRunnable);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                handleConnectionFailure(gatt, status);
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                handleConnectedState(gatt);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                handleDisconnectedState(gatt);
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                callback.onError("Service discovery failed");
                disconnectBLE();
                return;
            }

            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service == null) {
                callback.onError("Service not found");
                disconnectBLE();
                return;
            }

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(DATA_UUID);
            if (characteristic == null) {
                callback.onError("Characteristic not found");
                disconnectBLE();
                return;
            }
            if ((characteristic.getProperties() &
                    (BluetoothGattCharacteristic.PROPERTY_NOTIFY | BluetoothGattCharacteristic.PROPERTY_INDICATE)) == 0) {
                callback.onError("Characteristic doesn't support notifications");
                disconnectBLE();
                return;
            }

            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                callback.onError("Failed to enable notifications");
                disconnectBLE();
                return;
            }

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID);
            if (descriptor == null) {
                // Try alternative UUID format (some devices use different UUIDs)
                descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor == null) {
                    callback.onError("Descriptor not found - check ESP32 configuration");
                    disconnectBLE();
                    return;
                }
            }

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if (!gatt.writeDescriptor(descriptor)) {
                callback.onError("Failed to write descriptor");
                disconnectBLE();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (characteristic.getUuid().equals(DATA_UUID)) {
                String data = new String(characteristic.getValue(), StandardCharsets.UTF_8);
                callback.onDataReceived(data);
            }
        }
    };

    private void handleConnectedState(BluetoothGatt gatt) {
        isConnected = true;
        isConnecting = false;
        retryCount = 0;

        String deviceName = "Unknown Device";
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                deviceName = gatt.getDevice().getName();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting device name", e);
        }

        callback.onConnectionStateChanged(true, "Connected to " + (deviceName != null ? deviceName : "device"));

        // Start service discovery
        if (!gatt.discoverServices()) {
            callback.onError("Failed to start service discovery");
            disconnectBLE();
        }
    }

    @SuppressLint("MissingPermission")
    private void handleDisconnectedState(BluetoothGatt gatt) {
        isConnected = false;
        isConnecting = false;

        if (retryCount < MAX_RETRY_ATTEMPTS && connectedDevice != null) {
            retryCount++;
            callback.onConnectionStateChanged(false, "Reconnecting... (Attempt " + retryCount + ")");
            handler.postDelayed(reconnectRunnable, RECONNECT_DELAY);
        } else {
            callback.onConnectionStateChanged(false, "Disconnected");
            connectedDevice = null;
        }

        try {
            gatt.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing GATT", e);
        }
    }

    @SuppressLint("MissingPermission")
    private void handleConnectionFailure(BluetoothGatt gatt, int status) {
        callback.onError("Connection failed with status: " + status);
        isConnecting = false;

        if (retryCount < MAX_RETRY_ATTEMPTS && connectedDevice != null) {
            retryCount++;
            callback.onConnectionStateChanged(false, "Retrying connection... (" + retryCount + "/" + MAX_RETRY_ATTEMPTS + ")");
            handler.postDelayed(reconnectRunnable, RECONNECT_DELAY);
        } else {
            callback.onConnectionStateChanged(false, "Connection failed");
            connectedDevice = null;
        }

        try {
            if (gatt != null) {
                gatt.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error closing GATT on failure", e);
        }
    }

    private final Runnable reconnectRunnable = new Runnable() {
        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            if (!isConnected && connectedDevice != null) {
                connectToDevice(connectedDevice);
            }
        }
    };

    public void cleanup() {
        handler.removeCallbacksAndMessages(null);
        disconnectBLE();
        stopBleScan();
    }
}