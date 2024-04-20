package com.example.fundimtaa;

public class Worker {
    private String workerId;
    private String  name;
    private String dateOfApplication;
    private String experience;

    public Worker(String workerId,String name,String dateOfApplication,String experience) {
        this.workerId = workerId;
        this.name = name;
        this.dateOfApplication = dateOfApplication;
        this.experience =experience;

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
}
