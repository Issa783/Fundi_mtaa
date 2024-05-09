package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewProfileWorkerActivity extends AppCompatActivity {
    private static final String TAG = ViewProfileWorkerActivity.class.getSimpleName();

    private LinearLayout layoutRatingsReviews;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_worker);
        mAuth = FirebaseAuth.getInstance();
        String workerId = getIntent().getStringExtra("workerId");

        // Initialize layoutRatingsReviews LinearLayout
        layoutRatingsReviews = findViewById(R.id.layoutRatingsReviews);

        // Query Firestore to fetch ratings and reviews for jobs assigned to this worker
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract job details from AssignedJobs collection
                            String jobName = document.getString("jobName");
                            float rating = document.getDouble("rating").floatValue();
                            String review = document.getString("review");

                            // Update TextViews with job details
                            TextView textViewJobName = new TextView(ViewProfileWorkerActivity.this);
                            textViewJobName.setText("Job Name: " + jobName);

                            TextView textViewReview = new TextView(ViewProfileWorkerActivity.this);
                            textViewReview.setText("Review: " + review);

                            // Create RatingBar to display rating
                            RatingBar ratingBar = new RatingBar(ViewProfileWorkerActivity.this, null, android.R.attr.ratingBarStyleSmall);
                            ratingBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)); // Set layout parameters
                            ratingBar.setRating(rating);
                            ratingBar.setNumStars(5);
                            ratingBar.setStepSize(1.0f);
                            ratingBar.setIsIndicator(true);

                            // Add views to layoutRatingsReviews LinearLayout
                            layoutRatingsReviews.addView(textViewJobName);
                            layoutRatingsReviews.addView(ratingBar);
                            layoutRatingsReviews.addView(textViewReview);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch job ratings and reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching job ratings and reviews: ", task.getException());
                    }
                });

        // Retrieve worker's details from the job_applications collection
        db.collection("job_applications")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract worker details from job_applications collection
                            String workerName = document.getString("name");
                            String phoneNumber = document.getString("phoneNumber");
                            String location = document.getString("location");
                            String experience = document.getString("experience");

                            // Update TextViews with worker's details
                            TextView textViewName = findViewById(R.id.textViewName);
                            textViewName.setText("Worker Name: " + workerName);

                            TextView textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
                            textViewPhoneNumber.setText("Phone Number: " + phoneNumber);

                            TextView textViewLocation = findViewById(R.id.textViewLocation);
                            textViewLocation.setText("Location: " + location);

                            TextView textViewExperience = findViewById(R.id.textViewExperience);
                            textViewExperience.setText("Work Experience: " + experience);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch worker details.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching worker details: ", task.getException());
                    }
                });
    }
}
