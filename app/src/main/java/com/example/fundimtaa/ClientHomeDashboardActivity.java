package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientHomeDashboardActivity extends AppCompatActivity {
    private View unreadIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_home_dashboard);
        unreadIndicator = findViewById(R.id.unread_indicator);
        // Initial call to update the notification icon
        updateNotificationIndicator();

        // Find views
        CardView postJobCard = findViewById(R.id.card_post_job);
        CardView clientJobHistory = findViewById(R.id.client_job_history);
        CardView clientProfileCard = findViewById(R.id.card_clientProfile);
        CardView notificationCard = findViewById(R.id.card_notification);
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
        notificationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the job posting interface
                startActivity(new Intent(ClientHomeDashboardActivity.this, NotificationActivity.class));
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
    @Override
    protected void onResume() {
        super.onResume();
        // Update the notification icon every time the activity resumes
        updateNotificationIndicator();
    }
    public void updateNotificationIndicator() {
        // Query Firestore to check for unread notifications
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("clients").document(currentUserId).collection("notifications")
                .whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        boolean hasUnreadNotifications = !task.getResult().isEmpty();
                        unreadIndicator.setVisibility(hasUnreadNotifications ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e("Dashboard", "Error getting unread notifications: " + task.getException().getMessage());
                        unreadIndicator.setVisibility(View.GONE);
                    }
                });
    }
}
