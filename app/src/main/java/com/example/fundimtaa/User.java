package com.example.fundimtaa;
public class User {
    private String userId;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
    private String clientId; // Added field for clientId
    private String workerId; // Added field for workerId

    // Default constructor (required for Firestore)
    public User() {
        // Default constructor required for Firestore
    }

    public User(String userId, String name, String email, String phoneNumber, String role, String clientId, String workerId) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.clientId = clientId;
        this.workerId = workerId;
    }

    // Getters and setters for all fields
    // You can generate these automatically in most IDEs like Android Studio

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
}
