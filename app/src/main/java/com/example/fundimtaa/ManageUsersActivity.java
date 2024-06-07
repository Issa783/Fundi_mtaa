package com.example.fundimtaa;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private List<User> clientsList;
    private List<User> workersList;
    private UsersAdapter usersAdapter;
    private RecyclerView recyclerViewUsers;
    private Button buttonDeleteUser;
    private User selectedUser; // Keep track of the selected user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        db = FirebaseFirestore.getInstance();
        clientsList = new ArrayList<>();
        workersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(clientsList, workersList, this::onUserItemClick);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUsers.setAdapter(usersAdapter);

        loadUsers();

        buttonDeleteUser = findViewById(R.id.buttonDeleteUser);
        buttonDeleteUser.setVisibility(View.GONE); // Hide the delete button initially

        buttonDeleteUser.setOnClickListener(v -> {
            if (selectedUser != null) {
                deleteUser(selectedUser.getUserId());
            } else {
                Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsers() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                clientsList.clear();
                workersList.clear();
                QuerySnapshot usersSnapshot = task.getResult();
                for (DocumentSnapshot document : usersSnapshot) {
                    User user = document.toObject(User.class);
                    user.setUserId(document.getId());
                    if ("client".equals(user.getRole())) {
                        clientsList.add(user);
                    } else if ("worker".equals(user.getRole())) {
                        workersList.add(user);
                    }
                }
                usersAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(ManageUsersActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteUser(String userId) {
        db.collection("users").document(userId).delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ManageUsersActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
                selectedUser = null; // Reset the selected user
                buttonDeleteUser.setVisibility(View.GONE); // Hide the delete button
            } else {
                Toast.makeText(ManageUsersActivity.this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onUserItemClick(User user) {
        selectedUser = user;
        buttonDeleteUser.setVisibility(View.VISIBLE); // Show the delete button when a user is selected
    }
}
