package com.example.fundimtaa;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Find the "Save Profile" button
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        db = FirebaseFirestore.getInstance();


        // Set click listener on the button
        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click to save the profile
                saveProfile();
            }
        });
    }

    private void saveProfile() {
        // Get the profile data from the EditText fields
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();
        String specialization = editTextSpecialization.getText().toString().trim();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;
        if (!workerId.isEmpty()) {
            db.collection("Workers").document(workerId)
                    .set(new profileWorker(name, email, phoneNumber, location,experience,specialization))
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
            // Handle error if workerId is empty
            Toast.makeText(UpdateProfileActivity.this, "Worker ID not found", Toast.LENGTH_SHORT).show();
        }
    }
    }

