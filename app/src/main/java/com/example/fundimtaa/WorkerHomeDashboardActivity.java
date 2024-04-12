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

        // Set OnClickListener for the search jobs card
        searchJobsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the SearchJobActivity when the card is clicked
                Intent intent = new Intent(WorkerHomeDashboardActivity.this, SearchJob.class);
                startActivity(intent);
            }
        });
    }
}
