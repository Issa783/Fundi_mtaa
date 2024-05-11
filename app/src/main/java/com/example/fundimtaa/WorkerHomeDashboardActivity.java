package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class WorkerHomeDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home_dashboard);

        // Get reference to the search jobs card
        CardView searchJobsCard = findViewById(R.id.search_Jobs_Card);
        CardView workerhistory = findViewById(R.id.worker_history);
        CardView profileCard = findViewById(R.id.card_profile);
        CardView logOutCard = findViewById(R.id.card_logOut);



        // Set OnClickListener for the search jobs card
        searchJobsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SearchJobActivity when the card is clicked
                Intent intent = new Intent(WorkerHomeDashboardActivity.this, WorkerViewJobs.class);
                startActivity(intent);
            }
        });
        workerhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkerHomeDashboardActivity.this, WorkerApplicationJobHistory.class);
                startActivity(intent);
            }
        });
        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkerHomeDashboardActivity.this, WorkerProfileActivity.class);
                startActivity(intent);
            }
        });
          logOutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the app
                finishAffinity();
            }
        });

    }
}
