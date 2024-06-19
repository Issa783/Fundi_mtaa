package com.example.fundimtaa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
public class ViewApplicants extends AppCompatActivity {
private boolean isJobAssigned = false;
private RecyclerView recyclerViewApplicants;
private WorkerAdapter workerAdapter;
private List<Worker> workerList;
private FirebaseFirestore db;
private ImageView imageViewFilter;

private String jobId;
private String clientId;
private String jobName;
private Map<String, Set<String>> assignedJobsMap = new HashMap<>();
private ViewApplicantsViewModel viewModel;
private BroadcastReceiver updateAssignedJobsCountReceiver;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);

        ImageView imageViewBackArrow = findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View v) {
        // Navigate back to the previous activity
        onBackPressed();
        }
        });

        imageViewFilter = findViewById(R.id.imageViewFilter);
        db = FirebaseFirestore.getInstance();
        viewModel = new ViewModelProvider(this).get(ViewApplicantsViewModel.class);

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
        jobName = getIntent().getStringExtra("jobName");
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
        // Observe data from ViewModel
        viewModel.getWorkersLiveData().observe(this, new Observer<List<Worker>>() {
@Override
public void onChanged(List<Worker> workers) {
        workerList.clear();
        workerList.addAll(workers);
        workerAdapter.notifyDataSetChanged();
        }
        });

        recommendWorkers();
        }

private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        TextView textViewExperience = dialogView.findViewById(R.id.textViewExperience);
        TextView textViewRating = dialogView.findViewById(R.id.textViewRating);
        TextView textViewAvailability = dialogView.findViewById(R.id.textViewAvailability); // New filter option
        TextView textViewClose = dialogView.findViewById(R.id.textViewClose);

        textViewAvailability.setOnClickListener(v -> {
        textViewAvailability.setTextColor(ContextCompat.getColor(this, R.color.selectedText));
        textViewExperience.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
        textViewRating.setTextColor(ContextCompat.getColor(this, R.color.defaultText));

        Collections.sort(workerList, (worker1, worker2) -> Integer.compare(worker1.getAssignedJobs(), worker2.getAssignedJobs()));
        workerAdapter.notifyDataSetChanged();
        dialog.dismiss();
        });

        textViewExperience.setOnClickListener(v -> {
        textViewExperience.setTextColor(ContextCompat.getColor(this, R.color.selectedText));
        textViewRating.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
        textViewAvailability.setTextColor(ContextCompat.getColor(this, R.color.defaultText));

        Collections.sort(workerList, (worker1, worker2) -> {
        int experience1 = parseExperience(worker1.getExperience());
        int experience2 = parseExperience(worker2.getExperience());
        return Integer.compare(experience2, experience1);
        });
        workerAdapter.notifyDataSetChanged();
        dialog.dismiss();
        });

        textViewRating.setOnClickListener(v -> {
        textViewRating.setTextColor(ContextCompat.getColor(this, R.color.selectedText));
        textViewExperience.setTextColor(ContextCompat.getColor(this, R.color.defaultText));
        textViewAvailability.setTextColor(ContextCompat.getColor(this, R.color.defaultText));

        db.collection("RatingsAndReviews")
        .get()
        .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
        List<Worker> ratedWorkers = new ArrayList<>();
        for (QueryDocumentSnapshot document : task.getResult()) {
        String workerId = document.getString("workerId");
        double rating = document.getDouble("rating");
        String review = document.getString("review");

        for (Worker worker : workerList) {
        if (worker.getWorkerId().equals(workerId)) {
        worker.setRating(rating);
        worker.setReview(review);
        ratedWorkers.add(worker);
        }
        }
        }
        Collections.sort(ratedWorkers, (worker1, worker2) -> Double.compare(worker2.getRating(), worker1.getRating()));
        viewModel.setWorkers(ratedWorkers);
        }
        });
        dialog.dismiss();
        });

        textViewClose.setOnClickListener(v -> dialog.dismiss());
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
        boolean foundResults = false; // Flag to track if results are found
        for (QueryDocumentSnapshot document : task.getResult()) {
        String name = document.getString("name");

        // Check if the name starts with the search text (case-insensitive)
        if (name.toLowerCase().startsWith(searchTextLowerCase)) {
        foundResults = true; // Set flag to true if results are found
        String workerId = document.getString("workerId");
        String phoneNumber = document.getString("phoneNumber");
        String location = document.getString("location");
        String dateOfApplication = document.getString("dateOfApplication");
        String experience = document.getString("experience");
        Timestamp timestamp = document.getTimestamp("timestamp");
        Worker worker = new Worker(workerId, name, phoneNumber, location, dateOfApplication, experience, timestamp);
        workerList.add(worker);
        }
        }

        if (!foundResults) {
        // Show a message if no results are found
        Toast.makeText(ViewApplicants.this, "No workers found with that name.", Toast.LENGTH_SHORT).show();
        }
        workerAdapter.notifyDataSetChanged();
        } else {
        Toast.makeText(ViewApplicants.this, "Failed to load workers: " + task.getException(), Toast.LENGTH_SHORT).show();
        }
        });
        }

private void loadAssignedJobsFromFirestore(String clientId) {
        db.collection("AssignedJobs")
        .whereEqualTo("clientId", clientId)
        .get()
        .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
        for (QueryDocumentSnapshot document : task.getResult()) {
        String workerId = document.getString("workerId");
        String jobId = document.getString("jobId");

        // Add workerId and jobId to the assignedJobsMap
        if (!assignedJobsMap.containsKey(workerId)) {
        assignedJobsMap.put(workerId, new HashSet<>());
        }
        assignedJobsMap.get(workerId).add(jobId);
        }

        // Update the assigned jobs count for each worker
        updateAssignedJobsCountForWorkers();
        } else {
        Log.e("ViewApplicants", "Error getting assigned jobs: ", task.getException());
        }
        });
        }

private void updateAssignedJobsCountForWorkers() {
        for (Worker worker : workerList) {
        String workerId = worker.getWorkerId();
        int assignedJobsCount = assignedJobsMap.containsKey(workerId) ? assignedJobsMap.get(workerId).size() : 0;
        worker.setAssignedJobs(assignedJobsCount);
        }
        workerAdapter.notifyDataSetChanged();
        }

private void loadWorkers() {
        db.collection("job_applications")
        .orderBy("timestamp", Query.Direction.DESCENDING)
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
        Timestamp timestamp = document.getTimestamp("timestamp");
        Worker worker = new Worker(workerId, name, phoneNumber, location, dateOfApplication, experience, timestamp);
        workerList.add(worker);
        }

        // Fetch assigned jobs count for each worker
        fetchAssignedJobsCountForAllWorkers();

        // Sort workers based on scores
        Collections.sort(workerList, (worker1, worker2) -> Double.compare(calculateWorkerScore(worker2), calculateWorkerScore(worker1)));

        workerAdapter.notifyDataSetChanged();
        } else {
        Toast.makeText(ViewApplicants.this, "Failed to load workers: " + task.getException(), Toast.LENGTH_SHORT).show();
        }
        }
        });
        }

// Fetch assigned jobs count for each worker
private void fetchAssignedJobsCountForAllWorkers() {
        for (Worker worker : workerList) {
        fetchAssignedJobsCount(worker);
        }
        }

private void fetchAssignedJobsCount(Worker worker) {
        db.collection("AssignedJobs")
        .whereEqualTo("workerId", worker.getWorkerId())
        .get()
        .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
        int assignedJobsCount = task.getResult().size();
        worker.setAssignedJobs(assignedJobsCount);
        workerAdapter.notifyDataSetChanged();
        }
        });
        }

private double calculateWorkerScore(Worker worker) {
        double rating = worker.getRating();
        int assignedJobs = worker.getAssignedJobs();

        // Normalize values (adjust normalization logic based on your data)
        double normalizedRating = rating / 5; // Assuming max rating is 5
        double normalizedAssignedJobs = 1.0 / (assignedJobs + 1); // More jobs, lower score

        // Calculate final score based on weights
        return (RATING_WEIGHT * normalizedRating) +
        (ASSIGNED_JOBS_WEIGHT * normalizedAssignedJobs);
        }

private void recommendWorkers() {
        // Add code to recommend workers based on the scoring system
        Collections.sort(workerList, (worker1, worker2) -> Double.compare(calculateWorkerScore(worker2), calculateWorkerScore(worker1)));

        // Update the adapter to reflect the recommended workers
        workerAdapter.notifyDataSetChanged();
        }


    private class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder> {

        private List<Worker> workerList;
        private String jobId;
        private String jobName;
        private String startDate;
        private String minExperience;
        private String location;
        private String price;
        private String jobDescription;
        private String clientId;
        private String documentId;

        public WorkerAdapter(List<Worker> workerList, String jobId, String jobName, String startDate, String minExperience, String location, String price, String jobDescription, String clientId, String documentId) {
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

        @NonNull
        @Override
        public WorkerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_applicants, parent, false);
            return new WorkerViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull WorkerViewHolder holder, int position) {
            Worker worker = workerList.get(position);
            holder.textViewWorkerName.setText("Worker Name: " + worker.getName());
            holder.textViewDateOfApplication.setText("Applied on: " + worker.getDateOfApplication());
            holder.textViewExperience.setText("Experience: " + worker.getExperience());
            holder.textViewAssignedJobs.setText("Assigned Jobs: " + worker.getAssignedJobs());

            // Highlight recommended workers
            if (position < 3) { // Top 3 workers
                holder.textViewRecommended.setVisibility(View.VISIBLE);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(ViewApplicants.this, R.color.recommended_worker_bg));
            } else {
                holder.textViewRecommended.setVisibility(View.GONE);
                holder.itemView.setBackgroundColor(ContextCompat.getColor(ViewApplicants.this, R.color.default_worker_bg));
            }

            holder.buttonViewProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ViewApplicants.this, ViewProfileWorkerActivity.class);
                intent.putExtra("workerId", worker.getWorkerId());
                startActivity(intent);
            });

            holder.buttonAssignJob.setOnClickListener(v -> {
                assignJob(worker, jobName, startDate, minExperience, location, price, jobDescription);
            });
        }

        @Override
        public int getItemCount() {
            return workerList.size();
        }

        public class WorkerViewHolder extends RecyclerView.ViewHolder {
            TextView textViewWorkerName, textViewDateOfApplication, textViewExperience, textViewAssignedJobs, textViewRecommended;
            Button buttonViewProfile, buttonAssignJob;

            public WorkerViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewWorkerName = itemView.findViewById(R.id.textViewName);
                textViewDateOfApplication = itemView.findViewById(R.id.textViewDateOfApplication);
                textViewExperience = itemView.findViewById(R.id.textViewExperience);
                textViewAssignedJobs = itemView.findViewById(R.id.textViewAssignedJobs);
                textViewRecommended = itemView.findViewById(R.id.textViewRecommended); // TextView for recommended tag
                buttonViewProfile = itemView.findViewById(R.id.buttonViewProfile);
                buttonAssignJob = itemView.findViewById(R.id.buttonAssignJob);
            }
        }
    }

    // Weights for the criteria
     private static final double RATING_WEIGHT = 0.7;
     private static final double ASSIGNED_JOBS_WEIGHT = 0.3;


    private boolean isJobAssignedToWorker(String clientId, String jobId, String workerId) {
        // Use a unique key based on clientId and jobId
        String key = clientId + "_" + jobId;
        Set<String> assignedWorkers = assignedJobsMap.get(key);
        return assignedWorkers != null && assignedWorkers.contains(workerId);
    }
    private void assignJob(Worker worker, String jobName, String startDate, String minExperience, String location, String price, String jobDescription) {
        // First, check if the job is already assigned to this worker in Firestore
        db.collection("AssignedJobs")
                .whereEqualTo("clientId", clientId)
                .whereEqualTo("jobId", jobId)
                .whereEqualTo("workerId", worker.getWorkerId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Job is already assigned to this worker
                        Toast.makeText(ViewApplicants.this, "You have already assigned this job to " + worker.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with the assignment
                        FirebaseFirestore.getInstance().collection("users").document(clientId).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        String clientName = documentSnapshot.getString("name");
                                        String clientPhoneNumber = documentSnapshot.getString("phoneNumber");
                                        String clientLocation = documentSnapshot.getString("location");
                                        String clientEmail = documentSnapshot.getString("email");

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
                                        assignedJob.put("rating", 0);
                                        assignedJob.put("review", "");
                                        assignedJob.put("timestamp", Timestamp.now());

                                        String documentId = jobId; // Use jobId as the document ID

                                        FirebaseFirestore.getInstance().collection("AssignedJobs")
                                                .document(documentId) // Set document ID
                                                .set(assignedJob)
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d("AssignedJobs", "Assigned job ID: " + documentId);

                                                    String key = clientId + "_" + jobId;
                                                    Set<String> assignedWorkers = assignedJobsMap.getOrDefault(key, new HashSet<>());
                                                    assignedWorkers.add(worker.getWorkerId());
                                                    assignedJobsMap.put(key, assignedWorkers);

                                                    saveAssignmentState(true);
                                                    Toast.makeText(ViewApplicants.this, "Job assigned to " + worker.getName(), Toast.LENGTH_SHORT).show();
                                                    worker.setAssignedJobs(worker.getAssignedJobs() + 1);
                                                    workerAdapter.notifyDataSetChanged();
                                                    notifyJobAssignment(clientId, worker.getWorkerId(), jobName);
                                                    updateWorkerAssignedJobsCount(worker.getWorkerId());

                                                    FirebaseFirestore.getInstance().collection("ClientJobsDetail")
                                                            .document(documentId) // Set document ID
                                                            .set(assignedJob)
                                                            .addOnSuccessListener(clientJobsDetailRef -> {
                                                                Log.d("ClientJobsDetail", "Job details stored in ClientJobsDetail successfully");

                                                                // Add RatingsAndReviews document
                                                                Map<String, Object> ratingsAndReviews = new HashMap<>();
                                                                ratingsAndReviews.put("rating", 0);
                                                                ratingsAndReviews.put("review", "");
                                                                ratingsAndReviews.put("workerId", worker.getWorkerId());
                                                                ratingsAndReviews.put("jobId", jobId);
                                                                ratingsAndReviews.put("jobName", jobName);

                                                                FirebaseFirestore.getInstance().collection("RatingsAndReviews")
                                                                        .document(documentId) // Set document ID
                                                                        .set(ratingsAndReviews)
                                                                        .addOnSuccessListener(ratingsAndReviewsRef -> {
                                                                            Log.d("RatingsAndReviews", "Ratings and Reviews stored successfully");
                                                                        })
                                                                        .addOnFailureListener(e -> {
                                                                            Log.e("RatingsAndReviews", "Failed to store Ratings and Reviews: " + e.getMessage());
                                                                        });
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Log.e("ClientJobsDetail", "Failed to store job details in ClientJobsDetail: " + e.getMessage());
                                                            });
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
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewApplicants.this, "Error checking existing assignment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void updateWorkerAssignedJobsCount(String workerId) {
        DocumentReference workerRef = FirebaseFirestore.getInstance().collection("AssignedJobs").document(workerId);
        workerRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long currentCount = documentSnapshot.getLong("numberOfAssignedJobs");
                if (currentCount == null) {
                    currentCount = 0L;
                }
                workerRef.update("numberOfAssignedJobs", currentCount + 1)
                        .addOnSuccessListener(aVoid -> Log.d("WorkerUpdate", "Updated number of assigned jobs for worker " + workerId))
                        .addOnFailureListener(e -> Log.e("WorkerUpdate", "Failed to update number of assigned jobs: " + e.getMessage()));
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("numberOfAssignedJobs", 1);
                workerRef.set(data, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> Log.d("WorkerUpdate", "Set number of assigned jobs for worker " + workerId))
                        .addOnFailureListener(e -> Log.e("WorkerUpdate", "Failed to set number of assigned jobs: " + e.getMessage()));
            }
        });
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
        Log.d("NotifyJobApplication", "jobName: " + jobName);
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{\"clientId\":\"" + clientId + "\", \"workerId\":\"" + workerId + "\",\"jobName\":\"" + jobName + "\"}";
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