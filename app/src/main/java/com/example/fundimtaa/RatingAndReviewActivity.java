package com.example.fundimtaa;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class RatingAndReviewActivity extends AppCompatActivity {

    private static final String TAG = RatingAndReviewActivity.class.getSimpleName();

    private String jobId;
    private String jobName;

    private RatingBar ratingBar;
    private EditText editTextReview;
    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_and_review);

        // Retrieve job details from intent extras
        jobId = getIntent().getStringExtra("jobId");
        jobName = getIntent().getStringExtra("jobName");

        // Initialize views
        ratingBar = findViewById(R.id.ratingBar);
        editTextReview = findViewById(R.id.editTextReview);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Set action for the submit button
        buttonSubmit.setOnClickListener(v -> submitRatingAndReview());
    }

    private void submitRatingAndReview() {
        // Get the rating from the RatingBar
        float rating = ratingBar.getRating();
        // Get the review from the EditText
        String review = editTextReview.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Assuming "AssignedJobs" collection has a field for ratings and reviews
        db.collection("AssignedJobs")
                .document(jobId) // Using the jobId retrieved from intent extras
                .update("rating", rating, "review", review)
                .addOnSuccessListener(aVoid -> {
                    // Rating and review updated successfully
                    Toast.makeText(RatingAndReviewActivity.this, "Rating and review submitted for " + jobName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle errors
                    Toast.makeText(RatingAndReviewActivity.this, "Failed to submit rating and review. Please try again.", Toast.LENGTH_SHORT).show();
                    // Log the exception for debugging
                    Log.e(TAG, "Error updating rating and review: ", e);
                });
    }
}
