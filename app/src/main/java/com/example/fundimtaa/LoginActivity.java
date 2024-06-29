package com.example.fundimtaa;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient; // Google Sign-In client
    private static final int RC_SIGN_IN = 9001; // Request code for Google Sign-In
    private FirebaseFirestore mFirestore;
    private String selectedRole; // Role passed from ChooseRoleActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        // Get the role passed from ChooseRoleActivity
        selectedRole = getIntent().getStringExtra("ROLE");

        // Configure Google Sign-In
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("878531237039-odhvhm05a4h7t52pkmett7pd1eb38qqp.apps.googleusercontent.com") // Use your actual Web Client ID here
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        TextView forgotPassword = findViewById(R.id.forgotpassword);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPassword.class));
            }
        });

        // Find the Sign Up TextView in LoginActivity
        TextView textViewRegister = findViewById(R.id.textViewRegister);

        // Set up OnClickListener for the Sign Up TextView
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start RegistrationActivity
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

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
                    Toast.makeText(LoginActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login with Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Authentication succeeded.", Toast.LENGTH_SHORT).show();

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
                                                                    case "client":
                                                                        startActivity(new Intent(LoginActivity.this, ClientHomeDashboardActivity.class));
                                                                        break;
                                                                    case "worker":
                                                                        startActivity(new Intent(LoginActivity.this, WorkerHomeDashboardActivity.class));
                                                                        break;
                                                                    default:
                                                                        // User role not found
                                                                        Toast.makeText(LoginActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                                                        break;
                                                                }
                                                            }
                                                        } else {
                                                            // Document doesn't exist, handle error
                                                            Toast.makeText(LoginActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        // Error fetching user document
                                                        Toast.makeText(LoginActivity.this, "Error fetching user document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication failed. Email or password is incorrect.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // Find the Google Sign-In Button in LoginActivity
        Button buttonGoogleLogin = findViewById(R.id.buttonGoogleLogin);

        // Set up OnClickListener for the Google Sign-In Button
        buttonGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void handleLogin() {
        // After successful login, retrieve the device token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            Log.d("FCMDebug", "Device token retrieved successfully: " + token);
                            // Save the device token along with user information in the database
                            saveDeviceToken(token);
                        } else {
                            // Failed to retrieve device token
                            Log.e("FCMDebug", "Failed to get device token: " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveDeviceToken(String token) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Get the user ID
            String userId = currentUser.getUid();

            // Create a map to store user data including the device token
            Map<String, Object> userData = new HashMap<>();
            userData.put("token", token);
            // Add other user data as needed

            // Update the user document with the device token
            mFirestore.collection("users").document(userId)
                    .update(userData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("LoginActivity", "Device token saved for the user");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("LoginActivity", "Error saving device token: " + e.getMessage());
                        }
                    });
        } else {
            // Role not found
            Log.e("LoginActivity", "User role not found");
        }
    }

    private void signInWithGoogle() {
        // Sign out the current user to ensure the account chooser dialog appears every time
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Start the sign-in intent
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }


    // Handle the result of the Google Sign-In intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // Google Sign-In failed
            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Google Sign-In succeeded.", Toast.LENGTH_SHORT).show();

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
                                                            case "client":
                                                                startActivity(new Intent(LoginActivity.this, ClientHomeDashboardActivity.class));
                                                                break;
                                                            case "worker":
                                                                startActivity(new Intent(LoginActivity.this, WorkerHomeDashboardActivity.class));
                                                                break;
                                                            default:
                                                                // User role not found
                                                                Toast.makeText(LoginActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                                                break;
                                                        }
                                                    }
                                                } else {
                                                    // Document doesn't exist, handle error
                                                    Toast.makeText(LoginActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                // Error fetching user document
                                                Toast.makeText(LoginActivity.this, "Error fetching user document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Sign in failed
                            Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
