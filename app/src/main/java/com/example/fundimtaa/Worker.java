package com.example.fundimtaa;

public class Worker {
    private String workerId;
    private String  name;
    private String location;
    private String phoneNumber;
    private String dateOfApplication;
    private String experience;

    public Worker(String workerId,String name,String dateOfApplication,String experience,String location,
    String phoneNumber) {
        this.workerId = workerId;
        this.name = name;
        this.dateOfApplication = dateOfApplication;
        this.experience =experience;
        this.location = location;
        this.phoneNumber = phoneNumber;

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
}
