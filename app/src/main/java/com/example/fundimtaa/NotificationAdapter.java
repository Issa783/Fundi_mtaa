package com.example.fundimtaa;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    private Context context;

    public NotificationAdapter(List<Notification> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.titleTextView.setText(notification.getTitle());
        holder.bodyTextView.setText(notification.getBody());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NotificationDetailActivity.class);
            intent.putExtra("notificationTitle", notification.getTitle());
            intent.putExtra("notificationBody", notification.getBody());
            context.startActivity(intent);

            // Mark as read
            markNotificationAsRead(notification.getId());
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView bodyTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notificationTitle);
            bodyTextView = itemView.findViewById(R.id.notificationBody);
        }
    }

  private void markNotificationAsRead(String notificationId) {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser != null) {
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check if the user is a client
        db.collection("clients").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // User is a client, update notification in the clients collection
                db.collection("clients").document(userId).collection("notifications")
                        .document(notificationId)
                        .update("read", true)
                        .addOnSuccessListener(aVoid -> {
                            // Optionally, update the unread count
                            if (context instanceof ClientHomeDashboardActivity) {
                                ((ClientHomeDashboardActivity) context).updateNotificationIndicator();
                            }
                        })
                        .addOnFailureListener(e -> Log.e("NotificationAdapter", "Error marking notification as read: " + e.getMessage()));
            } else {
                // User is not a client, check if they are a worker
                db.collection("workers").document(userId).get().addOnSuccessListener(workerDocumentSnapshot -> {
                    if (workerDocumentSnapshot.exists()) {
                        // User is a worker, update notification in the workers collection
                        db.collection("workers").document(userId).collection("notifications")
                                .document(notificationId)
                                .update("read", true)
                                .addOnSuccessListener(aVoid -> {
                                    // Optionally, update the unread count
                                    if (context instanceof WorkerHomeDashboardActivity) {
                                        ((WorkerHomeDashboardActivity) context).updateNotificationIndicator();
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("NotificationAdapter", "Error marking notification as read: " + e.getMessage()));
                    } else {
                        // User role not found
                        Log.e("NotificationAdapter", "Error marking notification as read: User role not found");
                    }
                }).addOnFailureListener(e -> Log.e("NotificationAdapter", "Error checking worker document: " + e.getMessage()));
            }
        }).addOnFailureListener(e -> Log.e("NotificationAdapter", "Error checking client document: " + e.getMessage()));
    }
}

}
