//Claude made changes to this file

package com.example.otams.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
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

        // Setup toolbar with back button
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel); // Built-in back icon
        toolbar.setNavigationOnClickListener(v -> {
            // Sign out and navigate to login
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
        });

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
                        String uid = document.getId();
                        String email = document.getString("email");
                        String username = document.getString("username");

                        // Use email if available, otherwise use username
                        String displayEmail = email != null ? email : username;

                        // Get name fields - Firestore uses lowercase property names from getters
                        String firstName = document.getString("first_Name");
                        if (firstName == null)
                            firstName = document.getString("First_Name");

                        String lastName = document.getString("last_Name");
                        if (lastName == null)
                            lastName = document.getString("Last_Name");

                        String phoneNumber = document.getString("phone_Number");
                        if (phoneNumber == null)
                            phoneNumber = document.getString("Phone_Number");

                        // Get role - handle both enum name and old string format
                        Object roleObj = document.get("role");
                        String role = roleObj != null ? roleObj.toString() : "UNKNOWN";

                        // Skip ADMIN users
                        if ("ADMIN".equals(role)) {
                            continue;
                        }

                        // Get status - handle both enum name and old string format
                        Object statusObj = document.get("status");
                        String status = statusObj != null ? statusObj.toString() : "ACTIVE";

                        // Get role-specific information (exclude password)
                        String additionalInfo = "";
                        if ("STUDENT".equals(role)) {
                            String program = document.getString("program_Of_Study");
                            if (program == null)
                                program = document.getString("Program_Of_Study");
                            if (program != null) {
                                additionalInfo = "Program: " + program;
                            }
                        } else if ("TUTOR".equals(role)) {
                            String courses = document.getString("courses_Offered");
                            if (courses == null)
                                courses = document.getString("Courses_Offered");

                            String degree = document.getString("highest_Degree");
                            if (degree == null)
                                degree = document.getString("Highest_Degree");

                            if (courses != null && degree != null) {
                                additionalInfo = "Courses: " + courses + "\nDegree: " + degree;
                            } else if (courses != null) {
                                additionalInfo = "Courses: " + courses;
                            } else if (degree != null) {
                                additionalInfo = "Degree: " + degree;
                            }
                        }

                        usersList.add(new UserInfo(uid,
                                displayEmail,
                                firstName,
                                lastName,
                                phoneNumber,
                                role,
                                status,
                                additionalInfo));
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
        String firstName;
        String lastName;
        String phoneNumber;
        String role;
        String status;
        String additionalInfo;

        UserInfo(String uid, String email, String firstName, String lastName, String phoneNumber,
                String role, String status, String additionalInfo) {
            this.uid = uid;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.role = role;
            this.status = status;
            this.additionalInfo = additionalInfo;
        }
    }

    // Adapter for RecyclerView
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
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

            // Display full name
            String fullName = "Name: ";
            if (user.firstName != null && user.lastName != null) {
                fullName += user.firstName + " " + user.lastName;
            } else if (user.firstName != null) {
                fullName += user.firstName;
            } else if (user.lastName != null) {
                fullName += user.lastName;
            } else {
                fullName += "N/A";
            }
            holder.nameTextView.setText(fullName);

            // Display phone number
            String phone = "Phone: " + (user.phoneNumber != null ? user.phoneNumber : "N/A");
            holder.phoneTextView.setText(phone);

            holder.roleTextView.setText("Role: " + user.role);

            // Display additional info (program for students, courses/degree for tutors)
            if (user.additionalInfo != null && !user.additionalInfo.isEmpty()) {
                holder.additionalInfoTextView.setText(user.additionalInfo);
                holder.additionalInfoTextView.setVisibility(View.VISIBLE);
            } else {
                holder.additionalInfoTextView.setVisibility(View.GONE);
            }

            holder.statusTextView.setText("Status: " + user.status);

            // Set status text color
            if ("PENDING".equals(user.status)) {
                holder.statusTextView
                        .setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_orange_dark));
                holder.actionButtons.setVisibility(View.VISIBLE);
            } else if ("ACTIVE".equals(user.status)) {
                holder.statusTextView
                        .setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
                holder.actionButtons.setVisibility(View.GONE);
            } else if ("SUSPENDED".equals(user.status)) {
                holder.statusTextView
                        .setTextColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
                holder.actionButtons.setVisibility(View.GONE);
            }

            // Handle approve button
            holder.approveButton.setOnClickListener(v -> {
                updateUserStatus(user.uid, "ACTIVE");
            });

            // Handle reject button
            holder.rejectButton.setOnClickListener(v -> {
                updateUserStatus(user.uid, "SUSPENDED");
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView emailTextView;
            TextView nameTextView;
            TextView phoneTextView;
            TextView roleTextView;
            TextView additionalInfoTextView;
            TextView statusTextView;
            LinearLayout actionButtons;
            Button approveButton;
            Button rejectButton;

            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                emailTextView = itemView.findViewById(R.id.user_email);
                nameTextView = itemView.findViewById(R.id.user_name);
                phoneTextView = itemView.findViewById(R.id.user_phone);
                roleTextView = itemView.findViewById(R.id.user_role);
                additionalInfoTextView = itemView.findViewById(R.id.user_additional_info);
                statusTextView = itemView.findViewById(R.id.user_status);
                actionButtons = itemView.findViewById(R.id.action_buttons);
                approveButton = itemView.findViewById(R.id.approve_button);
                rejectButton = itemView.findViewById(R.id.reject_button);
            }
        }
    }

    private void updateUserStatus(String uid, String newStatus) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Simply update the status field in Firestore
        db.collection("users")
                .document(uid)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    String message = "ACTIVE".equals(newStatus) ? "User approved and activated"
                            : "User status updated to " + newStatus;
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Error updating status: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
