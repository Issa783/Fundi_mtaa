package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewProfileWorkerActivity extends AppCompatActivity {
    private static final String TAG = ViewProfileWorkerActivity.class.getSimpleName();
    private static final int INITIAL_DISPLAY_LIMIT = 3;

    private LinearLayout layoutRatingsReviews;
    private FirebaseAuth mAuth;
    private int currentDisplayedCount = 0;
    private float totalRating = 0;
    private int ratingCount = 0;
    private Button btnViewMore;
    private Button btnViewLess;
    private TextView textViewAverageRating;
    private List<QueryDocumentSnapshot> jobReviewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_worker);

        mAuth = FirebaseAuth.getInstance();
        String workerId = getIntent().getStringExtra("workerId");

        layoutRatingsReviews = findViewById(R.id.layoutRatingsReviews);
        btnViewMore = findViewById(R.id.btnViewMore);
        btnViewLess = findViewById(R.id.btnViewLess);
        textViewAverageRating = findViewById(R.id.textViewAverageRating);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("ClientJobsDetail")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int displayedCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            jobReviewsList.add(document);
                            if (displayedCount < INITIAL_DISPLAY_LIMIT) {
                                addJobToLayout(document);
                                displayedCount++;
                            }
                            totalRating += document.getDouble("rating").floatValue();
                            ratingCount++;
                        }

                        currentDisplayedCount = displayedCount;
                        updateButtonsVisibility();
                        displayAverageRating();
                    } else {
                        Toast.makeText(ViewProfileWorkerActivity.this, "Failed to fetch job ratings and reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching job ratings and reviews: ", task.getException());
                    }
                });

        btnViewMore.setOnClickListener(view -> loadMoreReviews());
        btnViewLess.setOnClickListener(view -> loadInitialReviews());

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

    private void loadInitialReviews() {
        layoutRatingsReviews.removeAllViews();
        currentDisplayedCount = 0;

        for (int i = 0; i < jobReviewsList.size() && i < INITIAL_DISPLAY_LIMIT; i++) {
            addJobToLayout(jobReviewsList.get(i));
            currentDisplayedCount++;
        }

        updateButtonsVisibility();
    }

    private void loadMoreReviews() {
        layoutRatingsReviews.removeAllViews();

        for (QueryDocumentSnapshot document : jobReviewsList) {
            addJobToLayout(document);
        }

        currentDisplayedCount = jobReviewsList.size();
        updateButtonsVisibility();
    }

    private void addJobToLayout(QueryDocumentSnapshot document) {
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

    private void updateButtonsVisibility() {
        if (currentDisplayedCount >= jobReviewsList.size()) {
            btnViewMore.setVisibility(View.GONE);
        } else {
            btnViewMore.setVisibility(View.VISIBLE);
        }

        if (currentDisplayedCount > INITIAL_DISPLAY_LIMIT) {
            btnViewLess.setVisibility(View.VISIBLE);
        } else {
            btnViewLess.setVisibility(View.GONE);
        }
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
