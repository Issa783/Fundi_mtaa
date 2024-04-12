package com.example.fundimtaa;
public class Job {
    private String jobId;
    private String jobName;
    private String startDate;
    private String minExperience;
    private  String location;
    private String price;


    public Job(String jobId, String jobName, String startDate, String minExperience, String location, String price) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.startDate = startDate;
        this.minExperience = minExperience;
        this.location = location;
        this.price = price;
    }

    // Getter methods
    public String getJobId() {
        return jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getMinExperience() {
        return minExperience;
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
}

