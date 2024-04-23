package com.example.fundimtaa;
import androidx.appcompat.widget.SearchView;

import android.content.Context; // Add import for Context
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_view_jobs);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerViewJobs = findViewById(R.id.recyclerWorkerViewJobs);
        recyclerViewJobs.setHasFixedSize(true);
        recyclerViewJobs.setLayoutManager(new LinearLayoutManager(this));

        // Initialize job list
        jobList = new ArrayList<>();

        // Initialize adapter
        jobAdapter = new JobAdapter(this, jobList); // Pass 'this' as the Context

        // Set adapter to RecyclerView
        recyclerViewJobs.setAdapter(jobAdapter);

        // Initialize search field
        searchView = findViewById(R.id.searchView);

        // Set up text change listener for search field
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Fetch jobs based on the input text
                fetchJobsStartingWith(newText.trim());
                return true;
            }
        });

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
        db.collection("jobs")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        jobList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String jobId = document.getId();
                            String clientId = document.getString("clientId");
                            String jobName = document.getString("jobName");
                            String jobStartDate = document.getString("jobStartDate");
                            String minExperience = document.getString("minExperience");
                            String location = document.getString("location");
                            String price = document.getString("price");
                            String jobDescription = document.getString("jobDescription");
                            Job job = new Job(jobId, clientId, jobName, jobStartDate, minExperience, location, price, jobDescription);
                            jobList.add(job);
                        }
                        jobAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(WorkerViewJobs.this, "Failed to load jobs: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchJobsStartingWith(String searchText) {
        List<Job> filteredList = new ArrayList<>();
        for (Job job : jobList) {
            if (job.getJobName().toLowerCase().startsWith(searchText.toLowerCase())) {
                filteredList.add(job);
            }
        }
        jobAdapter.setJobs(filteredList);
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
        private Context context;

        public JobAdapter(Context context, List<Job> jobList) {
            this.jobList = jobList;
            this.context = context;
        }

        public void setJobs(List<Job> jobs) {
            this.jobList = jobs;
            notifyDataSetChanged();
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
                // Start JobDetailsActivity and pass the job details
                Intent intent = new Intent(context, JobDetailsActivity.class);
                intent.putExtra("jobName", job.getJobName());
                intent.putExtra("jobStartDate", job.getStartDate());
                intent.putExtra("minExperience", job.getMinExperience());
                intent.putExtra("location", job.getLocation());
                intent.putExtra("price", job.getPrice());
                intent.putExtra("jobDescription", job.getJobDescription());
                context.startActivity(intent);
            });

            // Set OnClickListener for the Apply button
            holder.buttonViewApply.setOnClickListener(v -> {
                // Handle apply button click
                // Retrieve the job ID for the selected job
                String jobId = job.getJobId();

                // Start ApplyJobActivity and pass the job details
                Intent intent = new Intent(context, ApplyJobActivity.class);
                intent.putExtra("jobId", jobId);
                intent.putExtra("jobName", job.getJobName());
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return jobList.size();
        }
    }
}
