package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AgriChatbot extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private ImageButton sendButton;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();

    // In a real app, you would get this from your authentication system
    private final String currentUserId = "user1";

    // Update with your Render URL
    private static final String RENDER_URL = "https://shimschat-13.onrender.com/ask";
    private static final int TIMEOUT = 10000; // 10 seconds

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activityy_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        initializeViews();
        setupRecyclerView();
        setupMessageInput();
        loadMessages();

    }

    private void initializeViews() {
        messagesRecyclerView = findViewById(R.id.messages_recycler_view);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages, currentUserId);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);
    }

    private void setupMessageInput() {
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        // In a real app, you would load messages from your database or API
        // This is just sample data
        messages.add(new Message("Hello! Ask me anything about soil health!", "assistant", "user2",
                System.currentTimeMillis() - 3600000, false));

        messageAdapter.updateMessages(messages);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
    //corne
    private void sendMessage() {
        String query = messageInput.getText().toString().trim();
        if (!query.isEmpty()) {
            long timestamp = Calendar.getInstance().getTimeInMillis();
            Message newMessage = new Message(
                    query,
                    "You",
                    currentUserId,
                    timestamp,
                    true);

            messages.add(newMessage);
            messageAdapter.updateMessages(messages);
            messageInput.setText("");
            scrollToBottom();

            // send query to assistant
            new AgriGPTTask().execute(query);
        }

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

                            //chat response
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

        //reponse here
        @Override
        protected void onPostExecute(String response) {
            long timestamp = Calendar.getInstance().getTimeInMillis();
            Message replyMessage = new Message(response
                    ,
                    "Assistant",
                    "user2",
                    timestamp,
                    false);

            runOnUiThread(() -> {
                messages.add(replyMessage);
                messageAdapter.updateMessages(messages);
                scrollToBottom();
            });
        }
    }
}