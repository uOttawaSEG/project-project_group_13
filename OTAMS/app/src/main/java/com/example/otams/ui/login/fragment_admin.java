package com.example.otams.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class fragment_admin extends Fragment {

    private RecyclerView usersRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyView;
    private UserAdapter userAdapter;
    private List<UserInfo> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Initialize views
        usersRecyclerView = view.findViewById(R.id.users_list);
        loadingProgressBar = view.findViewById(R.id.loading);
        emptyView = view.findViewById(R.id.empty_view);

        // Setup RecyclerView
        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(usersList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(userAdapter);

        // Load users from Firebase
        loadUsers();
    }

    private void loadUsers() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    usersList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getString("uid");
                        String email = document.getString("email");
                        String role = document.getString("role");

                        usersList.add(new UserInfo(uid, email, role));
                    }

                    if (usersList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        userAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    // UserInfo class to hold user data
    private static class UserInfo {
        String uid;
        String email;
        String role;

        UserInfo(String uid, String email, String role) {
            this.uid = uid;
            this.email = email;
            this.role = role;
        }
    }

    // Adapter for RecyclerView
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<UserInfo> users;

        UserAdapter(List<UserInfo> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserInfo user = users.get(position);
            holder.emailTextView.setText(user.email);
            holder.roleTextView.setText("Role: " + user.role);
            holder.uidTextView.setText("UID: " + user.uid);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {
            TextView emailTextView;
            TextView roleTextView;
            TextView uidTextView;

            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                emailTextView = itemView.findViewById(R.id.user_email);
                roleTextView = itemView.findViewById(R.id.user_role);
                uidTextView = itemView.findViewById(R.id.user_uid);
            }
        }
    }
}
