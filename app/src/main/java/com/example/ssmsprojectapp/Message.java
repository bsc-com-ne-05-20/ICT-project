package com.example.ssmsprojectapp;

// Message.java
// Message.java

public class Message {
    private int id;
    private String content;
    private String senderName;
    private String senderId;
    private long timestamp;
    private boolean isSentByMe;

    public Message(String content, String senderName, String senderId, long timestamp, boolean isSentByMe) {
        this.content = content;
        this.senderName = senderName;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public String getSenderName() { return senderName; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    public boolean isSentByMe() { return isSentByMe; }

    public boolean isUser() {
        return isSentByMe;
    }
}
