package com.example.fundimtaa;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList, this);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        String userId = mAuth.getCurrentUser().getUid();
        final String[] collectionPath = {""}; // Path to the notifications collection

        // Determine the user's role based on their document in the Firestore
        mFirestore.collection("clients").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User is a client
                        collectionPath[0] = "clients/" + userId + "/notifications";
                        fetchNotifications(collectionPath[0]);
                    } else {
                        // User is not a client, check if they are a worker
                        mFirestore.collection("workers").document(userId).get()
                                .addOnSuccessListener(workerDocumentSnapshot -> {
                                    if (workerDocumentSnapshot.exists()) {
                                        // User is a worker
                                        collectionPath[0] = "workers/" + userId + "/notifications";
                                        fetchNotifications(collectionPath[0]);
                                    } else {
                                        // User role not found
                                        Toast.makeText(NotificationActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Error fetching worker document
                                    Toast.makeText(NotificationActivity.this, "Error fetching worker document", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Error fetching client document
                    Toast.makeText(NotificationActivity.this, "Error fetching client document", Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchNotifications(String collectionPath) {
        mFirestore.collection(collectionPath)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        // Manually set the ID since it's not automatically mapped
                        notification.setId(doc.getId());
                        notificationList.add(notification);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Error fetching notifications
                    Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show();
                });
    }

}
