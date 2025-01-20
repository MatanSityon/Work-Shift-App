package com.example.workshiftapp.models;

public class Message {
    private String username;
    private String message;
    private String timestamp;
    private String userPhoto;

    // Empty constructor for Firebase
    public Message() {}

    public Message(String username, String message, String timestamp, String userPhoto) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.userPhoto = userPhoto;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }
}
