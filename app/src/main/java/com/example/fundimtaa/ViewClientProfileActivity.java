package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ViewClientProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_client_profile);
        // Retrieve client details passed from WorkerApplicationJobHistory activity
        String clientName = getIntent().getStringExtra("clientName");
        String clientEmail = getIntent().getStringExtra("clientEmail");
        String clientPhoneNumber = getIntent().getStringExtra("clientPhoneNumber");
        String clientLocation = getIntent().getStringExtra("clientLocation");

        // Initialize TextViews to display client details
        TextView textViewClientName = findViewById(R.id.textViewClientName);
        TextView textViewClientEmail = findViewById(R.id.textViewClientEmail);
        TextView textViewClientPhoneNumber = findViewById(R.id.textViewClientPhoneNumber);
        TextView textViewClientLocation = findViewById(R.id.textViewClientLocation);

        // Set client details to TextViews
        textViewClientName.setText("Name: " + clientName);
        textViewClientEmail.setText("Email: " + clientEmail);
        textViewClientPhoneNumber.setText("Phone Number: " + clientPhoneNumber);
        textViewClientLocation.setText("Location: " + clientLocation);
    }
    }
