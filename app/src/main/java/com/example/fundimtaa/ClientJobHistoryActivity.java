package com.example.fundimtaa;



import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ClientJobHistoryActivity extends AppCompatActivity {
    private static final String TAG = ClientJobHistoryActivity.class.getSimpleName();
    private Snackbar snackbar;
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
    private void updateRatingAndReviewForJob(Job job, float rating, String review) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming "AssignedJobs" collection has a field for ratings and reviews
        db.collection("AssignedJobs")
                .document(job.getJobId()) // Assuming each job has a unique ID
                .update("rating", rating, "review", review)
                .addOnSuccessListener(aVoid -> {
                    // Rating and review updated successfully
                    Toast.makeText(ClientJobHistoryActivity.this, "Rating and review submitted for " + job.getJobName(), Toast.LENGTH_SHORT).show();
                    // Dismiss the Snackbar after submission
                    dismissSnackbar();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(ClientJobHistoryActivity.this, "Failed to submit rating and review. Please try again.", Toast.LENGTH_SHORT).show();
                    // Log the exception for debugging
                    Log.e(TAG, "Error updating rating and review: ", e);
                });
    }

    // Method to dismiss the Snackbar
    private void dismissSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
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
                    // Handle button click to view job details
                    Toast.makeText(itemView.getContext(), "View Details for " + job.getJobName(), Toast.LENGTH_SHORT).show();
                    // Implement your logic to view job details here
                });
                buttonRateAndReview.setOnClickListener(v -> {
                    // Show a Snackbar for rating and reviewing the job
                    /*Snackbar snackbar = Snackbar.make(v, "Rate this job", Snackbar.LENGTH_INDEFINITE);

                    // Set action to submit the rating and review
                    snackbar.setAction("Submit", v1 -> {
                        // Get the rating from the RatingBar
                        RatingBar ratingBar = (RatingBar) snackbar.getView().findViewById(R.id.ratingBar);
                        float rating = ratingBar.getRating();
                        // Get the review from the EditText
                        EditText editTextReview = (EditText) snackbar.getView().findViewById(R.id.editTextReview);
                        String review = editTextReview.getText().toString();
                        // Update Firestore with the rating and review for this job
                        updateRatingAndReviewForJob(job, rating, review);
                    });

                    // Get the Snackbar view
                    View snackbarView = snackbar.getView();

                    // Inflate a custom layout for the Snackbar content
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    View customSnackbarView = inflater.inflate(R.layout.snackbar_rate_review, null);

                    // Add the custom layout to the Snackbar view
                    ViewGroup snackbarLayout = (ViewGroup) snackbarView;
                    snackbarLayout.addView(customSnackbarView, 0);

                    // Show the Snackbar
                    snackbar.show();*/
                    // Create an Intent to navigate to the rating and review activity
                    Intent intent = new Intent(ClientJobHistoryActivity.this, RatingAndReviewActivity.class);
                    // Pass necessary data to the new activity (e.g., job details)
                    intent.putExtra("jobId", job.getJobId());
                    intent.putExtra("jobName", job.getJobName());
                    // Start the activity
                    startActivity(intent);
                });





            }
        }
    }
}
