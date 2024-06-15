package com.example.fundimtaa;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ManageJobsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private JobsAdapter jobsAdapter;
    private List<Job> jobList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_jobs);

        recyclerView = findViewById(R.id.recyclerViewJobs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        jobList = new ArrayList<>();
        jobsAdapter = new JobsAdapter(jobList, new JobsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Job job) {
                showClientDetailsDialog(job);
            }

            @Override
            public void onDeleteClick(Job job) {
                deleteJob(job);
            }

            @Override
            public void onEditClick(Job job) {
                showEditJobDialog(job);
            }
        });
        recyclerView.setAdapter(jobsAdapter);

        db = FirebaseFirestore.getInstance();

        loadJobs();
    }

    private void loadJobs() {
        db.collection("jobs")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ManageJobsActivity.this, "Error loading jobs", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        jobList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Job job = document.toObject(Job.class);
                            job.setDocumentId(document.getId()); // Use setDocumentId instead of setId
                            jobList.add(job);
                        }
                        jobsAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void deleteJob(Job job) {
        db.collection("jobs").document(job.getDocumentId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ManageJobsActivity.this, "Job deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ManageJobsActivity.this, "Error deleting job", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showEditJobDialog(final Job job) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_job, null);
        builder.setView(dialogView);

        final EditText editTextTitle = dialogView.findViewById(R.id.editTextJobTitle);
        final EditText editTextDescription = dialogView.findViewById(R.id.editTextJobDescription);
        final EditText editTextLocation = dialogView.findViewById(R.id.editTextJobLocation);
        final EditText editTextPrice = dialogView.findViewById(R.id.editTextJobPrice);
        final EditText editTextMinExperience = dialogView.findViewById(R.id.editTextJobMinExperience);
        final EditText editTextJobStartDate = dialogView.findViewById(R.id.editTextJobStartDate);

        editTextTitle.setText(job.getJobName());
        editTextDescription.setText(job.getJobDescription());
        editTextLocation.setText(job.getLocation());
        editTextPrice.setText(job.getPrice());
        editTextMinExperience.setText(job.getMinExperience());
        editTextJobStartDate.setText(job.getJobStartDate());

        builder.setTitle("Edit Job")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = editTextTitle.getText().toString();
                        String description = editTextDescription.getText().toString();
                        String location = editTextLocation.getText().toString();
                        String price = editTextPrice.getText().toString();
                        String minExperience = editTextMinExperience.getText().toString();
                        String jobStartDate = editTextJobStartDate.getText().toString();

                        if (!title.isEmpty() && !description.isEmpty() && !location.isEmpty() && !price.isEmpty() && !minExperience.isEmpty() && !jobStartDate.isEmpty()) {
                            job.setJobName(title);
                            job.setJobDescription(description);
                            job.setLocation(location);
                            job.setPrice(price);
                            job.setMinExperience(minExperience);
                            job.setJobStartDate(jobStartDate);

                            db.collection("jobs").document(job.getDocumentId())
                                    .set(job)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ManageJobsActivity.this, "Job updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(ManageJobsActivity.this, "Error updating job", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(ManageJobsActivity.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }



    private void showClientDetailsDialog(Job job) {
        String clientId = job.getClientId();

        if (clientId == null || clientId.isEmpty()) {
            Toast.makeText(ManageJobsActivity.this, "Client ID is invalid", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(clientId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String role = document.getString("role");
                                Log.d("Firestore", "User role: " + role); // Log the role

                                if ("client".equals(role)) {
                                    String clientName = document.getString("name");
                                    String clientEmail = document.getString("email");
                                    String clientPhoneNumber = document.getString("phoneNumber");
                                    String clientLocation = document.getString("location");

                                    String clientDetails = "Client Name: " + clientName + "\n" +
                                            "Client Email: " + clientEmail + "\n" +
                                            "Client Phone: " + clientPhoneNumber + "\n" +
                                            "Client Location: " + clientLocation;

                                    new AlertDialog.Builder(ManageJobsActivity.this)
                                            .setTitle("Client Details")
                                            .setMessage(clientDetails)
                                            .setPositiveButton("OK", null)
                                            .show();
                                } else {
                                    Toast.makeText(ManageJobsActivity.this, "The user is not a client", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(ManageJobsActivity.this, "Client not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ManageJobsActivity.this, "Error fetching client details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




}
