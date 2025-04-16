package com.example.ssmsprojectapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AgriChatbot extends AppCompatActivity {
    private TextView chatView;
    private EditText inputField;
    private Button sendButton;

    private boolean isUserSending;
    private boolean isBotResponding;

    // Update with your Render URL
    private static final String RENDER_URL = "https://shimschat-13.onrender.com/ask";
    private static final int TIMEOUT = 10000; // 10 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activityy_main);

        chatView = findViewById(R.id.chatView);
        inputField = findViewById(R.id.inputField);
        sendButton = findViewById(R.id.sendButton);

        // Welcome message
        appendToChat("AgriChat: Hello! Ask me anything about soil health!");
        appendToChat("Type 'exit' or 'quit' to end the conversation.\n");

        sendButton.setOnClickListener(v -> sendMessage());
        inputField.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void sendMessage() {
        String userMessage = inputField.getText().toString().trim();
        if (userMessage.isEmpty()) return;

        appendToChat("You: " + userMessage);
        inputField.setText("");

        if (userMessage.equalsIgnoreCase("exit") || userMessage.equalsIgnoreCase("quit")) {
            appendToChat("AgriChat: Goodbye! Have a great day!");
            return;
        }

        new AgriGPTTask().execute(userMessage);
    }

    private void appendToChat(String message) {
        runOnUiThread(() -> chatView.append(message + "\n\n"));
    }

    private class AgriGPTTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String question = params[0];
            HttpURLConnection conn = null;
            try {
                URL url = new URL(RENDER_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(TIMEOUT);
                conn.setReadTimeout(TIMEOUT);
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("question", question);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        return jsonResponse.getString("response");
                    }
                } else {
                    return "Server error: " + responseCode;
                }
            } catch (Exception e) {
                return "Connection error: " + e.getMessage();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String result) {
            appendToChat("AgriChat: " + result);
        }
    }
}