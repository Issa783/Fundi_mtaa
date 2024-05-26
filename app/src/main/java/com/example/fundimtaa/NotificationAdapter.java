package com.example.fundimtaa;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private List<Object> notifications;
    private Context context;

    public NotificationAdapter(List<Object> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (notifications.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            Notification notification = (Notification) notifications.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.titleTextView.setText(notification.getTitle());
            itemViewHolder.bodyTextView.setText(notification.getBody());

            itemViewHolder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, NotificationDetailActivity.class);
                intent.putExtra("notificationTitle", notification.getTitle());
                intent.putExtra("notificationBody", notification.getBody());
                context.startActivity(intent);

                markNotificationAsRead(notification.getId());
            });
        } else if (holder instanceof HeaderViewHolder) {
            String headerTitle = (String) notifications.get(position);
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.headerTextView.setText(headerTitle);
        }
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView bodyTextView;
        public CardView cardView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notificationTitle);
            bodyTextView = itemView.findViewById(R.id.notificationBody);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView headerTextView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerTextView = itemView.findViewById(R.id.notificationHeader);
        }
    }

    private void markNotificationAsRead(String notificationId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("clients").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    db.collection("clients").document(userId).collection("notifications")
                            .document(notificationId)
                            .update("read", true)
                            .addOnSuccessListener(aVoid -> {
                                if (context instanceof ClientHomeDashboardActivity) {
                                    ((ClientHomeDashboardActivity) context).updateNotificationIndicator();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("NotificationAdapter", "Error marking notification as read: " + e.getMessage()));
                } else {
                    db.collection("workers").document(userId).get().addOnSuccessListener(workerDocumentSnapshot -> {
                        if (workerDocumentSnapshot.exists()) {
                            db.collection("workers").document(userId).collection("notifications")
                                    .document(notificationId)
                                    .update("read", true)
                                    .addOnSuccessListener(aVoid -> {
                                        if (context instanceof WorkerHomeDashboardActivity) {
                                            ((WorkerHomeDashboardActivity) context).updateNotificationIndicator();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("NotificationAdapter", "Error marking notification as read: " + e.getMessage()));
                        } else {
                            Log.e("NotificationAdapter", "Error marking notification as read: User role not found");
                        }
                    }).addOnFailureListener(e -> Log.e("NotificationAdapter", "Error checking worker document: " + e.getMessage()));
                }
            }).addOnFailureListener(e -> Log.e("NotificationAdapter", "Error checking client document: " + e.getMessage()));
        }
    }
}
