package com.example.fundimtaa;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AdditionalRegistrationActivity extends AppCompatActivity {

    private EditText editTextAbout, editTextExperience, editTextLocation, editTextSpecialization;
    private RadioGroup radioGroupUserType;
    private Button buttonUploadProfilePicture, buttonSubmitRegistration;
    private Button buttonUploadCV;
    private Uri cvUri;
    private StorageReference cvStorageReference;
    private FirebaseFirestore db; // Add this line
    private String userId; // Add this line

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_PDF_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_registration);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore
        userId = getIntent().getStringExtra("userId"); // Retrieve user ID from intent

        editTextAbout = findViewById(R.id.editTextAbout);
        editTextExperience = findViewById(R.id.editTextExperience);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextSpecialization = findViewById(R.id.editTextSpecialization);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        buttonUploadProfilePicture = findViewById(R.id.buttonUploadProfilePicture);
        buttonSubmitRegistration = findViewById(R.id.buttonSubmitRegistration);
        buttonUploadCV = findViewById(R.id.buttonUploadCV);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        cvStorageReference = storage.getReference().child("cv_files");

        // Show or hide fields based on initial radio button selection
        toggleFields();

        // Set up listener for radio button changes
        radioGroupUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                toggleFields();
            }
        });

        buttonUploadProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageFileChooser();
            }
        });

        buttonUploadCV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPdfFileChooser();
            }
        });

        buttonSubmitRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRegistration();
            }
        });
    }

    private void toggleFields() {
        int selectedId = radioGroupUserType.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);

        if (selectedRadioButton != null) {
            String userType = selectedRadioButton.getText().toString();
            if (userType.equals("Worker")) {
                // Show fields for Worker
                editTextLocation.setVisibility(View.VISIBLE);
                editTextAbout.setVisibility(View.VISIBLE);
                editTextExperience.setVisibility(View.VISIBLE);
                editTextSpecialization.setVisibility(View.VISIBLE);
            } else {
                // Show fields for Client
                editTextLocation.setVisibility(View.VISIBLE);
                editTextAbout.setVisibility(View.VISIBLE);
                editTextExperience.setVisibility(View.GONE);
                editTextSpecialization.setVisibility(View.VISIBLE);
            }
        }
    }

    private void submitRegistration() {
        // Get additional registration details
        String about = editTextAbout.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String specialization = editTextSpecialization.getText().toString().trim();
        String userType = radioGroupUserType.getCheckedRadioButtonId() == R.id.radioButtonClient ? "Client" : "Worker";
        // Update Firestore document with additional information
        DocumentReference userRef = db.collection("users").document(userId);
        userRef
                .update("about", about,
                        "experience", experience,
                        "location", location,
                        "specialization", specialization)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AdditionalRegistrationActivity.this, "Additional information saved successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Finish the activity after saving data
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AdditionalRegistrationActivity.this, "Failed to save additional information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    

    private void openImageFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openPdfFileChooser() {
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select CV"), PICK_PDF_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
        } else if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            cvUri = data.getData();
            uploadCV();
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileReference = cvStorageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(imageUri));

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(AdditionalRegistrationActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AdditionalRegistrationActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void uploadCV() {
        if (cvUri != null) {
            StorageReference fileReference = cvStorageReference.child(System.currentTimeMillis() + ".pdf");
            fileReference.putFile(cvUri)
                    .addOnSuccessListener(taskSnapshot -> Toast.makeText(AdditionalRegistrationActivity.this, "CV Uploaded Successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(AdditionalRegistrationActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show());
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(uri));
    }
}
