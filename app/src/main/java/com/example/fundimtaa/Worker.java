package com.example.fundimtaa;
import com.google.firebase.Timestamp;
public class Worker {
    private String workerId;
    private String  name;
    private String location;
    private String phoneNumber;
    private String dateOfApplication;
    private String experience;
    private Timestamp timestamp;

    public Worker(String workerId, String name, String location, String phoneNumber, String dateOfApplication, String experience,Timestamp timestamp) {
    }
    public Worker(String workerId, String name, String location, String phoneNumber, String dateOfApplication, String experience) {
        this.workerId = workerId;
        this.name = name;
        this.location = location;
        this.phoneNumber = phoneNumber;
        this.dateOfApplication = dateOfApplication;
        this.experience = experience;
    }



    public String getWorkerId(){return  workerId;}

    public String getName() {
        return name;
    }

    public String getDateOfApplication() {
        return dateOfApplication;
    }

    public String getExperience() {
        return experience;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setExperience(String experience) {
        this.experience = experience;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

