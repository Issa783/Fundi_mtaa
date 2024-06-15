package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button buttonManageUsers = findViewById(R.id.buttonManageUsers);
        Button buttonManageJobs = findViewById(R.id.buttonManageJobs);
        Button buttonContentModeration = findViewById(R.id.buttonContentModeration);

        buttonManageUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, ManageUsersActivity.class));
            }
        });

        buttonManageJobs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(AdminActivity.this, ManageJobsActivity.class));
            }
        });

        buttonContentModeration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminActivity.this, ContentModerationActivity.class));
            }
        });
    }
}
