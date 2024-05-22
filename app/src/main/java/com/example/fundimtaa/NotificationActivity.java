package com.example.fundimtaa;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        // Extract data from the intent
        String notificationTitle = getIntent().getStringExtra("notificationTitle");
        String notificationBody = getIntent().getStringExtra("notificationBody");

        // Display the notification details (you can customize this as needed)
        TextView titleTextView = findViewById(R.id.notificationTitle);
        TextView bodyTextView = findViewById(R.id.notificationBody);

        titleTextView.setText(notificationTitle);
        bodyTextView.setText(notificationBody);
    }
}


