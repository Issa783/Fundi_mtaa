package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ClientHomeDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_dashboard);

        // Find views
        CardView postJobCard = findViewById(R.id.card_post_job);
        CardView clientJobHistory = findViewById(R.id.client_job_history);
        CardView clientProfileCard = findViewById(R.id.card_clientProfile);
        CardView logOutCard = findViewById(R.id.card_logOut);


        // Set OnClickListener for the post job card
        postJobCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the job posting interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, PostingActivity.class));
            }
        });
        clientJobHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the job posting interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, ClientJobHistoryActivity.class));
            }
        });
        clientProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the job posting interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, ClientProfileActivity.class));
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
