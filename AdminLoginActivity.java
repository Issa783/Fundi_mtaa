package com.example.fundimtaa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private String selectedRole;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        selectedRole = getIntent().getStringExtra("ROLE");
        // Find the Login Button in LoginActivity
        Button buttonLogin = findViewById(R.id.buttonLogin);

        // Set up OnClickListener for the Login Button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get email and password from EditText fields and perform normal email/password authentication
                EditText editTextEmail = findViewById(R.id.editTextEmail);
                EditText editTextPassword = findViewById(R.id.editTextPassword);
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                // Check if email and password are empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(AdminLoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(AdminLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(AdminLoginActivity.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();

                                    // Get user's role from Firestore and redirect accordingly
                                    String userId = user.getUid();
                                    mFirestore.collection("users").document(userId).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            String role = document.getString("role");
                                                            if (role != null) {
                                                                switch (role) {
                                                                    case "admin":
                                                                        startActivity(new Intent(AdminLoginActivity.this, AdminActivity.class));
                                                                        break;
                                                                    default:
                                                                        // User role not found
                                                                        Toast.makeText(AdminLoginActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                                                        break;
                                                                }
                                                            }
                                                        } else {
                                                            // Document doesn't exist, handle error
                                                            Toast.makeText(AdminLoginActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        // Error fetching user document
                                                        Toast.makeText(AdminLoginActivity.this, "Error fetching user document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(AdminLoginActivity.this, "Authentication failed. Email or password is incorrect.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}