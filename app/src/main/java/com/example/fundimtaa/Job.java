package com.example.fundimtaa;
public class Job {
    private String jobId;
    private String clientId;
    private String jobName;
    private String startDate;
    private String minExperience;
    private  String location;
    private String price;
   private String jobDescription;

    public Job(String jobId,String clientId, String jobName, String startDate, String minExperience, String location, String price,String jobDescription) {
        this.jobId = jobId;
        this.clientId = clientId;
        this.jobName = jobName;
        this.startDate = startDate;
        this.minExperience = minExperience;
        this.location = location;
        this.price = price;
        this.jobDescription = jobDescription;
    }

    // Getter methods
    public String getJobId() {
        return jobId;
    }
    public String getClientId(){return clientId;}
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
public void setClientId(String clientId){
this.clientId = clientId;}
    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getJobDescription() {
        return jobDescription;
    }
}

