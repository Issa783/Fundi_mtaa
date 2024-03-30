
package com.example.fundimtaa;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.fundimtaa.R;
import com.example.fundimtaa.WorkerHomeDashboardActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class LoginActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);

        ImageView imageViewBackArrow = findViewById(R.id.imageViewBackArrow);
        MaterialButton buttonClient = findViewById(R.id.buttonClient);
        MaterialButton buttonWorker = findViewById(R.id.buttonWorker);

        // Click listener for the back arrow ImageView
        imageViewBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Navigate back to the previous activity
            }
        });

        // Click listener for the Client button
        buttonClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apply visual feedback (e.g., change button color)
                buttonClient.setBackgroundColor(getResources().getColor(R.color.client_button_clicked));
                buttonWorker.setBackgroundColor(getResources().getColor(R.color.worker_button_unclicked));

                // Navigate to the client interface
                Intent intent = new Intent(LoginActivity2.this, ClientHomeDashboardActivity.class);
                startActivity(intent);
            }
        });

        // Click listener for the Worker button
        buttonWorker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Apply visual feedback (e.g., change button color)
                buttonWorker.setBackgroundColor(getResources().getColor(R.color.worker_button_clicked));
                buttonClient.setBackgroundColor(getResources().getColor(R.color.client_button_unclicked));

                // Navigate to the worker interface
                Intent intent = new Intent(LoginActivity2.this, WorkerHomeDashboardActivity.class);
                startActivity(intent);
            }
        });
    }
}
