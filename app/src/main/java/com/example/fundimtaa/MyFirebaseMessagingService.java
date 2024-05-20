package com.example.fundimtaa;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMDebug";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        saveDeviceToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
    }

    private void saveDeviceToken(String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            Map<String, Object> userData = new HashMap<>();
            userData.put("token", token);

            FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
            mFirestore.collection("clients").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot clientDocument = task.getResult();
                            if (clientDocument.exists()) {
                                mFirestore.collection("clients").document(userId)
                                        .update(userData)
                                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Device token saved for client"))
                                        .addOnFailureListener(e -> Log.e(TAG, "Error saving device token for client: " + e.getMessage()));
                            } else {
                                mFirestore.collection("workers").document(userId).get()
                                        .addOnCompleteListener(workerTask -> {
                                            if (workerTask.isSuccessful()) {
                                                DocumentSnapshot workerDocument = workerTask.getResult();
                                                if (workerDocument.exists()) {
                                                    mFirestore.collection("workers").document(userId)
                                                            .update(userData)
                                                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Device token saved for worker"))
                                                            .addOnFailureListener(e -> Log.e(TAG, "Error saving device token for worker: " + e.getMessage()));
                                                } else {
                                                    Log.e(TAG, "User role not found");
                                                }
                                            } else {
                                                Log.e(TAG, "Error fetching worker document: " + workerTask.getException().getMessage());
                                            }
                                        });
                            }
                        } else {
                            Log.e(TAG, "Error fetching client document: " + task.getException().getMessage());
                        }
                    });
        }
    }
}
