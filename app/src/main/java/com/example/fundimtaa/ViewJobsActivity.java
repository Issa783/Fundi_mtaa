package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        // Load jobs from Firestore
        loadJobs();
    }

    private void loadJobs() {
        db.collection("jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String jobName = document.getString("jobName");
                            String jobStartDate = document.getString("jobStartDate");
                            String minExperience = document.getString("minExperience");
                            Job job = new Job(document.getId(), jobName, jobStartDate, minExperience);
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewJobsActivity.this, "Failed to load jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    // ViewHolder for the RecyclerView
    private static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView textViewJobName;
        TextView textViewPublishedOn;
        TextView textViewMinExperience;
        Button buttonManage;
        Button buttonViewWorkers;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewPublishedOn = itemView.findViewById(R.id.textViewPublishedOn);
            textViewMinExperience = itemView.findViewById(R.id.textViewMinExperience);
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
            holder.textViewMinExperience.setText("Minimum Experience: " + job.getMinExperience());

            // Set OnClickListener for the Manage button
            holder.buttonManage.setOnClickListener(v -> {
                // Handle manage button click
                // You can implement the logic to open the post job activity here
                Toast.makeText(ViewJobsActivity.this, "Manage button clicked for job: " + job.getJobName(), Toast.LENGTH_SHORT).show();
            });

            // Set OnClickListener for the View Workers button
            holder.buttonViewWorkers.setOnClickListener(v -> {
                // Handle view workers button click
                // You can implement the logic to view workers here
                Toast.makeText(ViewJobsActivity.this, "View workers clicked for job: " + job.getJobName(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }
}
