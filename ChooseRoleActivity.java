package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
public class ChooseRoleActivity extends AppCompatActivity {

    private CardView cardClient, cardWorker, cardAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        cardClient = findViewById(R.id.card_client);
        cardWorker = findViewById(R.id.card_worker);
        cardAdmin = findViewById(R.id.card_admin);

        cardClient.setOnClickListener(view -> navigateToLogin("client"));
        cardWorker.setOnClickListener(view -> navigateToLogin("worker"));
        cardAdmin.setOnClickListener(view -> navigateToLogin("admin"));
    }

    private void navigateToLogin(String role) {
        Intent intent = new Intent(ChooseRoleActivity.this, LoginActivity.class);
        intent.putExtra("ROLE", role);
        startActivity(intent);
    }
}
