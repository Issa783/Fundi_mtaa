package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewJobsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerViewJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_jobs);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerViewJobs = findViewById(R.id.recyclerViewJobs);
        recyclerViewJobs.setHasFixedSize(true);
        recyclerViewJobs.setLayoutManager(new LinearLayoutManager(this));

        // Initialize job list
        jobList = new ArrayList<>();

        // Initialize adapter
        jobAdapter = new JobAdapter(jobList);

        // Set adapter to RecyclerView
        recyclerViewJobs.setAdapter(jobAdapter);
        ImageView imageViewBackArrow = findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                onBackPressed();
            }
        });

        // Load jobs from Firestore
        loadJobs();
    }

    private void loadJobs() {
        // Retrieve current client ID
        String clientId = getCurrentClientId(); // Implement this method to get the current client's ID
        // Query Firestore to fetch jobs posted by the current client, ordered by timestamp
        db.collection("jobs")
                .whereEqualTo("clientId", clientId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp descending
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String jobId = document.getId(); // Get the document ID as the jobId
                            String jobName = document.getString("jobName");
                            String jobStartDate = document.getString("jobStartDate");
                            String minExperience = document.getString("minExperience");
                            String location = document.getString("location");
                            String price = document.getString("price");
                            String jobDescription = document.getString("jobDescription");
                            Boolean rejected = document.getBoolean("rejected");
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            String workerId = document.getString("workerId");
                            Boolean isAssignedBoolean = document.getBoolean("isAssigned");
                            boolean isAssigned = (isAssignedBoolean != null) ? isAssignedBoolean : false; // Handle null Boolean

                            Job job = new Job(jobId, clientId, null, jobName, jobStartDate, minExperience, location, price, jobDescription, false,false, timestamp, workerId, isAssigned);
                            // Set the document ID to the Job object
                            job.setDocumentId(document.getId());

                            // Add the Job object to the list
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewJobsActivity.this, "Failed to load jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to delete a job from Firestore
    private void deleteJob(Job job) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("jobs").document(job.getDocumentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Job deleted successfully
                    Toast.makeText(ViewJobsActivity.this, "Job deleted successfully", Toast.LENGTH_SHORT).show();
                    // Remove the job from the RecyclerView's data source
                    jobList.remove(job);
                    jobAdapter.notifyDataSetChanged(); // Notify the adapter of the change
                })
                .addOnFailureListener(e -> {
                    // Failed to delete job
                    Toast.makeText(ViewJobsActivity.this, "Failed to delete job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Implement this method to retrieve the client's ID
    private String getCurrentClientId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }


    // ViewHolder for the RecyclerView
    private static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView textViewJobName;
        TextView textViewPublishedOn;
        TextView textViewPrice;
        Button buttonDelete;
        Button buttonViewWorkers;
        Button buttonViewJobDetails;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewPublishedOn = itemView.findViewById(R.id.textViewPublishedOn);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonViewWorkers = itemView.findViewById(R.id.buttonViewWorkers);
            buttonViewJobDetails = itemView.findViewById(R.id.buttonViewJobDetails);
        }
    }

    // Adapter for the RecyclerView
    private class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {

        private List<Job> jobList;

        public JobAdapter(List<Job> jobList) {
            this.jobList = jobList;
        }

        @Override
        public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.textViewJobName.setText("Job Name: " + job.getJobName());
            holder.textViewPublishedOn.setText("Job Start Date: " + job.getJobStartDate());
            holder.textViewPrice.setText("Price: " + job.getPrice());


            holder.buttonDelete.setOnClickListener(v -> {
                deleteJob(job);
            });

            holder.buttonViewWorkers.setOnClickListener(v -> {
                String jobId = job.getJobId();
                String documentId = job.getDocumentId();

                // Start ViewWorkersActivity and pass the jobId to it
                Intent intent = new Intent(ViewJobsActivity.this, ViewApplicants.class);
                intent.putExtra("jobId", jobId);
                intent.putExtra("documentId", documentId);
                intent.putExtra("jobName", job.getJobName());
                intent.putExtra("jobStartDate", job.getJobStartDate());
                intent.putExtra("minExperience", job.getMinExperience());
                intent.putExtra("location", job.getLocation());
                intent.putExtra("price", job.getPrice());
                intent.putExtra("jobDescription", job.getJobDescription());
                intent.putExtra("clientId",job.getClientId());
                startActivity(intent);
            });
            holder.buttonViewJobDetails.setOnClickListener(v -> {
                Intent intent = new Intent(ViewJobsActivity.this, JobDetailsActivity.class);
                intent.putExtra("jobName", job.getJobName());
                intent.putExtra("jobStartDate", job.getJobStartDate());
                intent.putExtra("minExperience", job.getMinExperience());
                intent.putExtra("location", job.getLocation());
                intent.putExtra("price", job.getPrice());
                intent.putExtra("jobDescription", job.getJobDescription());
                startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }
}

