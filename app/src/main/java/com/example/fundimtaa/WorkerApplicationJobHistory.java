package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private RecyclerView recyclerViewPendingJobs;
    private RecyclerView recyclerViewCompletedJobs;
    private JobAdapter pendingJobAdapter;
    private JobAdapter completedJobAdapter;
    private List<Job> pendingJobsList;
    private List<Job> completedJobsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_application_history);

        // Initialize RecyclerViews
        recyclerViewPendingJobs = findViewById(R.id.recyclerViewPendingJobs);
        recyclerViewPendingJobs.setLayoutManager(new LinearLayoutManager(this));
        // Initialize lists
        pendingJobsList = new ArrayList<>();


        // Initialize adapters with the appropriate isPending parameter
        pendingJobAdapter = new JobAdapter(pendingJobsList); // For pending jobs



        // Set adapters to RecyclerViews
        recyclerViewPendingJobs.setAdapter(pendingJobAdapter);


        // Retrieve assigned jobs for the worker from Firestore
        retrieveAssignedJobs();
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
        db.collection("AssignedJobs").document(jobId)
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

    private void retrieveAssignedJobs() {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the current user is not null
        if (currentUser != null) {
            String workerId = currentUser.getUid();
            // Query Firestore to fetch assigned pending jobs for the worker
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("AssignedJobs")
                    .whereEqualTo("workerId", workerId)
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pendingJobsList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Job job = document.toObject(Job.class);
                                // Set the documentId for the job
                                job.setDocumentId(document.getId());
                                // Add the pending job to the list
                                pendingJobsList.add(job);
                            }
                            // Notify adapter of data change
                            pendingJobAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            Toast.makeText(WorkerApplicationJobHistory.this, "Failed to retrieve assigned pending jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If currentUser is null, handle the case where the user is not signed in
            Toast.makeText(WorkerApplicationJobHistory.this, "No user signed in", Toast.LENGTH_SHORT).show();
        }

    }
    private void deleteJob(String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs").document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Job deleted successfully
                    Toast.makeText(WorkerApplicationJobHistory.this, "Job marked as done", Toast.LENGTH_SHORT).show();
                    // Remove the deleted job from the list
                    removeDeletedJob(documentId);
                })
                .addOnFailureListener(e -> {
                    // Handle errors while deleting job
                    Toast.makeText(WorkerApplicationJobHistory.this, "Failed to mark job as done: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeDeletedJob(String documentId) {
        // Find the position of the job in the list
        int index = -1;
        for (int i = 0; i < pendingJobsList.size(); i++) {
            if (pendingJobsList.get(i).getDocumentId().equals(documentId)) {
                index = i;
                break;
            }
        }
        // If the job is found, remove it from the list and notify the adapter
        if (index != -1) {
            pendingJobsList.remove(index);
            pendingJobAdapter.notifyItemRemoved(index);
        }
    }



    // Adapter for displaying pending jobs in RecyclerView
    private class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {

        private List<Job> jobList;

        public JobAdapter(List<Job> jobList) {
            this.jobList = jobList;
        }

        @Override
        public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker_application_history, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.bind(job);
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }

    // ViewHolder for displaying job items in RecyclerView
    private class JobViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewJobName;
        private TextView textViewJobStartDate;
        private Button buttonViewJobDetails;
        private  Button buttonViewClientDetails;
        private Button buttonMarkAsDone;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewJobStartDate = itemView.findViewById(R.id.textViewJobDate);
            buttonViewJobDetails = itemView.findViewById(R.id.buttonViewJobDetails);
            buttonViewClientDetails = itemView.findViewById(R.id.buttonViewClientDetails);
            buttonMarkAsDone = itemView.findViewById(R.id.buttonMarkAsDone);
        }

        public void bind(Job job) {
            textViewJobName.setText("Job Name: " + job.getJobName());
            textViewJobStartDate.setText("Job Start Date: " + job.getJobStartDate());

            // Show "Mark as Done" button
            buttonMarkAsDone.setVisibility(View.VISIBLE);
            buttonViewJobDetails.setVisibility(View.VISIBLE);
            buttonViewClientDetails.setVisibility(View.VISIBLE);

            // Set OnClickListener for "Mark as Done" button
            buttonMarkAsDone.setOnClickListener(v -> {
                String documentId = job.getDocumentId();
                deleteJob(documentId);
            });

            // Set OnClickListener for "View Job Details" button
            buttonViewJobDetails.setOnClickListener(v -> {
                retrieveJobDetails(job.getJobId());
            });
            buttonViewClientDetails.setOnClickListener(v -> {
                retrieveClientDetails(job.getJobId());
            });
        }
    }


}