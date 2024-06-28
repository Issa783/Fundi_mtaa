package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class WorkerApplicationJobHistory extends AppCompatActivity {
    private RecyclerView recyclerViewPendingJobs, recyclerViewCompletedJobs, recyclerViewRejectedJobs;
    private JobAdapter pendingJobAdapter, completedJobAdapter, rejectedJobAdapter;
    private List<Job> pendingJobList = new ArrayList<>();
    private List<Job> completedJobList = new ArrayList<>();
    private List<Job> rejectedJobList = new ArrayList<>();
    private FirebaseFirestore db;
    private String workerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_application_history);

        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerViewPendingJobs = findViewById(R.id.recyclerViewPendingJobs);
        recyclerViewCompletedJobs = findViewById(R.id.recyclerViewCompletedJobs);
        recyclerViewRejectedJobs = findViewById(R.id.recyclerViewRejectedJobs);

        recyclerViewPendingJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCompletedJobs.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRejectedJobs.setLayoutManager(new LinearLayoutManager(this));

        pendingJobAdapter = new JobAdapter(pendingJobList, "Pending");
        completedJobAdapter = new JobAdapter(completedJobList, "Completed");
        rejectedJobAdapter = new JobAdapter(rejectedJobList, "Rejected");

        recyclerViewPendingJobs.setAdapter(pendingJobAdapter);
        recyclerViewCompletedJobs.setAdapter(completedJobAdapter);
        recyclerViewRejectedJobs.setAdapter(rejectedJobAdapter);

        fetchJobs();
    }




    private void retrieveClientDetails(String jobId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Client details exist in the AssignedJobs document
                        String clientName = documentSnapshot.getString("clientName");
                        String clientEmail = documentSnapshot.getString("clientEmail");
                        String clientPhoneNumber = documentSnapshot.getString("clientPhoneNumber");
                        String clientLocation = documentSnapshot.getString("clientLocation");

                        // Pass client details to a new activity or dialog
                        Intent intent = new Intent(WorkerApplicationJobHistory.this, ViewClientProfileActivity.class);
                        intent.putExtra("clientName", clientName);
                        intent.putExtra("clientEmail", clientEmail);
                        intent.putExtra("clientPhoneNumber", clientPhoneNumber);
                        intent.putExtra("clientLocation", clientLocation);
                        startActivity(intent);
                    } else {
                        // Client details not found in the AssignedJobs document
                        Toast.makeText(WorkerApplicationJobHistory.this, "Client details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    Toast.makeText(WorkerApplicationJobHistory.this, "Failed to retrieve client details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    // Method to retrieve additional job details from Firestore
    private void retrieveJobDetails(String jobId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jobs").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Job document exists, retrieve additional details
                        Job job = documentSnapshot.toObject(Job.class);
                        if (job != null) {
                            // Pass job details to the JobDetailsActivity
                            Intent intent = new Intent(WorkerApplicationJobHistory.this, JobDetailsActivity.class);
                            intent.putExtra("jobName", job.getJobName());
                            intent.putExtra("jobStartDate", job.getJobStartDate());
                            intent.putExtra("minExperience", job.getMinExperience());
                            intent.putExtra("location", job.getLocation());
                            intent.putExtra("price", job.getPrice());
                            intent.putExtra("jobDescription", job.getJobDescription());
                            startActivity(intent);
                        } else {
                            // Handle case where job object is null
                            Toast.makeText(WorkerApplicationJobHistory.this, "Failed to retrieve job details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Job document does not exist
                        Toast.makeText(WorkerApplicationJobHistory.this, "Job details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    Toast.makeText(WorkerApplicationJobHistory.this, "Failed to retrieve job details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void fetchJobs() {
        // Fetch assigned jobs
        db.collection("AssignedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Job job = document.toObject(Job.class);
                            if (job.isCompleted()) {
                                completedJobList.add(job);
                            } else {
                                pendingJobList.add(job);
                            }
                        }
                        pendingJobAdapter.notifyDataSetChanged();
                        completedJobAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("WorkerApplicationJobHistory", "Error fetching assigned jobs: ", task.getException());
                    }
                });

        // Fetch rejected jobs
        db.collection("RejectedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Job job = document.toObject(Job.class);
                            rejectedJobList.add(job);
                        }
                        rejectedJobAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("WorkerApplicationJobHistory", "Error fetching rejected jobs: ", task.getException());
                    }
                });
    }
    class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
        private List<Job> jobList;
        private String sectionType;

        JobAdapter(List<Job> jobList, String sectionType) {
            this.jobList = jobList;
            this.sectionType = sectionType;
        }

        @NonNull
        @Override
        public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker_application_history, parent, false);
            return new JobViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.textViewJobName.setText("Job Name: " + job.getJobName());
            holder.textViewJobStartDate.setText("Job Start Date: " + job.getJobStartDate());

            holder.buttonViewJobDetails.setOnClickListener(v -> {
                retrieveJobDetails(job.getJobId());
            });

            holder.buttonViewClientDetails.setOnClickListener(v -> {
                retrieveClientDetails(job.getJobId());
            });

            if (sectionType.equals("Pending")) {
                holder.buttonMarkAsDone.setVisibility(View.VISIBLE);
                holder.buttonMarkAsDone.setOnClickListener(v -> markJobAsDone(job));
            } else {
                holder.buttonMarkAsDone.setVisibility(View.GONE);
            }

            if (sectionType.equals("Rejected")) {
                holder.buttonViewClientDetails.setVisibility(View.GONE);
                holder.buttonMarkAsDone.setVisibility(View.GONE);
                holder.buttonViewJobDetails.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }

        class JobViewHolder extends RecyclerView.ViewHolder {
            TextView textViewJobName, textViewJobStartDate;
            Button buttonViewJobDetails, buttonViewClientDetails, buttonMarkAsDone;

            JobViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewJobName = itemView.findViewById(R.id.textViewJobName);
                textViewJobStartDate = itemView.findViewById(R.id.textViewPublishedOn);
                buttonViewJobDetails = itemView.findViewById(R.id.buttonViewJobDetails);
                buttonViewClientDetails = itemView.findViewById(R.id.buttonViewClientDetails);
                buttonMarkAsDone = itemView.findViewById(R.id.buttonMarkAsDone);
            }
        }

        private void markJobAsDone(Job job) {
            job.setCompleted(true);
            FirebaseFirestore.getInstance().collection("AssignedJobs")
                    .document(job.getJobId())
                    .update("completed", true)
                    .addOnSuccessListener(aVoid -> {
                        pendingJobList.remove(job);
                        completedJobList.add(job);
                        notifyDataSetChanged();
                        updateAssignedJobsCount(job.getWorkerId(), -1);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("JobAdapter", "Failed to mark job as done: " + e.getMessage());
                    });
        }

        private void updateAssignedJobsCount(String workerId, int delta) {
            FirebaseFirestore.getInstance().collection("AssignedJobsCount")
                    .document(workerId)
                    .update("numberOfAssignedJobs", FieldValue.increment(delta))
                    .addOnSuccessListener(aVoid -> {
                        Log.d("JobAdapter", "Updated number of assigned jobs for worker " + workerId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("JobAdapter", "Failed to update number of assigned jobs: " + e.getMessage());
                    });
        }
    }




}
