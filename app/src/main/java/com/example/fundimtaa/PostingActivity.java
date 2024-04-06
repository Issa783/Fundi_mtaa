package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PostingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        // Find the floating action button
        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);

        // Set OnClickListener for the floating action button
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the post job activity
                startActivity(new Intent(PostingActivity.this, PostJobActivity.class));
            }
        });

        // Find the "View Jobs" button
        Button buttonViewJobs = findViewById(R.id.buttonViewJobs);

        // Set OnClickListener for the "View Jobs" button
        buttonViewJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the view jobs activity
                startActivity(new Intent(PostingActivity.this, ViewJobsActivity.class));
            }
        });
    }
}
