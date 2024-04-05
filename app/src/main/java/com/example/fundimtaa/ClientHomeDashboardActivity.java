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

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingButton);
        // Set OnClickListener for the post job card
        postJobCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the job posting interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, PostingActivity.class));
            }
        });

        // Set OnClickListener for the floating action button
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the post job interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, PostJobActivity.class));
            }
        });
    }
}
