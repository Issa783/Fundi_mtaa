package com.example.fundimtaa;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import android.text.Editable; // Add import for Editable
import android.text.TextWatcher; // Add import for TextWatcher
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.RemoteMessage;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewApplicants extends AppCompatActivity {

    private RecyclerView recyclerViewApplicants;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;

    private FirebaseFirestore db;
    private FirebaseFirestore mFirestore;

    private String jobId; // Job ID received from Intent extra

    private ImageView imageViewFilter;
    private List<String> suggestionsList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);

        // Initialize views

        imageViewFilter = findViewById(R.id.imageViewFilter);
        mFirestore = FirebaseFirestore.getInstance();

        // Set up search suggestions

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Fetch worker names based on the input text
                fetchWorkerNamesStartingWith(newText.trim());
                return true;
            }
        });
        // Set up filter dialog
        imageViewFilter.setOnClickListener(v -> showFilterDialog());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the job ID from Intent extra
        jobId = getIntent().getStringExtra("jobId");
        String jobName = getIntent().getStringExtra("jobName");
        String startDate = getIntent().getStringExtra("jobStartDate");
        String clientId = getIntent().getStringExtra("clientId");
        String documentId = getIntent().getStringExtra("documentId");

        // Initialize RecyclerView
        recyclerViewApplicants = findViewById(R.id.recyclerWorkerViewApplicants);
        recyclerViewApplicants.setHasFixedSize(true);
        recyclerViewApplicants.setLayoutManager(new LinearLayoutManager(this));

        // Initialize worker list
        workerList = new ArrayList<>();

        // Initialize adapter
        workerAdapter = new WorkerAdapter(workerList,jobId,jobName,startDate,clientId,documentId);

        // Set adapter to RecyclerView
        recyclerViewApplicants.setAdapter(workerAdapter);

        // Load workers for the specified job
        loadWorkers();
    }

    private void showFilterDialog() {
        // Create dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Find views in the dialog layout
        TextView textViewName = dialogView.findViewById(R.id.textViewName);
        TextView textViewExperience = dialogView.findViewById(R.id.textViewExperience);
        TextView textViewClose = dialogView.findViewById(R.id.textViewClose);

        // Set up name filter click listener
        textViewName.setOnClickListener(v -> {
            // Change text color to indicate selection
            textViewName.setTextColor(ContextCompat.getColor(this, R.color.selectedText));

            // Reset other text view colors
            textViewExperience.setTextColor(ContextCompat.getColor(this, R.color.defaultText));

            // Sort workers by name in ascending order
            Collections.sort(workerList, (worker1, worker2) -> worker1.getName().compareTo(worker2.getName()));
            // Notify adapter of data change
            workerAdapter.notifyDataSetChanged();
            dialog.dismiss(); // Dismiss the dialog after performing the action
        });

        // Set up experience filter click listener
        textViewExperience.setOnClickListener(v -> {
            // Change text color to indicate selection
            textViewExperience.setTextColor(ContextCompat.getColor(this, R.color.selectedText));

            // Reset other text view colors
            textViewName.setTextColor(ContextCompat.getColor(this, R.color.defaultText));

            // Sort workers by experience in descending order
            Collections.sort(workerList, (worker1, worker2) -> {
                // Parse experience strings into integers
                int experience1 = parseExperience(worker1.getExperience());
                int experience2 = parseExperience(worker2.getExperience());
                // Compare the parsed experience values
                return Integer.compare(experience2, experience1);
            });
            // Notify adapter of data change
            workerAdapter.notifyDataSetChanged();
            dialog.dismiss(); // Dismiss the dialog after performing the action
        });
        textViewClose.setOnClickListener(v -> dialog.dismiss());
        // Show dialog
        dialog.show();
    }
    // Helper method to parse experience strings into integers
    private int parseExperience(String experience) {
        // Remove non-numeric characters and parse the remaining string as an integer
        return Integer.parseInt(experience.replaceAll("[^0-9]", ""));
    }

    private void fetchWorkerNamesStartingWith(String searchText) {
        // Convert the search text to lowercase for case-insensitive search
        String searchTextLowerCase = searchText.toLowerCase();

        // Query Firestore to fetch worker names
        db.collection("job_applications")
                .whereEqualTo("jobId", jobId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        workerList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");

                            // Check if the name starts with the search text (case-insensitive)
                            if (name.toLowerCase().startsWith(searchTextLowerCase)) {
                                String workerId = document.getString("workerId");
                                String dateOfApplication = document.getString("dateOfApplication");
                                String experience = document.getString("experience");
                                Worker worker = new Worker(workerId, name, dateOfApplication, experience);
                                workerList.add(worker);
                            }
                        }
                        workerAdapter.notifyDataSetChanged(); // Notify adapter that data set has changed
                    } else {
                        Toast.makeText(ViewApplicants.this, "Failed to fetch workers: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void loadWorkers() {
        // Query Firestore to fetch workers for the specified job ID
        db.collection("job_applications")
                .whereEqualTo("jobId", jobId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            workerList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String workerId = document.getString("workerId");
                                String name = document.getString("name");
                                String dateOfApplication = document.getString("dateOfApplication");
                                String experience = document.getString("experience");
                                Worker worker = new Worker(workerId, name, dateOfApplication, experience);
                                workerList.add(worker);
                            }
                            workerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ViewApplicants.this, "Failed to load workers: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void retrieveWorkerDeviceToken(String jobId) {
        mFirestore.collection("job_applications").whereEqualTo("jobId", jobId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String workerId = documentSnapshot.getString("workerId");
                        if (workerId != null) {
                            // Retrieve the worker's device token
                            mFirestore.collection("workers").document(workerId).get()
                                    .addOnSuccessListener(workerDocument -> {
                                        if (workerDocument.exists()) {
                                            String workerDeviceToken = workerDocument.getString("deviceToken");
                                            if (workerDeviceToken != null) {
                                                // Now you have the worker's device token
                                                // Send notification to the worker
                                                sendNotificationToWorker(workerDeviceToken);
                                            } else {
                                                Toast.makeText(ViewApplicants.this, "Worker device token not found", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(ViewApplicants.this, "Worker details not found", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(ViewApplicants.this, "Error fetching worker details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewApplicants.this, "Error fetching job applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void sendNotificationToWorker(String workerDeviceToken) {
        String message = "You have been assigned a job.";
        String title = "Job Assignment";

        Map<String, String> notificationMessage = new HashMap<>();
        notificationMessage.put("title", title);
        notificationMessage.put("message", message);

        // Send the notification
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(workerDeviceToken + "@gcm.googleapis.com")
                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                .setData(notificationMessage)
                .build());
    }



    // Method to handle the result of sending the notification
    private void handleNotificationResult(Task<Void> task) {
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Notification sent successfully
                Toast.makeText(ViewApplicants.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to send notification
                Toast.makeText(ViewApplicants.this, "Failed to send notification: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private class WorkerAdapter extends RecyclerView.Adapter<WorkerViewHolder> {

        private List<Worker> workerList;
        private String jobId;
        private String jobName;// Add jobName field
        private String startDate;
        private String clientId;
        private String documentId;

        public WorkerAdapter(List<Worker> workerList,String jobId,String jobName,String startDate,
                             String clientId,String documentId) {
            this.workerList = workerList;
            this.jobId = jobId;
            this.jobName = jobName; // Assign the jobName
            this.startDate = startDate;
            this.clientId = clientId;
            this.documentId = documentId;
        }

        @Override
        public WorkerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_view_applicants, parent, false);
            return new WorkerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WorkerViewHolder holder, int position) {
            Worker worker = workerList.get(position);
            holder.textViewWorkerName.setText("Name: " + worker.getName());
            holder.textViewDateOfApplication.setText("Applied on: " + worker.getDateOfApplication());
            holder.textViewExperience.setText("Experience: " + worker.getExperience());

            // Set OnClickListener for the "View Profile" button
            holder.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle view profile button click
                    // Implement the logic to view worker's profile
                    Intent intent = new Intent(ViewApplicants.this, WorkerProfileActivity.class);
                    intent.putExtra("workerId", worker.getWorkerId());
                    startActivity(intent);
                }
            });

            // Set OnClickListener for the "Assign Job" button
            // Set OnClickListener for the "Assign Job" button
            holder.buttonAssignJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retrieve the worker associated with this item
                    Worker worker = workerList.get(holder.getAdapterPosition());
                    // Store the assigned job details in Firestore
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    Map<String, Object> assignedJob = new HashMap<>();
                    assignedJob.put("workerId", worker.getWorkerId());
                    assignedJob .put("workerName", worker.getName());
                    assignedJob.put("clientId", clientId);
                    assignedJob.put("jobId", jobId);
                    assignedJob.put("documentId", documentId);
                    assignedJob.put("jobName", jobName);
                    assignedJob.put("jobStartDate", startDate);
                    assignedJob.put("rating", 0); // Initialize the rating with a default value, e.g., 0
                    assignedJob.put("review", ""); // Initialize the review with an empty string

                    assignedJob.put("assignedDate", new Date()); // You can use the current date/time as the assigned date
                    db.collection("AssignedJobs")
                            .add(assignedJob)
                            .addOnSuccessListener(documentReference -> {
                                String jobId = documentReference.getId(); // Retrieve the generated document ID

                                // Update the job document with the jobId
                                documentReference.update("jobId", jobId);
                                // Job assigned successfully, you can display a message or perform any other action if needed
                                Toast.makeText(ViewApplicants.this, "Job assigned to " + worker.getName(), Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors if any
                                Toast.makeText(ViewApplicants.this, "Failed to assign job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            });


        };
        @Override
        public int getItemCount() {
            return workerList.size();
        }
    }
    private static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWorkerName;
        TextView textViewDateOfApplication;
        TextView textViewExperience;
        Button buttonViewProfile;
        Button buttonAssignJob;

        public WorkerViewHolder(View itemView) {
            super(itemView);
            textViewWorkerName = itemView.findViewById(R.id.textViewName);
            textViewDateOfApplication = itemView.findViewById(R.id.textViewDateOfApplication);
            textViewExperience = itemView.findViewById(R.id.textViewExperience);
            buttonViewProfile = itemView.findViewById(R.id.buttonViewProfile);
            buttonAssignJob = itemView.findViewById(R.id.buttonAssignJob);
        }
    }

}

