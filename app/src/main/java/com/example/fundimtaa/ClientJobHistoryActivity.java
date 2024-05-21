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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ClientJobHistoryActivity extends AppCompatActivity {
    private static final String TAG = ClientJobHistoryActivity.class.getSimpleName();

    private RecyclerView recyclerViewClientJobHistory;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_job_history);

        // Initialize RecyclerView
        recyclerViewClientJobHistory = findViewById(R.id.recyclerClientJobHistory);
        recyclerViewClientJobHistory.setLayoutManager(new LinearLayoutManager(this));

        // Initialize job list and adapter
        jobList = new ArrayList<>();
        jobAdapter = new JobAdapter(jobList);
        recyclerViewClientJobHistory.setAdapter(jobAdapter);

        // Load assigned jobs for the current client
        loadAssignedJobsForClient();
    }
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
                            Intent intent = new Intent(ClientJobHistoryActivity.this, JobDetailsActivity.class);
                            intent.putExtra("jobName", job.getJobName());
                            intent.putExtra("jobStartDate", job.getJobStartDate());
                            intent.putExtra("minExperience", job.getMinExperience());
                            intent.putExtra("location", job.getLocation());
                            intent.putExtra("price", job.getPrice());
                            intent.putExtra("jobDescription", job.getJobDescription());
                            startActivity(intent);
                        } else {
                            // Handle case where job object is null
                            Toast.makeText(ClientJobHistoryActivity.this, "Failed to retrieve job details", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Job document does not exist
                        Toast.makeText(ClientJobHistoryActivity.this, "Job details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle Firestore query failure
                    Toast.makeText(ClientJobHistoryActivity.this, "Failed to retrieve job details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void loadAssignedJobsForClient() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String clientId = currentUser.getUid();

            // Query Firestore to fetch assigned jobs for the client
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("AssignedJobs")
                    .whereEqualTo("clientId", clientId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            jobList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Job job = document.toObject(Job.class);
                                jobList.add(job);
                            }
                            jobAdapter.notifyDataSetChanged();
                        } else {
                            // Handle errors
                            Toast.makeText(ClientJobHistoryActivity.this, "Failed to retrieve assigned jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // If currentUser is null, handle the case where the user is not signed in
            Toast.makeText(ClientJobHistoryActivity.this, "No user signed in", Toast.LENGTH_SHORT).show();
        }
    }


    public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {

        private List<Job> jobList;

        public JobAdapter(List<Job> jobList) {
            this.jobList = jobList;
        }

        @NonNull
        @Override
        public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_job_history, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.bind(job);
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }

        public class JobViewHolder extends RecyclerView.ViewHolder {

            private TextView textViewJobName;
            private TextView textViewJobStartDate;
            private Button buttonViewDetails;
            private Button buttonRateAndReview;

            public JobViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewJobName = itemView.findViewById(R.id.textViewJobName);
                textViewJobStartDate = itemView.findViewById(R.id.textViewJobStartDate);
                buttonViewDetails = itemView.findViewById(R.id.buttonViewDetails);
                buttonRateAndReview = itemView.findViewById(R.id.buttonRateAndReview);
            }

            public void bind(Job job) {
                textViewJobName.setText("Job Name: " + job.getJobName());
                textViewJobStartDate.setText("Job Start Date: " + job.getJobStartDate());

                buttonViewDetails.setOnClickListener(v -> {
                    // Retrieve job details from Firestore and start JobDetailsActivity
                    retrieveJobDetails(job.getJobId());

                });
                buttonRateAndReview.setOnClickListener(v -> {
                    // Create an Intent to navigate to the rating and review activity
                    Intent intent = new Intent(ClientJobHistoryActivity.this, RatingAndReviewActivity.class);
                    intent.putExtra("jobId", job.getJobId());
                    intent.putExtra("jobName", job.getJobName());
                    // Start the activity
                    startActivity(intent);
                });

            }
        }
    }
}
