package com.example.fundimtaa;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDialog extends Dialog {
    private String jobId;

    private String userId;
    private FirebaseFirestore db;

    public ReportDialog(@NonNull Context context, String jobId,String userId) {
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
            Report report = new Report(userId,jobId, reason, System.currentTimeMillis());

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
        private String userId; // Add userId field
        private String jobId;
        private String reason;
        private long timestamp;

        public Report() {
            // Firestore requires a no-arg constructor
        }

        public Report(String userId,String jobId, String reason, long timestamp) {
            this.userId = userId;
            this.jobId = jobId;
            this.reason = reason;
            this.timestamp = timestamp;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        // Getters and setters
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

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
