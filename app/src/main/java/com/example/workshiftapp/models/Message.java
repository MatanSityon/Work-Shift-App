package com.example.workshiftapp.models;

public class Message {
    private String username;
    private String message;
    private long timestamp;

    // **Important**: Empty constructor is needed for Firebase’s automatic data mapping.
    public Message() {
    }

    public Message(String username, String message, long timestamp) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
