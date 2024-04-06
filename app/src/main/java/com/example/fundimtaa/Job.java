package com.example.fundimtaa;
public class Job {
    private String jobId;
    private String jobName;
    private String startDate;
    private String minExperience;

    public Job(String jobId, String jobName, String startDate, String minExperience) {
        this.jobId = jobId;
        this.jobName = jobName;
        this.startDate = startDate;
        this.minExperience = minExperience;
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
}
