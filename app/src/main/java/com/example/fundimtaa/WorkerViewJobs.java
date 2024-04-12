package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerViewJobs extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerViewJobs;
    private JobAdapter jobAdapter;
    private List<Job> jobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_view_jobs);

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
                            String location = document.getString("location");
                            String price = document.getString("price");
                            Job job = new Job(document.getId(), jobName, jobStartDate, minExperience, location, price);
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(WorkerViewJobs.this, "Failed to load jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ViewHolder for the RecyclerView
    private static class JobViewHolder extends RecyclerView.ViewHolder {
        TextView textViewJobName;
        TextView textViewStartDate;
        TextView textViewPrice;
        Button buttonViewDetails;
        Button buttonViewApply;

        public JobViewHolder(View itemView) {
            super(itemView);
            textViewJobName = itemView.findViewById(R.id.textViewJobName);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            buttonViewDetails = itemView.findViewById(R.id.buttonViewDetails);
            buttonViewApply = itemView.findViewById(R.id.buttonApply);
        }
    }

    private class JobAdapter extends RecyclerView.Adapter<JobViewHolder> {

        private List<Job> jobList;

        public JobAdapter(List<Job> jobList) {
            this.jobList = jobList;
        }

        @Override
        public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_workers, parent, false);
            return new JobViewHolder(view);
        }

        @Override
        public void onBindViewHolder(JobViewHolder holder, int position) {
            Job job = jobList.get(position);
            holder.textViewJobName.setText("Job Name: " + job.getJobName());
            holder.textViewStartDate.setText("Start Date: " + job.getStartDate());
            holder.textViewPrice.setText("Price: " + job.getPrice());

            // Set OnClickListener for the View Details button
            holder.buttonViewDetails.setOnClickListener(v -> {
                // Handle view details button click
                // You can implement the logic to view job details here
                Toast.makeText(WorkerViewJobs.this, "View details clicked for job: " + job.getJobName(), Toast.LENGTH_SHORT).show();
            });

            // Set OnClickListener for the Apply button
            holder.buttonViewApply.setOnClickListener(v -> {
                // Handle apply button click
                // You can implement the logic to apply for the job here
                Toast.makeText(WorkerViewJobs.this, "Apply clicked for job: " + job.getJobName(), Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }
}
