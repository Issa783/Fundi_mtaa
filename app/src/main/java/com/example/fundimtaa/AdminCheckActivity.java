package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminCheckActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String role = document.getString("role");
                    if ("admin".equals(role)) {
                        Intent intent = new Intent(AdminCheckActivity.this, AdminActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(AdminCheckActivity.this, "Access denied", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(AdminCheckActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(AdminCheckActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
