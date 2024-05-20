package com.example.fundimtaa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    private Button btnSaveProfile;
    private FirebaseAuth mAuth;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private EditText editTextLocation;
    private EditText editTextExperience;
    private EditText editTextSpecialization;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find EditText views by their IDs
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextExperience = findViewById(R.id.editTextExperience);
        editTextSpecialization = findViewById(R.id.editTextSpecialization);

        // Find the "Save Profile" button
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Set click listener on the button
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click to save the profile
                saveProfile();
            }
        });

        // Pre-populate EditText fields with user details
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String workerId = currentUser.getUid();
            db.collection("workers").document(workerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Populate EditText fields with user details
                            editTextName.setText(documentSnapshot.getString("name"));
                            editTextEmail.setText(documentSnapshot.getString("email"));
                            editTextPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                            editTextLocation.setText(documentSnapshot.getString("location"));
                            editTextExperience.setText(documentSnapshot.getString("experience"));
                            editTextSpecialization.setText(documentSnapshot.getString("specialization"));
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateProfileActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(UpdateProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        // Get the profile data from the EditText fields
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();
        String specialization = editTextSpecialization.getText().toString().trim();

        // Get the current user's ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String workerId = currentUser.getUid();

            // Prepare updated profile data
            Map<String, Object> updatedProfile = new HashMap<>();
            updatedProfile.put("name", name);
            updatedProfile.put("email", email);
            updatedProfile.put("phoneNumber", phoneNumber);
            updatedProfile.put("location", location);
            updatedProfile.put("experience", experience);
            updatedProfile.put("specialization", specialization);

            // Update user profile in Firestore
            db.collection("workers").document(workerId)
                    .update(updatedProfile)
                    .addOnSuccessListener(aVoid -> {
                        // Profile saved successfully
                        Toast.makeText(UpdateProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Finish this activity and go back to the previous activity
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure
                        Toast.makeText(UpdateProfileActivity.this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(UpdateProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
