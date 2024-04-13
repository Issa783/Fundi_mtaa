package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApplyJobActivity extends AppCompatActivity {

    private EditText editTextName;
    private EditText editTextDate;
    private Button buttonApplyJob;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        editTextName = findViewById(R.id.editTextName);
        editTextDate = findViewById(R.id.editTextDate);
        buttonApplyJob = findViewById(R.id.buttonApplyJob);

        // Set Click Listener for Apply Job Button
        buttonApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String name = editTextName.getText().toString().trim();
                String date = editTextDate.getText().toString().trim();

                // Check if input fields are empty
                if (name.isEmpty() || date.isEmpty()) {
                    Toast.makeText(ApplyJobActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Save details to the database
                    saveJobApplication(name, date);
                }
            }
        });
    }

    // Method to save job application details to the database
    private void saveJobApplication(String name, String date) {
        // Create a new document with a generated ID
        Map<String, Object> application = new HashMap<>();
        application.put("name", name);
        application.put("date", date);

        // Add a new document with a generated ID
        db.collection("job_applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    // Display success message
                    Toast.makeText(ApplyJobActivity.this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    editTextName.setText("");
                    editTextDate.setText("");

                    // Send notification
                    sendNotification(name);
                })
                .addOnFailureListener(e -> {
                    // Display error message
                    Toast.makeText(ApplyJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotification(String name) {
        // Construct notification payload
        String message = "Worker " + name + " has applied for a job";
        String title = "New Job Application";

        // Create notification JSON payload
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", title);
            notificationBody.put("message", message);

            notification.put("to", "/topics/client_notifications"); // Subscribe client to this topic
            notification.put("data", notificationBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send the notification
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder("878531237039")
                .setMessageId("your_message_id")
                .addData("title", title)
                .addData("message", message)
                .build());
    }
}
