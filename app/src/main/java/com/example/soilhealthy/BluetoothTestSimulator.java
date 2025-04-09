package com.example.soilhealthy;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class BluetoothTestSimulator {

    public interface TestDataListener {
        void onTestDataReceived(String data);
    }

    private Handler handler;
    private Runnable testRunnable;
    private volatile boolean isTesting = false;
    private TestDataListener listener; // Changed from WeakReference
    private static final long TEST_DURATION = 6000;
    private static final long TEST_INTERVAL = 1000;

    public BluetoothTestSimulator(TestDataListener listener) {
        this.listener = listener; // Strong reference
        this.handler = new Handler(Looper.getMainLooper()); // Explicit main looper
    }

    public synchronized void startTest() {
        if (isTesting) {
            Log.d("BluetoothTestSimulator", "Test mode is already running");
            return;
        }
        isTesting = true;

        testRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isTesting) return;

                String testData = generateTestDataJson();
                Log.d("BluetoothTestSimulator", "Sending test data: " + testData);

                if (listener != null) {
                    listener.onTestDataReceived(testData);
                    handler.postDelayed(this, TEST_INTERVAL);
                } else {
                    Log.e("BluetoothTestSimulator", "Listener is null");
                    stopTest();
                }
            }
        };

        handler.post(testRunnable);
        Log.d("BluetoothTestSimulator", "Test mode started");

        // Stop automatically after duration
        handler.postDelayed(this::stopTest, TEST_DURATION);
    }

    public synchronized void stopTest() {
        if (!isTesting) {
            Log.d("BluetoothTestSimulator", "Test mode is already stopped");
            return;
        }
        isTesting = false;

        if (testRunnable != null) {
            handler.removeCallbacks(testRunnable);
            testRunnable = null;
        }

        Log.d("BluetoothTestSimulator", "Test mode stopped");
    }

    public void cleanup() {
        stopTest();
        handler.removeCallbacksAndMessages(null);
        listener = null; // Clear reference
        Log.d("BluetoothTestSimulator", "Resources cleaned up");
    }

    private String generateTestDataJson() {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("temperature", getRandomTemperature());
            jsonData.put("salinity", getRandomSalinity());
            jsonData.put("ph", getRandompH());
            jsonData.put("moisture", getRandomMoisture());
            jsonData.put("nitrogen", getRandomNPKValue());
            jsonData.put("phosphorus", getRandomNPKValue());
            jsonData.put("potassium", getRandomNPKValue());

            return jsonData.toString();
        } catch (JSONException e) {
            Log.e("BluetoothTestSimulator", "Error generating test JSON", e);
            return "{}";
        }
    }

    private float getRandomTemperature() {
        return new Random().nextFloat() * 20.0f + 15.0f; // 15-35°C
    }

    private int getRandomSalinity() {
        return new Random().nextInt(1501) + 500; // 500-2000
    }

    private float getRandompH() {
        return new Random().nextFloat() * 5.0f + 4.0f; // 4.0-9.0
    }

    private int getRandomMoisture() {
        return new Random().nextInt(101); // 0-100%
    }

    private int getRandomNPKValue() {
        return new Random().nextInt(21); // 0-20 for each nutrient
    }

    public boolean isTesting() {
        return isTesting;
    }
}