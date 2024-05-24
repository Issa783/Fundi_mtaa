package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewApplicants extends AppCompatActivity {
    private boolean isJobAssigned = false;
    private RecyclerView recyclerViewApplicants;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;
    private FirebaseFirestore db;
    private ImageView imageViewFilter;

    private String jobId;
    private String clientId;
    private Map<String, Set<String>> assignedJobsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);

        imageViewFilter = findViewById(R.id.imageViewFilter);
        db = FirebaseFirestore.getInstance();

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fetchWorkerNamesStartingWith(newText.trim());
                return true;
            }
        });

        imageViewFilter.setOnClickListener(v -> showFilterDialog());

        db = FirebaseFirestore.getInstance();

        recyclerViewApplicants = findViewById(R.id.recyclerWorkerViewApplicants);
        recyclerViewApplicants.setHasFixedSize(true);
        recyclerViewApplicants.setLayoutManager(new LinearLayoutManager(this));
        workerList = new ArrayList<>();
        jobId = getIntent().getStringExtra("jobId");
        clientId = getIntent().getStringExtra("clientId");
        String jobName = getIntent().getStringExtra("jobName");
        String startDate = getIntent().getStringExtra("jobStartDate");
        String minExperience = getIntent().getStringExtra("minExperience");
        String location = getIntent().getStringExtra("location");
        String price = getIntent().getStringExtra("price");
        String jobDescription = getIntent().getStringExtra("jobDescription");
        String documentId = getIntent().getStringExtra("documentId");
// Initialize adapter
        workerAdapter = new WorkerAdapter(workerList, jobId, jobName, startDate, minExperience, location, price, jobDescription, clientId, documentId);
        recyclerViewApplicants.setAdapter(workerAdapter);
        loadWorkers();
        loadAssignedJobsFromFirestore(clientId);
        // Retrieve assignment state
        isJobAssigned = getAssignmentState();
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
                                String phoneNumber = document.getString("phoneNumber");
                                String location = document.getString("location");
                                String dateOfApplication = document.getString("dateOfApplication");
                                String experience = document.getString("experience");
                                Worker worker = new Worker(workerId, name, phoneNumber, location, dateOfApplication, experience);
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
                                String phoneNumber = document.getString("phoneNumber");
                                String location = document.getString("location");
                                String dateOfApplication = document.getString("dateOfApplication");
                                String experience = document.getString("experience");
                                Worker worker = new Worker(workerId, name, phoneNumber, location, dateOfApplication, experience);
                                workerList.add(worker);
                            }
                            workerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ViewApplicants.this, "Failed to load workers: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadAssignedJobsFromFirestore(String clientId) {
        FirebaseFirestore.getInstance().collection("AssignedJobs")
                .whereEqualTo("clientId", clientId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String workerId = document.getString("workerId");
                        Set<String> assignedWorkers = assignedJobsMap.getOrDefault(clientId, new HashSet<>());
                        assignedWorkers.add(workerId);
                        assignedJobsMap.put(clientId, assignedWorkers);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private class WorkerAdapter extends RecyclerView.Adapter<WorkerViewHolder> {
        private List<Worker> workerList;
        private String jobId;
        private String jobName;// Add jobName field
        private String startDate;
        private String minExperience;
        private String location;
        private String price;
        private String jobDescription;
        private String clientId;
        private String documentId;


        public WorkerAdapter(List<Worker> workerList, String jobId, String jobName, String startDate,
                             String minExperience, String location, String price, String jobDescription,
                             String clientId, String documentId) {
            this.workerList = workerList;
            this.jobId = jobId;
            this.jobName = jobName;
            this.startDate = startDate;
            this.minExperience = minExperience;
            this.location = location;
            this.price = price;
            this.jobDescription = jobDescription;
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
            holder.textViewWorkerName.setText("Worker Name: " + worker.getName());
            holder.textViewDateOfApplication.setText("Applied on: " + worker.getDateOfApplication());
            holder.textViewExperience.setText("Experience: " + worker.getExperience());

            // Set OnClickListener for the "View Profile" button
            holder.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle view profile button click
                    // Implement the logic to view worker's profile
                    Intent intent = new Intent(ViewApplicants.this, ViewProfileWorkerActivity.class);
                    intent.putExtra("workerId", worker.getWorkerId());
                    startActivity(intent);
                }
            });

            holder.buttonAssignJob.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    assignJob(
                            workerList.get(holder.getAdapterPosition()), // Worker
                            jobName, // Job name
                            startDate, // Start date
                            minExperience, // Minimum experience
                            location, // Location
                            price, // Price
                            jobDescription // Job description
                    );
                }
            });

        }

        @Override
        public int getItemCount() {
            return workerList.size();
        }
    }

    private static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWorkerName;
        TextView textViewDateOfApplication;
        TextView textViewExperience;
        Button buttonAssignJob;
        Button buttonViewProfile;

        public WorkerViewHolder(View itemView) {
            super(itemView);
            textViewWorkerName = itemView.findViewById(R.id.textViewName);
            textViewDateOfApplication = itemView.findViewById(R.id.textViewDateOfApplication);
            textViewExperience = itemView.findViewById(R.id.textViewExperience);
            buttonAssignJob = itemView.findViewById(R.id.buttonAssignJob);
            buttonViewProfile =  itemView.findViewById(R.id.buttonViewProfile);
        }
    }

    private boolean isJobAssignedToWorker(String clientId, String workerId) {
        Set<String> assignedWorkers = assignedJobsMap.get(clientId);
        return assignedWorkers != null && assignedWorkers.contains(workerId);
    }

    private void assignJob(Worker worker, String jobName, String startDate, String minExperience, String location, String price, String jobDescription) {
        // Check if the job has already been assigned
        if (isJobAssigned) {
            Toast.makeText(ViewApplicants.this, "The job has already been assigned to a worker ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the job is already assigned to the worker by the client
        if (isJobAssignedToWorker(clientId, worker.getWorkerId())) {
            Toast.makeText(ViewApplicants.this, "You have already assigned this job to " + worker.getName(), Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve client details
        FirebaseFirestore.getInstance().collection("clients").document(clientId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String clientName = documentSnapshot.getString("name");
                        String clientPhoneNumber = documentSnapshot.getString("phoneNumber");
                        String clientLocation = documentSnapshot.getString("location");
                        String clientEmail = documentSnapshot.getString("email");

                        // Store assigned job details in Firestore
                        Map<String, Object> assignedJob = new HashMap<>();
                        assignedJob.put("clientId", clientId);
                        assignedJob.put("clientName", clientName);
                        assignedJob.put("clientPhoneNumber", clientPhoneNumber);
                        assignedJob.put("clientLocation", clientLocation);
                        assignedJob.put("clientEmail", clientEmail);
                        assignedJob.put("workerId", worker.getWorkerId());
                        assignedJob.put("workerName", worker.getName());
                        assignedJob.put("jobId", jobId);
                        assignedJob.put("jobName", jobName);
                        assignedJob.put("jobStartDate", startDate);
                        assignedJob.put("minExperience", minExperience);
                        assignedJob.put("location", location);
                        assignedJob.put("price", price);
                        assignedJob.put("jobDescription", jobDescription);
                        assignedJob.put("rating", 0); // Initialize the rating with a default value, e.g., 0
                        assignedJob.put("review", "");
                        assignedJob.put("assignedDate", new Date()); // Use current date/time as assigned date

                        FirebaseFirestore.getInstance().collection("AssignedJobs")
                                .add(assignedJob)
                                .addOnSuccessListener(documentReference -> {
                                    // Retrieve the generated document ID
                                    String jobId = documentReference.getId();

                                    // Update the job document with the jobId
                                    documentReference.update("jobId", jobId);

                                    // Update the assigned jobs map
                                    Set<String> assignedWorkers = assignedJobsMap.getOrDefault(clientId, new HashSet<>());
                                    assignedWorkers.add(worker.getWorkerId());
                                    assignedJobsMap.put(clientId, assignedWorkers);

                                    // Save the assignment state to SharedPreferences
                                    saveAssignmentState(true);

                                    // Notify the client about successful assignment
                                    Toast.makeText(ViewApplicants.this, "Job assigned to " + worker.getName(), Toast.LENGTH_SHORT).show();
                                    // Notify the worker about job assignment
                                    notifyJobAssignment(clientId,worker.getWorkerId(), jobId);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(ViewApplicants.this, "Failed to assign job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(ViewApplicants.this, "Client details not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewApplicants.this, "Error fetching client details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        isJobAssigned = true;
    }
    private void saveAssignmentState(boolean isAssigned) {
        getSharedPreferences("ViewApplicantsPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isJobAssigned_" + jobId, isAssigned)
                .apply();
    }

    private boolean getAssignmentState() {
        return getSharedPreferences("ViewApplicantsPrefs", MODE_PRIVATE)
                .getBoolean("isJobAssigned_" + jobId, false);
    }
    private void notifyJobAssignment(String clientId, String workerId, String jobId) {
        // Log the request parameters
        Log.d("NotifyJobApplication", "clientId: " + clientId);
        Log.d("NotifyJobApplication", "workerId: " + workerId);
        Log.d("NotifyJobApplication", "jobId: " + jobId);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{\"clientId\":\"" + clientId + "\", \"workerId\":\"" + workerId + "\", \"jobId\":\"" + jobId + "\"}";
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url("https://notify-1-wk1o.onrender.com/notify-job-assignment")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Log.e("NotifyJobApplication", "Notification failed: " + e.getMessage());
                    Toast.makeText(ViewApplicants.this, "Notification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Log.d("NotifyJobApplication", "Notification sent successfully");
                        Toast.makeText(ViewApplicants.this, "Notification sent successfully", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Log.e("NotifyJobApplication", "Notification failed: " + response.message());
                        Toast.makeText(ViewApplicants.this, "Notification failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
                response.close(); // Always close the response
            }
        });

    }



}
