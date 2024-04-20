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
        String userId = getCurrentUserId(); // Implement this method to get the current client's ID

        // Query Firestore to fetch jobs posted by the current client
        db.collection("jobs")
                .whereEqualTo("userId", userId)
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
                            Job job = new Job(jobId, userId, jobName, jobStartDate, minExperience, location, price, jobDescription);
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewJobsActivity.this, "Failed to load jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Implement this method to retrieve the client's ID
    private String getCurrentUserId() {
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
        Button buttonManage;
        Button buttonViewWorkers;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewPublishedOn = itemView.findViewById(R.id.textViewPublishedOn);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonManage = itemView.findViewById(R.id.buttonManage);
            buttonViewWorkers = itemView.findViewById(R.id.buttonViewWorkers);
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
            holder.textViewPublishedOn.setText("Published on: " + job.getStartDate());
            holder.textViewPrice.setText("Price: " + job.getPrice());

            // Set OnClickListener for the Manage button
            holder.buttonManage.setOnClickListener(v -> {
                // Handle manage button click
                // You can implement the logic to open the post job activity here
                Toast.makeText(ViewJobsActivity.this, "Manage button clicked for job: " + job.getJobName(), Toast.LENGTH_SHORT).show();
            });

            // Set OnClickListener for the View Workers button
            // Set OnClickListener for the View Workers button
            holder.buttonViewWorkers.setOnClickListener(v -> {
                // Handle view workers button clic

                // Retrieve the job ID for the selected job
                String jobId = job.getJobId();

                // Start ViewWorkersActivity and pass the jobId to it
                Intent intent = new Intent(ViewJobsActivity.this, ViewApplicants.class);
                intent.putExtra("jobId", jobId);
                startActivity(intent);
            });

        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }
}
