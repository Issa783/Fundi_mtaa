package com.example.fundimtaa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ClientProfileActivity extends AppCompatActivity {
    private static final String TAG = ClientProfileActivity.class.getSimpleName();
    private String clientId;

    private Button btnUpdateProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click
                Intent intent = new Intent(ClientProfileActivity.this, UpdateClientProfileActivity.class);
                startActivity(intent);
            }
        });
        FirebaseUser currentUser = mAuth.getCurrentUser();
        clientId = currentUser != null ? currentUser.getUid() : null;
        // Retrieve and display client details
        fetchClientDetails();

    }



    private void fetchClientDetails() {
        if (clientId == null) {
            Log.e(TAG, "Client ID is null.");
            return;
        }
        // Retrieve client details from Firestore
        db.collection("users")
                .document(clientId)
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
                            String about = document.getString("about");

                            // Update TextViews with worker's details
                            TextView textViewName = findViewById(R.id.textViewName);
                            textViewName.setText("Name: " + name);

                            TextView textViewEmail = findViewById(R.id.textViewEmail);
                            textViewEmail.setText("Email: " + email);

                            TextView textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
                            textViewPhoneNumber.setText("Phone Number: " + phoneNumber);

                            TextView textViewLocation = findViewById(R.id.textViewLocation);
                            textViewLocation.setText("Location: " + location);

                            TextView textViewExperience = findViewById(R.id.textViewAbout);
                            textViewExperience.setText("About: " + about);

                        }
                    } else {
                        // Handle errors
                        Toast.makeText(ClientProfileActivity.this, "Failed to fetch worker details.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error fetching worker details: ", task.getException());
                    }
                });

    }
}
