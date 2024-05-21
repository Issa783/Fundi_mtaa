package com.example.fundimtaa;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        saveDeviceToken(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle FCM messages here.
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
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

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notificationTitle", title);
        intent.putExtra("notificationBody", messageBody);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Default Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

}
