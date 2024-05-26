package com.example.fundimtaa;

import android.os.Bundle;
import android.widget.TextView;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.google.firebase.Timestamp;
import android.widget.TextView;

public class NotificationActivity extends AppCompatActivity {

    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Object> notificationList;

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

        TextView textViewNotifications = findViewById(R.id.textViewNotifications);
        textViewNotifications.setText("Notifications");

        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        String userId = mAuth.getCurrentUser().getUid();
        final String[] collectionPath = {""};

        mFirestore.collection("clients").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        collectionPath[0] = "clients/" + userId + "/notifications";
                        fetchNotifications(collectionPath[0]);
                    } else {
                        mFirestore.collection("workers").document(userId).get()
                                .addOnSuccessListener(workerDocumentSnapshot -> {
                                    if (workerDocumentSnapshot.exists()) {
                                        collectionPath[0] = "workers/" + userId + "/notifications";
                                        fetchNotifications(collectionPath[0]);
                                    } else {
                                        Toast.makeText(NotificationActivity.this, "User role not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(NotificationActivity.this, "Error fetching worker document", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(NotificationActivity.this, "Error fetching client document", Toast.LENGTH_SHORT).show());
    }

    private void fetchNotifications(String collectionPath) {
        mFirestore.collection(collectionPath)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    List<Notification> thisWeek = new ArrayList<>();
                    List<Notification> thisMonth = new ArrayList<>();
                    List<Notification> earlier = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification notification = doc.toObject(Notification.class);
                        notification.setId(doc.getId());

                        if (isWithinLastWeek(notification.getTimestamp())) {
                            thisWeek.add(notification);
                        } else if (isWithinLastMonth(notification.getTimestamp())) {
                            thisMonth.add(notification);
                        } else {
                            earlier.add(notification);
                        }
                    }

                    if (!thisWeek.isEmpty()) {
                        notificationList.add("This Week");
                        notificationList.addAll(thisWeek);
                    }
                    if (!thisMonth.isEmpty()) {
                        notificationList.add("This Month");
                        notificationList.addAll(thisMonth);
                    }
                    if (!earlier.isEmpty()) {
                        notificationList.add("Earlier");
                        notificationList.addAll(earlier);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(NotificationActivity.this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
    }

    private boolean isWithinLastWeek(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date lastWeek = calendar.getTime();
        return timestamp.toDate().after(lastWeek);
    }

    private boolean isWithinLastMonth(Timestamp timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Date lastMonth = calendar.getTime();
        return timestamp.toDate().after(lastMonth) && !isWithinLastWeek(timestamp);
    }
}
