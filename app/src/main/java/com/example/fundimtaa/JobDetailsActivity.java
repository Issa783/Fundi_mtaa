package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class JobDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs_details);
        ImageView imageViewBackArrow = findViewById(R.id.imageViewBackArrow);
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                onBackPressed();
            }
        });

        // Get the job details from intent
        String jobName = getIntent().getStringExtra("jobName");
        String jobStartDate = getIntent().getStringExtra("jobStartDate");
        String minExperience = getIntent().getStringExtra("minExperience");
        String location = getIntent().getStringExtra("location");
        String price = getIntent().getStringExtra("price");
        String jobDescription = getIntent().getStringExtra("jobDescription");

        // Set the job details to TextViews
        TextView textViewJobName = findViewById(R.id.textViewJobName);
        TextView textViewJobStartDate = findViewById(R.id.textViewJobStartDate);
        TextView textViewMinExperience = findViewById(R.id.textViewMinExperience);
        TextView textViewLocation = findViewById(R.id.textViewLocation);
        TextView textViewPrice = findViewById(R.id.textViewPrice);
        TextView textViewJobDescription = findViewById(R.id.textViewJobDescription);

        textViewJobName.setText("Job Name: " + jobName);
        textViewJobStartDate.setText("Start Date: " + jobStartDate);
        textViewMinExperience.setText("Minimum Experience: " + minExperience);
        textViewLocation.setText("Location: " + location);
        textViewPrice.setText("Price: " + price);
        textViewJobDescription.setText("Job Description: " + jobDescription);
    }
}
