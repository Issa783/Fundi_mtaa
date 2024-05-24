package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class WorkerHomeDashboardActivity extends AppCompatActivity {
    private View unreadIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_home_dashboard);
        unreadIndicator = findViewById(R.id.unread_indicator);
        // Initial call to update the notification icon
        updateNotificationIndicator();

        // Get reference to the search jobs card
        CardView searchJobsCard = findViewById(R.id.search_Jobs_Card);
        CardView workerhistory = findViewById(R.id.worker_history);
        CardView profileCard = findViewById(R.id.card_profile);
        CardView notificationCard = findViewById(R.id.card_notification);
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
        notificationCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkerHomeDashboardActivity.this, NotificationActivity.class);
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
        db.collection("workers").document(currentUserId).collection("notifications")
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


