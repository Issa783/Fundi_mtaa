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
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apply_job);
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
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

        // Set Click Listener for Apply Job Button
        buttonApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String name = editTextName.getText().toString().trim();
                String date = editTextDate.getText().toString().trim();
                String experience = editTextExperience.getText().toString().trim();

                // Check if input fields are empty
                if (name.isEmpty() || date.isEmpty() || experience.isEmpty()) {
                    Toast.makeText(ApplyJobActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Save details to the database
                    saveJobApplication(name, date, experience);
                }
            }
        });
    }
    private void showDatePickerDialog(final EditText editText) {
        // Get current date to set as default in the DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog and set the default date
        DatePickerDialog datePickerDialog = new DatePickerDialog(ApplyJobActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Set the selected date in the EditText field
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                        String selectedDate = dateFormat.format(calendar.getTime());
                        editText.setText(selectedDate);
                    }
                }, year, month, dayOfMonth);

        // Show DatePickerDialog
        datePickerDialog.show();
    }

    // Method to save job application details to the database and notify the client
    private void saveJobApplication(String name, String dateOfApplication, String experience) {
        // Retrieve the current user ID (worker)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String workerId = currentUser != null ? currentUser.getUid() : null;
        String jobId = getIntent().getStringExtra("jobId"); // Get the job ID from Intent extra

        // Create a new document with job application details
        Map<String, Object> application = new HashMap<>();
        application.put("name", name);
        application.put("dateOfApplication", dateOfApplication);
        application.put("experience", experience);
        application.put("jobId", jobId); // Store job ID along with worker details
        application.put("workerId", workerId); // Store worker ID

        // Add a new document with a generated ID
        db.collection("job_applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    // Display success message
                    Toast.makeText(ApplyJobActivity.this, "Application submitted successfully", Toast.LENGTH_SHORT).show();
                    // Clear input fields
                    editTextName.setText("");
                    editTextDate.setText("");
                    editTextExperience.setText("");

                    // Send notification to the client
                    sendNotificationToClient(jobId, name);
                })
                .addOnFailureListener(e -> {
                    // Display error message
                    Toast.makeText(ApplyJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method to send notification to the client
    private void sendNotificationToClient(String jobId, String workerName) {
        // Retrieve client ID who posted the job
        db.collection("jobs").document(jobId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String clientId = documentSnapshot.getString("clientId");
                        // Construct notification payload
                        String message = "Worker " + workerName + " has applied for the job you posted";
                        String title = "New Job Application";

                        // Send notification
                        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(clientId + "@gcm.googleapis.com")
                                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                                .addData("title", title)
                                .addData("message", message)
                                .build());
                    } else {
                        Toast.makeText(ApplyJobActivity.this, "Job details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ApplyJobActivity.this, "Error fetching job details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
