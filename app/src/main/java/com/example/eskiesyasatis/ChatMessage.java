package com.example.eskiesyasatis;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String senderId;
    private String text;
    private Timestamp timestamp;

    public ChatMessage() {}

    public ChatMessage(String senderId, String text, Timestamp timestamp) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
