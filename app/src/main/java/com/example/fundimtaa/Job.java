package com.example.fundimtaa;

import com.google.firebase.Timestamp;

public class Job {
    private String jobId;
    private String clientId;
    private String documentId;
    private String jobName;
    private String jobStartDate;
    private String minExperience;
    private String location;
    private String price;
    private String jobDescription;
    private boolean isCompleted;
    private Timestamp timestamp;
    private String workerId;
    private boolean isAssigned; // New field
    private String clientName;
    private String clientEmail;
    private  String clientPhoneNumber;
    private  String clientLocation;

    // Default constructor required by Firestore
    public Job() {
        // Default constructor is necessary for Firestore
    }

    public Job(String jobId, String clientId, String documentId, String jobName,
               String jobStartDate, String minExperience,
               String location, String price, String jobDescription, boolean isCompleted, Timestamp timestamp, String workerId, boolean isAssigned) {
        this.jobId = jobId;
        this.clientId = clientId;
        this.documentId = documentId;
        this.jobName = jobName;
        this.jobStartDate = jobStartDate;
        this.minExperience = minExperience;
        this.location = location;
        this.price = price;
        this.jobDescription = jobDescription;
        this.isCompleted = isCompleted;
        this.timestamp = timestamp;
        this.workerId = workerId;
        this.isAssigned = isAssigned;
    }

    // Getter methods
    public String getJobId() {
        return jobId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobStartDate() {
        return jobStartDate;
    }

    public void setJobStartDate(String jobStartDate) {
        this.jobStartDate = jobStartDate;
    }

    public String getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(String minExperience) {
        this.minExperience = minExperience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhoneNumber() {
        return clientPhoneNumber;
    }

    public void setClientPhoneNumber(String clientPhoneNumber) {
        this.clientPhoneNumber = clientPhoneNumber;
    }

    public String getClientLocation() {
        return clientLocation;
    }

    public void setClientLocation(String clientLocation) {
        this.clientLocation = clientLocation;
    }
    public boolean isAssigned() {
        return isAssigned;
    }

    public void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }




}
