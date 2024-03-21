package com.example.fundimtaa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdditionalRegistrationActivity extends AppCompatActivity {

    private EditText editTextAbout, editTextExperience, editTextLocation;
    private RadioGroup radioGroupUserType;
    private Button buttonUploadProfilePicture, buttonSubmitRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_registration);

        editTextAbout = findViewById(R.id.editTextAbout);
        editTextExperience = findViewById(R.id.editTextExperience);
        editTextLocation = findViewById(R.id.editTextLocation);
        radioGroupUserType = findViewById(R.id.radioGroupUserType);
        buttonUploadProfilePicture = findViewById(R.id.buttonUploadProfilePicture);
        buttonSubmitRegistration = findViewById(R.id.buttonSubmitRegistration);

        // Show or hide fields based on initial radio button selection
        toggleFields();

        // Set up listener for radio button changes
        radioGroupUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                toggleFields();
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
            } else {
                // Show fields for Client
                editTextLocation.setVisibility(View.VISIBLE);
                editTextAbout.setVisibility(View.VISIBLE);
                editTextExperience.setVisibility(View.GONE);
            }
        }
    }

    private void submitRegistration() {
        // Get additional registration details
        String about = editTextAbout.getText().toString().trim();
        String experience = editTextExperience.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String userType = radioGroupUserType.getCheckedRadioButtonId() == R.id.radioButtonClient ? "Client" : "Worker";

        // Perform registration logic (e.g., validate and store data)
        // For demonstration purposes, just displaying user input
        String registrationInfo = "About: " + about + "\nExperience: " + experience +
                "\nLocation: " + location + "\nUser Type: " + userType;
        Toast.makeText(this, registrationInfo, Toast.LENGTH_LONG).show();
    }
}
