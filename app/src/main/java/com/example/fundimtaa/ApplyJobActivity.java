package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

public class ApplyJobActivity extends AppCompatActivity {

    private EditText editTextName;
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
                String experience = editTextExperience.getText().toString().trim();

                if (name.isEmpty() || date.isEmpty() || experience.isEmpty()) {
                    Toast.makeText(ApplyJobActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Save details to the database and send notification
                    saveJobApplication(name, date, experience);
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

    private void saveJobApplication(String name, String date, String experience) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;
        jobId = getIntent().getStringExtra("jobId");

        if (workerId == null) {
            Toast.makeText(ApplyJobActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> application = new HashMap<>();
        application.put("name", name);
        application.put("dateOfApplication", date);
        application.put("experience", experience);
        application.put("workerId", workerId);
        application.put("jobId",jobId);

        db.collection("job_applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ApplyJobActivity.this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                    editTextName.setText("");
                    editTextDate.setText("");
                    editTextExperience.setText("");

                    // Retrieve jobId from the newly added document
                    String jobId = documentReference.getId();

                    // Retrieve client's device token and send notification
                    retrieveClientDeviceToken(jobId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ApplyJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveClientDeviceToken(String jobId) {
        mFirestore.collection("jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String clientId = documentSnapshot.getString("clientId");
                        if (clientId != null) {
                            mFirestore.collection("clients").document(clientId).get()
                                    .addOnSuccessListener(clientDocument -> {
                                        if (clientDocument.exists()) {
                                            String clientDeviceToken = clientDocument.getString("deviceToken");
                                            if (clientDeviceToken != null) {
                                                // Now you have the client's device token
                                                // Send notification to the client
                                                sendNotificationToClient(clientDeviceToken);
                                            } else {
                                                Toast.makeText(ApplyJobActivity.this, "Client device token not found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ApplyJobActivity.this, "Client details not found", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ApplyJobActivity.this, "Error fetching client details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(ApplyJobActivity.this, "Client ID not found for the job", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ApplyJobActivity.this, "Job details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ApplyJobActivity.this, "Error fetching job details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToClient(String clientDeviceToken) {
        String message = "A worker has applied for the job you posted";
        String title = "New Job Application";

        Map<String, String> notificationMessage = new HashMap<>();
        notificationMessage.put("title", title);
        notificationMessage.put("message", message);

        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(clientDeviceToken + "@gcm.googleapis.com")
                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                .setData(notificationMessage)
                .build());
    }
}
