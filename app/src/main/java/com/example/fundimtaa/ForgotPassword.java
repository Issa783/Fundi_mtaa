package com.example.fundimtaa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


import com.google.android.material.button.MaterialButton;

public class ForgotPassword extends AppCompatActivity {
    private EditText editTextEmail;
    private MaterialButton buttonResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        buttonResetPassword = findViewById(R.id.buttonResetPassword);
        // Inside onCreate method of ForgotPassword activity
        Button buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the login page
                Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });


        buttonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the email entered by the user
                String email = editTextEmail.getText().toString().trim();

                // Validate email
                if (isValidEmail(email)) {
                    // Send password reset email
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password reset email sent successfully
                                        Toast.makeText(ForgotPassword.this, "Password reset email sent. Please check your email.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Failed to send password reset email
                                        Toast.makeText(ForgotPassword.this, "Failed to send password reset email. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    // Show error message for invalid email
                    editTextEmail.setError("Enter a valid email address");
                    editTextEmail.requestFocus();
                }
            }
        });

    }

    // Function to validate email
    private boolean isValidEmail(String email) {
        // Email validation logic
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
