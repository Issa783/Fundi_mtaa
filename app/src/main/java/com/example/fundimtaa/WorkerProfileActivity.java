package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkerProfileActivity extends AppCompatActivity {
    private static final String TAG = WorkerProfileActivity.class.getSimpleName();
    private static final int INITIAL_DISPLAY_LIMIT = 3;

    private LinearLayout layoutRatingsReviews;
    private FirebaseAuth mAuth;
    private Button btnUpdateProfile;
    private Button btnViewMore;
    private Button btnViewLess;
    private TextView textViewAverageRating;
    private int currentDisplayedCount = 0;
    private List<QueryDocumentSnapshot> jobReviewsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);

        mAuth = FirebaseAuth.getInstance();

        layoutRatingsReviews = findViewById(R.id.layoutRatingsReviews);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnViewMore = findViewById(R.id.btnViewMore);
        btnViewLess = findViewById(R.id.btnViewLess);
        textViewAverageRating = findViewById(R.id.textViewAverageRating);

        btnUpdateProfile.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerProfileActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;

        if (workerId != null) {
            loadWorkerDetails(workerId);
            loadInitialReviews(workerId);
        }

        btnViewMore.setOnClickListener(v -> loadMoreReviews());
        btnViewLess.setOnClickListener(v -> loadInitialReviews(workerId));
    }

    private void loadWorkerDetails(String workerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("workers").document(workerId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            TextView textViewName = findViewById(R.id.textViewName);
                            TextView textViewEmail = findViewById(R.id.textViewEmail);
                            TextView textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
                            TextView textViewLocation = findViewById(R.id.textViewLocation);
                            TextView textViewExperience = findViewById(R.id.textViewExperience);
                            TextView textViewSpecialization = findViewById(R.id.textViewSpecialization);

                            textViewName.setText("Name: " + document.getString("name"));
                            textViewEmail.setText("Email: " + document.getString("email"));
                            textViewPhoneNumber.setText("Phone Number: " + document.getString("phoneNumber"));
                            textViewLocation.setText("Location: " + document.getString("location"));
                            textViewExperience.setText("Work Experience: " + document.getString("experience"));
                            textViewSpecialization.setText("Specialization: " + document.getString("specialization"));
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch worker details.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching worker details: ", task.getException());
                    }
                });
    }

    private void loadInitialReviews(String workerId) {
        layoutRatingsReviews.removeAllViews();
        jobReviewsList.clear();
        currentDisplayedCount = 0;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs").whereEqualTo("workerId", workerId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        float totalRating = 0;
                        int ratingCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            jobReviewsList.add(document);
                            if (currentDisplayedCount < INITIAL_DISPLAY_LIMIT) {
                                addReviewToLayout(document);
                                currentDisplayedCount++;
                            }
                            totalRating += document.getDouble("rating").floatValue();
                            ratingCount++;
                        }

                        updateButtonsVisibility();

                        if (ratingCount > 0) {
                            textViewAverageRating.setText("Average Rating: " + (totalRating / ratingCount));
                        } else {
                            textViewAverageRating.setText("Average Rating: N/A");
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch job ratings and reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching job ratings and reviews: ", task.getException());
                    }
                });
    }

    private void loadMoreReviews() {
        layoutRatingsReviews.removeAllViews();
        currentDisplayedCount = 0;

        for (QueryDocumentSnapshot document : jobReviewsList) {
            addReviewToLayout(document);
            currentDisplayedCount++;
        }

        updateButtonsVisibility();
    }

    private void addReviewToLayout(QueryDocumentSnapshot document) {
        String jobName = document.getString("jobName");
        float rating = document.getDouble("rating").floatValue();
        String review = document.getString("review");

        TextView textViewJobName = new TextView(this);
        textViewJobName.setText("Job Name: " + jobName);

        TextView textViewReview = new TextView(this);
        textViewReview.setText("Review: " + review);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleSmall);
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
}
