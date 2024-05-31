
package com.example.fundimtaa;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class WorkerActivity extends AppCompatActivity {

    private FirebaseFirestore db;
  /*  private static final int PICK_PDF_REQUEST = 2;
    private Uri cvUri;
    private StorageReference cvStorageReference;
    private EditText editTextCVName;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker);
       // cvStorageReference  = FirebaseStorage.getInstance().getReference("cv_uploads");
        //editTextCVName = findViewById(R.id.editTextCVName);

        db = FirebaseFirestore.getInstance();

        // Retrieve the data passed from RegistrationActivity
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        // Initialize EditText fields
        EditText editTextAbout = findViewById(R.id.editTextAbout);
        EditText editTextExperience = findViewById(R.id.editTextExperience);
        EditText editTextLocation = findViewById(R.id.editTextLocation);
        EditText editTextSpecialization = findViewById(R.id.editTextSpecialization);
       // EditText editTextCVName = findViewById(R.id.editTextCVName);
        //Button buttonUploadCV = findViewById(R.id.buttonUploadCV);
        Button buttonSubmit = findViewById(R.id.buttonSubmitRegistration);
     /*   buttonUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfFileChooser();
            }
        });*/
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get additional data filled on WorkerActivity
                String about = editTextAbout.getText().toString().trim();
                String experience = editTextExperience.getText().toString().trim();
                String location = editTextLocation.getText().toString().trim();
                String specialization = editTextSpecialization.getText().toString().trim();
                //String cvName = editTextCVName.getText().toString().trim();

                // Check if all fields are filled
                if (about.isEmpty() || experience.isEmpty() || location.isEmpty() || specialization.isEmpty()) {
                    Toast.makeText(WorkerActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a HashMap to hold the additional data
                Map<String, Object> workerData = new HashMap<>();
                workerData.put("about", about);
                workerData.put("experience", experience);
                workerData.put("location", location);
                workerData.put("specialization", specialization);
                workerData.put("assignedJobsCount",0);
               // workerData.put("cvName", cvName);

                // Add the additional data to Firestore
                db.collection("workers").document(userId)
                        .set(workerData, SetOptions.merge())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                // Data added successfully
                                Toast.makeText(WorkerActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(WorkerActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to add additional data
                                Toast.makeText(WorkerActivity.this, "Failed to store additional data in Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
   /* private void openPdfFileChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select CV"), PICK_PDF_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            cvUri = data.getData();
            uploadCV();
        }
    }
    private void uploadCV() {
        if (cvUri != null) {
            // Get the actual file name from the URI
            String cvFileName = getFileName(cvUri);
            StorageReference fileReference = cvStorageReference.child(cvFileName); // Use the actual file name
            fileReference.putFile(cvUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        Toast.makeText(WorkerActivity.this, "CV Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        // Set the name of the uploaded file to the EditText field
                        editTextCVName.setText(cvFileName);
                        // Make the EditText field visible
                        editTextCVName.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> Toast.makeText(WorkerActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show());
        }
    }

    // Method to extract the file name from the URI
    // Method to extract the file name from the URI
    private String getFileName(Uri uri) {
        String result = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    result = cursor.getString(nameIndex);
                } else {
                    // Handle the case where DISPLAY_NAME column is not found
                    result = uri.getLastPathSegment();
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }
    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }*/
}
