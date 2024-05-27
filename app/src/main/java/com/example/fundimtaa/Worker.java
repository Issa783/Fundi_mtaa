package com.example.fundimtaa;

import com.google.firebase.Timestamp;

public class Worker {
    private String workerId;
    private String name;
    private String phoneNumber;
    private String location;
    private String dateOfApplication;
    private String experience;
    private Timestamp timestamp;
    private double rating; // Add rating field
    private String review; // Add review field

    // Constructor
    public Worker(String workerId, String name, String phoneNumber, String location, String dateOfApplication, String experience, Timestamp timestamp) {
        this.workerId = workerId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.dateOfApplication = dateOfApplication;
        this.experience = experience;
        this.timestamp = timestamp;
        this.rating = 0; // Default value
        this.review = ""; // Default value
    }

    // Getters and setters
    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDateOfApplication() {
        return dateOfApplication;
    }

    public void setDateOfApplication(String dateOfApplication) {
        this.dateOfApplication = dateOfApplication;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
