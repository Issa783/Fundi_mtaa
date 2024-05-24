package com.example.fundimtaa;

import com.google.firebase.Timestamp;

public class Notification {
    private String title;
    private String body;
    private Timestamp timestamp;  // Use Timestamp type for Firestore timestamps
    private boolean read;
    private String id;  // Assuming you have an ID field

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String title, String body, Timestamp timestamp, boolean read, String id) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.read = read;
        this.id = id;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
