package com.example.fundimtaa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class ClientActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        db = FirebaseFirestore.getInstance();

        // Retrieve the data passed from RegistrationActivity
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        // Initialize EditText fields
        EditText editTextAbout = findViewById(R.id.editTextAbout);
        EditText editTextLocation = findViewById(R.id.editTextLocation);

        Button buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get additional data filled on ClientActivity
                String about = editTextAbout.getText().toString().trim();
                String location = editTextLocation.getText().toString().trim();

                // Check if all fields are filled
                if (about.isEmpty() || location.isEmpty()) {
                    Toast.makeText(ClientActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a HashMap to hold the additional data
                Map<String, Object> clientData = new HashMap<>();
                clientData.put("about", about);
                clientData.put("location", location);
                clientData.put("role", "client"); // Adding the role field

                // Add the additional data to Firestore users collection
                db.collection("users").document(userId)
                        .set(clientData, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Data added successfully
                                Toast.makeText(ClientActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                // Navigate back to LoginActivity
                                startActivity(new Intent(ClientActivity.this, LoginActivity.class));
                                finish(); // Finish the current activity to prevent going back to it from LoginActivity
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to add additional data
                                Toast.makeText(ClientActivity.this, "Failed to store additional data in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
