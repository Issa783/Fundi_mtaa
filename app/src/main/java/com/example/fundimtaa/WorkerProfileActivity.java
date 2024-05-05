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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class WorkerProfileActivity extends AppCompatActivity {
    private static final String TAG = WorkerProfileActivity.class.getSimpleName();

    private LinearLayout layoutRatingsReviews;
    private FirebaseAuth mAuth;
    private Button btnUpdateProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_profile);

        mAuth = FirebaseAuth.getInstance();

        // Initialize layoutRatingsReviews LinearLayout
        layoutRatingsReviews = findViewById(R.id.layoutRatingsReviews);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        // Set click listener on the button
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click
                // For example, you can navigate to an activity where the worker can update their profile
                Intent intent = new Intent(WorkerProfileActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        // Retrieve worker's ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;

        // Query Firestore to fetch ratings and reviews for jobs assigned to this worker
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("AssignedJobs")
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Extract job details
                            String jobId = document.getId();
                            String jobName = document.getString("jobName");
                            float rating = document.getDouble("rating").floatValue();
                            String review = document.getString("review");

                            // Create TextViews to display job name and review
                            TextView textViewJobName = new TextView(WorkerProfileActivity.this);
                            textViewJobName.setText("Job: " + jobName);

                            TextView textViewReview = new TextView(WorkerProfileActivity.this);
                            textViewReview.setText("Review: " + review);

                            // Create RatingBar to display rating
                            RatingBar ratingBar = new RatingBar(WorkerProfileActivity.this, null, android.R.attr.ratingBarStyleSmall);
                            ratingBar.setRating(rating);
                            ratingBar.setIsIndicator(true);

                            // Add views to layoutRatingsReviews LinearLayout
                            layoutRatingsReviews.addView(textViewJobName);
                            layoutRatingsReviews.addView(ratingBar);
                            layoutRatingsReviews.addView(textViewReview);
                        }
                    } else {
                        // Handle errors
                        Toast.makeText(WorkerProfileActivity.this, "Failed to fetch job ratings and reviews.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching job ratings and reviews: ", task.getException());
                    }
                });

        // Retrieve and display worker's details from the Firestore "Workers" collection
        if (workerId != null) {
            db.collection("Workers")
                    .document(workerId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // Extract worker details
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phoneNumber");
                                String location = document.getString("location");
                                String experience = document.getString("experience");
                                String specialization = document.getString("specialization");

                                // Update TextViews with worker's details
                                TextView textViewName = findViewById(R.id.textViewName);
                                textViewName.setText("Name: " + name);

                                TextView textViewEmail = findViewById(R.id.textViewEmail);
                                textViewEmail.setText("Email: " + email);

                                TextView textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
                                textViewPhoneNumber.setText("Phone Number: " + phoneNumber);

                                TextView textViewLocation = findViewById(R.id.textViewLocation);
                                textViewLocation.setText("Location: " + location);

                                TextView textViewExperience = findViewById(R.id.textViewExperience);
                                textViewExperience.setText("Experience: " + experience);

                                TextView textViewSpecialization = findViewById(R.id.textViewSpecialization);
                                textViewSpecialization.setText("Specialization: " + specialization);
                            }
                        } else {
                            // Handle errors
                            Toast.makeText(WorkerProfileActivity.this, "Failed to fetch worker details.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error fetching worker details: ", task.getException());
                        }
                    });
        }
    }
}
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">




<!-- Worker's Profile Information -->
<androidx.cardview.widget.CardView android:id="@+id/cardViewProfileInfo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:elevation="4dp"
        android:foreground="?android:attr/selectableItemBackground"
        android:padding="16dp"
        xmlns:android="http://schemas.android.com/apk/res/android">

<LinearLayout
        android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

<!-- Fieldset: Personal Information -->
<TextView
            android:id="@+id/textViewPersonalInfo"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Personal Information"
                    android:textStyle="bold"
                    android:textSize="18sp"
                    android:layout_marginBottom="8dp"/>

<!-- Name -->
<TextView
            android:id="@+id/textViewName"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Name: "/>

<!-- Email -->
<TextView
            android:id="@+id/textViewEmail"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Email: "/>

<!-- Phone Number -->
<TextView
            android:id="@+id/textViewPhoneNumber"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Phone Number: "/>

<!-- Location -->
<TextView
            android:id="@+id/textViewLocation"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Location: "/>

<!-- Fieldset: Experience and Specialization -->
<TextView
            android:id="@+id/textViewExperienceSpecialization"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Experience and Specialization"
                    android:textStyle="bold"
                    android:textSize="18sp"
                    android:layout_marginTop="16dp"
                    android:layout_marginBottom="8dp"/>

<!-- Experience -->
<TextView
            android:id="@+id/textViewExperience"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Experience: "/>

<!-- Specialization -->
<TextView
            android:id="@+id/textViewSpecialization"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:text="Specialization: "/>

<!-- Fieldset: Ratings and Reviews -->
<TextView
            android:id="@+id/textViewRatingsReviews"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Ratings and Reviews"
                    android:textStyle="bold"
                    android:textSize="18sp"
                    android:layout_marginTop="16dp"
                    android:layout_marginBottom="8dp"/>

<!-- Dynamic layout to display ratings and reviews -->
<LinearLayout
            android:id="@+id/layoutRatingsReviews"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="vertical"/>

</LinearLayout>
</androidx.cardview.widget.CardView>

<!-- Button: Update Profile -->
<Button
    android:id="@+id/btnUpdateProfile"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Update Profile"
            android:layout_below="@id/cardViewProfileInfo"
            android:layout_centerHorizontal="true"
            android:layout_marginTop="16dp"/>
</LinearLayout>

