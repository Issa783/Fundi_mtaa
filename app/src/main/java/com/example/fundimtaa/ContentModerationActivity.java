package com.example.fundimtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ContentModerationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewReportedContent;
    private ReportedContentAdapter reportedContentAdapter;
    private List<ReportDialog.Report> reportedContentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_moderation);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerViewReportedContent = findViewById(R.id.recyclerReportedContent);
        recyclerViewReportedContent.setHasFixedSize(true);
        recyclerViewReportedContent.setLayoutManager(new LinearLayoutManager(this));

        // Initialize reported content list
        reportedContentList = new ArrayList<>();

        // Initialize adapter
        reportedContentAdapter = new ReportedContentAdapter(reportedContentList);

        // Set adapter to RecyclerView
        recyclerViewReportedContent.setAdapter(reportedContentAdapter);

        // Load reported content from Firestore
        loadReportedContent();
    }

    private void loadReportedContent() {
        db.collection("reported_content")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        reportedContentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String jobId = document.getString("jobId");
                            String userId = document.getString("userId");
                            String reason = document.getString("reason");
                            long timestamp = document.getLong("timestamp");
                            ReportDialog.Report report = new ReportDialog.Report(userId,jobId, reason, timestamp);
                            reportedContentList.add(report);
                        }
                        reportedContentAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ContentModerationActivity.this, "Failed to load reported content: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class ReportedContentAdapter extends RecyclerView.Adapter<ReportedContentAdapter.ReportViewHolder> {
        private List<ReportDialog.Report> reportList;

        public ReportedContentAdapter(List<ReportDialog.Report> reportList) {
            this.reportList = reportList;
        }

        @NonNull
        @Override
        public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reported_content, parent, false);
            return new ReportViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
            ReportDialog.Report report = reportList.get(position);
            holder.textViewJobId.setText("Job ID: " + report.getJobId());
            holder.textViewReason.setText("Reason: " + report.getReason());
            holder.textViewTimestamp.setText("Timestamp: " + report.getTimestamp());

            holder.buttonDismiss.setOnClickListener(v -> {
                // Dismiss the report
                dismissReport(report);
            });

            holder.buttonRemoveJob.setOnClickListener(v -> {
                // Remove the reported job
                removeJob(report);
            });
        }

        @Override
        public int getItemCount() {
            return reportList.size();
        }

        private class ReportViewHolder extends RecyclerView.ViewHolder {
            TextView textViewJobId;
            TextView textViewReason;
            TextView textViewTimestamp;
            Button buttonDismiss;
            Button buttonRemoveJob;

            public ReportViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewJobId = itemView.findViewById(R.id.textViewJobId);
                textViewReason = itemView.findViewById(R.id.textViewReason);
                textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
                buttonDismiss = itemView.findViewById(R.id.buttonDismiss);
                buttonRemoveJob = itemView.findViewById(R.id.buttonRemoveJob);
            }
        }

        private void dismissReport(ReportDialog.Report report) {
            // Remove the report from Firestore
            db.collection("reported_content")
                    .whereEqualTo("jobId", report.getJobId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("reported_content").document(document.getId()).delete();
                            }
                            Toast.makeText(ContentModerationActivity.this, "Report dismissed", Toast.LENGTH_SHORT).show();
                            loadReportedContent();
                        } else {
                            Toast.makeText(ContentModerationActivity.this, "Failed to dismiss report: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        private void removeJob(ReportDialog.Report report) {
            // Remove the job from Firestore
            db.collection("jobs").document(report.getJobId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove the report from Firestore after job deletion
                        db.collection("reported_content")
                                .whereEqualTo("jobId", report.getJobId())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            db.collection("reported_content").document(document.getId()).delete();
                                        }
                                        Toast.makeText(ContentModerationActivity.this, "Job and report removed", Toast.LENGTH_SHORT).show();
                                        loadReportedContent();
                                    } else {
                                        Toast.makeText(ContentModerationActivity.this, "Failed to remove report: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(ContentModerationActivity.this, "Failed to remove job: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
