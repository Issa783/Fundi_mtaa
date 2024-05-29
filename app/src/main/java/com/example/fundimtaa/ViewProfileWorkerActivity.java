package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewProfileWorkerActivity extends AppCompatActivity {
    private static final String TAG = ViewProfileWorkerActivity.class.getSimpleName();
    private static final int INITIAL_DISPLAY_LIMIT = 3;

    private LinearLayout layoutRatingsReviews;
    private FirebaseAuth mAuth;
    private int currentDisplayedCount = 0; // To track the number of reviews displayed
    private float totalRating = 0; // To calculate the total rating
    private int ratingCount = 0; // To count the number of ratings
    private Button btnViewMore; // Button to load more reviews
    private Button btnViewLess; // Button to return to initial view
    private int maxDisplayedCount = INITIAL_DISPLAY_LIMIT; // Maximum number of reviews to display initially
    private TextView textViewAverageRating; // TextView for displaying average rating

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_worker);
        mAuth = FirebaseAuth.getInstance();
        String workerId = getIntent().getStringExtra("workerId");

        layoutRatingsReviews = findViewById(R.id.layoutRatingsReviews);
        btnViewMore = new Button(this);
        btnViewMore.setText("View More");
        textViewAverageRating = findViewById(R.id.textViewAverageRating); // Initialize TextView

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int displayedCount = 0; // Track the number of displayed reviews

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (displayedCount < INITIAL_DISPLAY_LIMIT) {
                                addReviewToLayout(document);
                                displayedCount++;
                            }

                            // Calculate total rating for average
                            totalRating += document.getDouble("rating").floatValue();
                            ratingCount++;
                        }

                        currentDisplayedCount = displayedCount;

                        // Add "View More" button if there are more reviews
                        if (task.getResult().size() > INITIAL_DISPLAY_LIMIT) {
                            layoutRatingsReviews.addView(btnViewMore);
                        }

                        // Calculate and display the average rating
                        displayAverageRating();
                    } else {
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch job ratings and reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching job ratings and reviews: ", task.getException());
                    }
                });

        btnViewMore.setOnClickListener(view -> loadMoreReviews(workerId));

        // Retrieve worker's details from the job_applications collection
        db.collection("job_applications")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String workerName = document.getString("name");
                            String phoneNumber = document.getString("phoneNumber");
                            String location = document.getString("location");
                            String experience = document.getString("experience");

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
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch worker details.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching worker details: ", task.getException());
                    }
                });
    }

    private void addReviewToLayout(QueryDocumentSnapshot document) {
        String jobName = document.getString("jobName");
        float rating = document.getDouble("rating").floatValue();
        String review = document.getString("review");

        TextView textViewJobName = new TextView(ViewProfileWorkerActivity.this);
        textViewJobName.setText("Job Name: " + jobName);

        TextView textViewReview = new TextView(ViewProfileWorkerActivity.this);
        textViewReview.setText("Review: " + review);

        RatingBar ratingBar = new RatingBar(ViewProfileWorkerActivity.this, null, android.R.attr.ratingBarStyleSmall);
        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ratingBar.setRating(rating);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setIsIndicator(true);

        layoutRatingsReviews.addView(textViewJobName);
        layoutRatingsReviews.addView(ratingBar);
        layoutRatingsReviews.addView(textViewReview);
    }

    private void loadMoreReviews(String workerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int displayedCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (displayedCount >= currentDisplayedCount && displayedCount < currentDisplayedCount + INITIAL_DISPLAY_LIMIT) {
                                addReviewToLayout(document);
                                displayedCount++;
                            }
                        }

                        currentDisplayedCount += INITIAL_DISPLAY_LIMIT;

                        // Add "View More" button if there are more reviews
                        if (currentDisplayedCount < task.getResult().size()) {
                            layoutRatingsReviews.addView(btnViewMore);
                        } else {
                            // If all reviews are displayed, remove the button
                            layoutRatingsReviews.removeView(btnViewMore);
                        }
                    } else {
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch more reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching more reviews: ", task.getException());
                    }
                });
    }


    private void displayAverageRating() {
        if (ratingCount > 0) {
            float averageRating = totalRating / ratingCount;
            textViewAverageRating.setText("Average Rating: " + averageRating);
        } else {
            textViewAverageRating.setText("Average Rating: N/A");
        }
    }
}
