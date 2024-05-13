package com.example.fundimtaa;

import static com.google.firebase.messaging.Constants.MessagePayloadKeys.SENDER_ID;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ApplyJobActivity extends AppCompatActivity {
    // Declare messageId as a class variable
    private AtomicInteger messageId = new AtomicInteger(0);

    private EditText editTextName;
    private EditText editTextPhoneNumber;
    private EditText editTextLocation;
    private EditText editTextDate;
    private EditText editTextExperience;
    private Button buttonApplyJob;
    private String jobId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.editTextName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextDate = findViewById(R.id.editTextDate);
        editTextExperience = findViewById(R.id.editTextExperience);
        buttonApplyJob = findViewById(R.id.buttonApplyJob);

        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(editTextDate);
            }
        });

        buttonApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();
                String date = editTextDate.getText().toString().trim();
                String phoneNumber = editTextPhoneNumber.getText().toString().trim();
                String location = editTextLocation.getText().toString().trim();
                String experience = editTextExperience.getText().toString().trim();

                if (name.isEmpty() || date.isEmpty() || experience.isEmpty() || phoneNumber.isEmpty() || location.isEmpty()) {
                    Toast.makeText(ApplyJobActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Save details to the database and send notification
                    saveJobApplication(name, date, phoneNumber, location, experience);
                }
            }
        });
    }

    private void showDatePickerDialog(final EditText editText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(ApplyJobActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        String selectedDate = dateFormat.format(calendar.getTime());
                        editText.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void saveJobApplication(String name, String date, String phoneNumber, String location, String experience) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;
        jobId = getIntent().getStringExtra("jobId");

        if (workerId == null) {
            Toast.makeText(ApplyJobActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the worker has already applied for the job
        db.collection("job_applications")
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("workerId", workerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Worker has already applied for this job
                            Toast.makeText(ApplyJobActivity.this, "You have already applied for this job", Toast.LENGTH_SHORT).show();
                            editTextName.setText("");
                            editTextDate.setText("");
                            editTextPhoneNumber.setText("");
                            editTextLocation.setText("");
                            editTextExperience.setText("");
                        } else {
                            // Worker has not applied for this job yet, proceed with application
                            Map<String, Object> application = new HashMap<>();
                            application.put("name", name);
                            application.put("dateOfApplication", date);
                            application.put("phoneNumber", phoneNumber);
                            application.put("location", location);
                            application.put("experience", experience);
                            application.put("workerId", workerId);
                            application.put("jobId", jobId);

                            db.collection("job_applications")
                                    .add(application)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(ApplyJobActivity.this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                                        editTextName.setText("");
                                        editTextDate.setText("");
                                        editTextPhoneNumber.setText("");
                                        editTextLocation.setText("");
                                        editTextExperience.setText("");

                                        // Retrieve jobId from the newly added document
                                        String jobId = documentReference.getId();
                                        documentReference.update("jobId", jobId);
                                        notifyClientAboutJobApplication(jobId); // Notify client about the job application
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ApplyJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(ApplyJobActivity.this, "Error checking application status: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void notifyClientAboutJobApplication(String jobId) {
        Log.d("NotifyDebug", "Attempting to notify client about job application");

        // Retrieve client's FCM token based on jobId
        db.collection("jobs").document(jobId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("NotifyDebug", "Retrieved job document successfully");
                    if (documentSnapshot.exists()) {
                        String clientId = documentSnapshot.getString("clientId");
                        if (clientId != null && !clientId.isEmpty()) {
                            // Retrieve client's FCM token from the clients collection
                            db.collection("clients").document(clientId)
                                    .get()
                                    .addOnSuccessListener(clientDocument -> {
                                        Log.d("NotifyDebug", "Retrieved client document successfully");
                                        if (clientDocument.exists()) {
                                            String clientFCMToken = clientDocument.getString("deviceToken");
                                            if (clientFCMToken != null && !clientFCMToken.isEmpty()) {
                                                // Construct notification payload
                                                Map<String, String> notification = new HashMap<>();
                                                notification.put("title", "New Job Application");
                                                notification.put("body", "A worker has applied for your job.");

                                                // Construct message payload
                                                Map<String, Object> message = new HashMap<>();
                                                message.put("token", clientFCMToken);
                                                message.put("notification", notification);

                                                // Send notification
                                                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
                                                        .setMessageId(Integer.toString(messageId.incrementAndGet()))
                                                        .setData((Map<String, String>) (Map<?, ?>) message) // Explicit cast to Map<String, String>
                                                        .build());
                                                Log.d("NotifyDebug", "Notification sent successfully");
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("NotifyDebug", "Failed to retrieve client's FCM token: " + e.getMessage());
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotifyDebug", "Failed to retrieve job document: " + e.getMessage());
                });
    }



}
