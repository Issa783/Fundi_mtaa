package com.example.fundimtaa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewApplicants extends AppCompatActivity {

    private RecyclerView recyclerViewWorkers;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;

    private FirebaseFirestore db;

    private String jobId; // Job ID received from Intent extra
    private AutoCompleteTextView editTextSearch;
    private ImageView imageViewFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_applicants);
        // Initialize views
        editTextSearch = findViewById(R.id.editTextSearch);
        imageViewFilter = findViewById(R.id.imageViewFilter);
        // Set up search suggestions
        String[] suggestions = {"Suggestion 1", "Suggestion 2", "Suggestion 3"}; // Example suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
        editTextSearch.setAdapter(adapter);

        // Set up filter dialog
        imageViewFilter.setOnClickListener(v -> showFilterDialog());


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the job ID from Intent extra
        jobId = getIntent().getStringExtra("jobId");

        // Initialize RecyclerView
        recyclerViewWorkers = findViewById(R.id.recyclerViewApplicants);
        recyclerViewWorkers.setHasFixedSize(true);
        recyclerViewWorkers.setLayoutManager(new LinearLayoutManager(this));

        // Initialize worker list
        workerList = new ArrayList<>();

        // Initialize adapter
        workerAdapter = new WorkerAdapter(workerList);

        // Set adapter to RecyclerView
        recyclerViewWorkers.setAdapter(workerAdapter);

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
// Set up name filter click listener
        textViewName.setOnClickListener(v -> {
            String nameFilter = editTextSearch.getText().toString().trim();
            if (!nameFilter.isEmpty()) {
                // Filter workers with matching names
                List<Worker> filteredWorkers = filterWorkersByName(nameFilter);
                // Display filtered workers
                displayFilteredWorkers(filteredWorkers);
            } else {
                // No input in search field, handle accordingly
                Toast.makeText(this, "Please enter a name to search.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // Dismiss the dialog after performing the action
        });

// Set up experience filter click listener
        textViewExperience.setOnClickListener(v -> {
            String experienceFilter = editTextSearch.getText().toString().trim();
            if (!experienceFilter.isEmpty()) {
                // Filter workers with matching years of experience
                List<Worker> filteredWorkers = filterWorkersByExperience(experienceFilter);
                // Display filtered workers
                displayFilteredWorkers(filteredWorkers);
            } else {
                // No input in search field, handle accordingly
                Toast.makeText(this, "Please enter years of experience to search.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // Dismiss the dialog after performing the action
        });

        // Show dialog
        dialog.show();
    }
    private List<Worker> filterWorkersByName(String name) {
        List<Worker> filteredWorkers = new ArrayList<>();
        for (Worker worker : workerList) {
            if (worker.getName().toLowerCase().contains(name.toLowerCase())) {
                filteredWorkers.add(worker);
            }
        }
        return filteredWorkers;
    }

    private List<Worker> filterWorkersByExperience(String experience) {
        List<Worker> filteredWorkers = new ArrayList<>();
        for (Worker worker : workerList) {
            // Assuming the experience is stored as a string in the Worker object
            if (worker.getExperience().equals(experience)) {
                filteredWorkers.add(worker);
            }
        }
        return filteredWorkers;
    }

    private void displayFilteredWorkers(List<Worker> filteredWorkers) {
        // Clear the current worker list and add the filtered workers
        workerList.clear();
        workerList.addAll(filteredWorkers);
        // Notify the adapter of the data change
        workerAdapter.notifyDataSetChanged();
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
                                Worker worker = new Worker(workerId, name, dateOfApplication,experience);
                                workerList.add(worker);
                            }
                            workerAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ViewApplicants.this, "Failed to load workers: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Adapter for the RecyclerView
    private class WorkerAdapter extends RecyclerView.Adapter<WorkerViewHolder> {

        private List<Worker> workerList;

        public WorkerAdapter(List<Worker> workerList) {
            this.workerList = workerList;
        }

        @Override
        public WorkerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_view_applicants, parent, false);
            return new WorkerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(WorkerViewHolder holder, int position) {
            Worker worker = workerList.get(position);
            holder.textViewWorkerName.setText(worker.getName());
            holder.textViewDateOfApplication.setText("Applied on: " + worker.getDateOfApplication());
            holder.textViewExperience.setText("Experience: " + worker.getExperience());

            // Set OnClickListener for the "View Profile" button
            holder.buttonViewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle view profile button click
                    // Implement the logic to view worker's profile
                    Toast.makeText(ViewApplicants.this, "View profile clicked for worker: " + worker.getName(), Toast.LENGTH_SHORT).show();
                }
            });

            // Set OnClickListener for the "Apply" button
            holder.buttonApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Handle apply button click
                    // Implement the logic to apply the job to the worker
                    Toast.makeText(ViewApplicants.this, "Apply clicked for worker: " + worker.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return workerList.size();
        }
    }

    // ViewHolder for the RecyclerView
    private static class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWorkerName;
        TextView textViewDateOfApplication;
        TextView textViewExperience;
        Button buttonViewProfile;
        Button buttonApply;

        public WorkerViewHolder(View itemView) {
            super(itemView);
            textViewWorkerName = itemView.findViewById(R.id.textViewName);
            textViewDateOfApplication = itemView.findViewById(R.id.textViewDateOfApplication);
            textViewExperience = itemView.findViewById(R.id.textViewExperience);
            buttonViewProfile = itemView.findViewById(R.id.buttonViewProfile);
            buttonApply = itemView.findViewById(R.id.buttonApply);
        }
    }
}
