package com.example.fundimtaa;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

public class ReportDialog extends Dialog {
    private String jobId;
    private String userId;
    private FirebaseFirestore db;

    public ReportDialog(@NonNull Context context, String jobId, String userId) {
        super(context);
        this.jobId = jobId;
        this.userId = userId;
        db = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_report);

        EditText editTextReportReason = findViewById(R.id.editTextReportReason);
        Button buttonSubmitReport = findViewById(R.id.buttonSubmitReport);

        buttonSubmitReport.setOnClickListener(v -> {
            String reason = editTextReportReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(getContext(), "Please provide a reason", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a report object
            Report report = new Report(userId, jobId, reason, Timestamp.now());

            // Submit the report to Firestore
            db.collection("reported_content")
                    .add(report)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(getContext(), "Report submitted", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to submit report", Toast.LENGTH_SHORT).show();
                    });
        });
    }


    public static class Report {
        private String userId;
        private String jobId;
        private String reason;
        private Timestamp timestamp;

        // Additional fields for job and client details
        private String jobName;
        private String jobDescription;
        private String clientName;
        private String clientEmail;
        private String clientPhone;

        public Report() {
            // Firestore requires a no-arg constructor
        }

        // Constructor with basic details
        public Report(String userId, String jobId, String reason, Timestamp timestamp) {
            this.userId = userId;
            this.jobId = jobId;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        // Constructor with all details
        public Report(String userId, String jobId, String reason, Timestamp timestamp, String jobName, String jobDescription, String clientName, String clientEmail, String clientPhone) {
            this.userId = userId;
            this.jobId = jobId;
            this.reason = reason;
            this.timestamp = timestamp;
            this.jobName = jobName;
            this.jobDescription = jobDescription;
            this.clientName = clientName;
            this.clientEmail = clientEmail;
            this.clientPhone = clientPhone;
        }

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public String getJobName() {
            return jobName;
        }
        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getJobDescription() {
            return jobDescription;
        }

        public void setJobDescription(String jobDescription) {
            this.jobDescription = jobDescription;
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

        public String getClientPhone() {
            return clientPhone;
        }

        public void setClientPhone(String clientPhone) {
            this.clientPhone = clientPhone;
        }
    }
}
