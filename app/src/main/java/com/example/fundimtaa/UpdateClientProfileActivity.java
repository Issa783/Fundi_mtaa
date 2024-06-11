package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UpdateClientProfileActivity extends AppCompatActivity {
    private Button btnSaveProfile;
    private FirebaseAuth mAuth;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPhoneNumber;
    private EditText editTextLocation;
    private EditText editTextAbout;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_client_profile);
        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find EditText views by their IDs
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextAbout = findViewById(R.id.editTextAbout);


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
            String clientId = currentUser.getUid();
            db.collection("users").document(clientId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Populate EditText fields with user details
                            editTextName.setText(documentSnapshot.getString("name"));
                            editTextEmail.setText(documentSnapshot.getString("email"));
                            editTextPhoneNumber.setText(documentSnapshot.getString("phoneNumber"));
                            editTextLocation.setText(documentSnapshot.getString("location"));
                            editTextAbout.setText(documentSnapshot.getString("about"));

                        } else {
                            Toast.makeText(UpdateClientProfileActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateClientProfileActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(UpdateClientProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        // Get the profile data from the EditText fields
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phoneNumber = editTextPhoneNumber.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String about = editTextAbout.getText().toString().trim();

        // Get the current user's ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String clientId = currentUser.getUid();
            // Retrieve the existing client document
            db.collection("clients").document(clientId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userId = documentSnapshot.getString("userId");

                            // Include clientId and userId in the updated profile data
                            profileClient updatedProfile = new profileClient(
                                    name,
                                    email,
                                    phoneNumber,
                                    location,
                                    about,
                                    clientId,
                                    userId,
                                    documentSnapshot.getString("deviceToken") // Include the existing userId
                            );
                            // Update user profile in Firestore
                            db.collection("clients").document(clientId)
                                    .set(updatedProfile)
                                    .addOnSuccessListener(aVoid -> {
                                        // Profile saved successfully
                                        Toast.makeText(UpdateClientProfileActivity.this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                                        finish(); // Finish this activity and go back to the previous activity
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure
                                        Toast.makeText(UpdateClientProfileActivity.this, "Failed to save profile. Please try again.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(UpdateClientProfileActivity.this, "User details not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(UpdateClientProfileActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(UpdateClientProfileActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

}
