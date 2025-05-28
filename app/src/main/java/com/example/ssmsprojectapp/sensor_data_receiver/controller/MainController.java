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

    private void checkPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    (android.app.Activity) context,
                    new String[]{
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    101);
        }
    } else {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    (android.app.Activity) context,
                    new String[]{
                            Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    101);
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void startDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Scan permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            view.setStatus("Bluetooth not enabled");
            return;
        }

        deviceList.clear();
        view.clearDeviceList();
        view.setStatus("Scanning...");

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        view.setStatus("Connecting to " + device.getName() + "...");

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothAdapter.cancelDiscovery();
                bluetoothSocket.connect();

                ((android.app.Activity) context).runOnUiThread(() -> view.setStatus("Connected to " + device.getName()));

                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();

            } catch (IOException e) {
                ((android.app.Activity) context).runOnUiThread(() -> view.setStatus("Connection Failed"));
                e.printStackTrace();
            }
        }).start();
    }

    public void disconnect() {
        try {
            if (connectedThread != null) {
                connectedThread.cancel();
                connectedThread = null;
            }
            view.setStatus("Disconnected");
        } catch (Exception e) {
            view.setStatus("Error Disconnecting");
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        try {
            context.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    if (!deviceList.contains(device)) {
                        deviceList.add(device);
                        view.addDevice(device.getName() + "\n" + device.getAddress());

                        view.getDeviceListView().setOnItemClickListener((parent, view1, position, id) -> {
                            connectToDevice(deviceList.get(position));
                        });
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ((android.app.Activity) context).runOnUiThread(() -> view.setStatus("Scan complete"));
            }
        }
    };

private class ConnectedThread extends Thread {
    private final InputStream inStream;

    public ConnectedThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        inStream = tmpIn;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];
        int bytes;

        while (true) {
            try {
                bytes = inStream.read(buffer);
                String incoming = new String(buffer, 0, bytes);

                try {
                    SensorData data = SensorData.fromJson(incoming);

                    ((android.app.Activity) context).runOnUiThread(() -> view.updateSensorData(data));
                } catch (JSONException e) {
                    // Optional: log or show toast
                }
            } catch (IOException e) {
                ((android.app.Activity) context).runOnUiThread(() -> view.setStatus("Connection Lost"));
                break;
            }
        }
    }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}