package com.example.fundimtaa;

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
import com.google.firebase.firestore.FirebaseFirestore;
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
        recyclerViewCompletedJobs = findViewById(R.id.recyclerViewCompletedJobs);
        recyclerViewCompletedJobs.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists
        pendingJobsList = new ArrayList<>();
        completedJobsList = new ArrayList<>();

        // Initialize adapters with the appropriate isPending parameter
        pendingJobAdapter = new JobAdapter(pendingJobsList, true); // For pending jobs
        completedJobAdapter = new JobAdapter(completedJobsList, false); // For completed jobs


        // Set adapters to RecyclerViews
        recyclerViewPendingJobs.setAdapter(pendingJobAdapter);
        recyclerViewCompletedJobs.setAdapter(completedJobAdapter);

        // Retrieve assigned jobs for the worker from Firestore
        retrieveAssignedJobs();
    }

    // Add a method to update the job status in Firestore
    private void updateJobStatus(Job job, String documentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs").document(documentId)
                .update("completed", true)
                .addOnSuccessListener(aVoid -> {
                    // Job status updated successfully
                    // Now, remove the job from the pending list
                    pendingJobsList.remove(job);
                    // Notify the adapter of data change
                    pendingJobAdapter.notifyDataSetChanged();
                    // Add the job to the completed list
                    completedJobsList.add(job);
                    // Notify the adapter of data change
                    completedJobAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle errors while updating job status
                    Toast.makeText(WorkerApplicationJobHistory.this, "Failed to mark job as done: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void retrieveAssignedJobs() {
        // Get the current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Check if the current user is not null
        if (currentUser != null) {
            String workerId = currentUser.getUid();
            // Query Firestore to fetch assigned jobs for the worker
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("AssignedJobs")
                    .whereEqualTo("workerId", workerId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            pendingJobsList.clear();
                            completedJobsList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Job job = document.toObject(Job.class);
                                // Set the documentId for the job
                                job.setDocumentId(document.getId());
                                // Differentiate between pending and completed jobs based on their status
                                if (job.isCompleted()) {
                                    completedJobsList.add(job);
                                } else {
                                    pendingJobsList.add(job);
                                }
                            }
                            // Notify adapters of data change
                            pendingJobAdapter.notifyDataSetChanged();
                            completedJobAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            Toast.makeText(WorkerApplicationJobHistory.this, "Failed to retrieve assigned jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If currentUser is null, handle the case where the user is not signed in
            Toast.makeText(WorkerApplicationJobHistory.this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }


    // Adapter for displaying jobs in RecyclerView
    private class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {

        private List<Job> jobList;
        private boolean isPending; // Flag to differentiate between pending and completed jobs

        public JobAdapter(List<Job> jobList, boolean isPending) {
            this.jobList = jobList;
            this.isPending = isPending;
        }

        @Override
        public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_worker_application_history, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.bind(job, isPending);
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
        private Button buttonMarkAsDone;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewJobStartDate = itemView.findViewById(R.id.textViewJobDate);
            buttonViewJobDetails = itemView.findViewById(R.id.buttonViewJobDetails);
            buttonMarkAsDone = itemView.findViewById(R.id.buttonMarkAsDone);
        }

        public void bind(Job job, boolean isPending) {
            textViewJobName.setText("Job Name: " + job.getJobName());
            textViewJobStartDate.setText("Job Start Date: " + job.getJobStartDate());

            if (isPending) {
                // If the job is pending, show both buttons
                buttonMarkAsDone.setVisibility(View.VISIBLE);
                buttonViewJobDetails.setVisibility(View.VISIBLE);

                // Set OnClickListener for "Mark as Done" button
                buttonMarkAsDone.setOnClickListener(v -> {
                    String documentId = job.getDocumentId();
                    updateJobStatus(job, documentId);
                });

                // Set OnClickListener for "View Job Details" button
                buttonViewJobDetails.setOnClickListener(v -> {
                    // Handle "View Job Details" button click
                    // You can implement the logic to view job details here
                    Toast.makeText(itemView.getContext(), "View Job Details for " + job.getJobName(), Toast.LENGTH_SHORT).show();
                });
            } else {
                // If the job is completed, hide "Mark as Done" button and only show "View Job Details" button
                buttonMarkAsDone.setVisibility(View.GONE);
                buttonViewJobDetails.setVisibility(View.VISIBLE);

                // Set OnClickListener for "View Job Details" button
                buttonViewJobDetails.setOnClickListener(v -> {
                    // Handle "View Job Details" button click
                    // You can implement the logic to view job details here
                    Toast.makeText(itemView.getContext(), "View Job Details for " + job.getJobName(), Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

}
