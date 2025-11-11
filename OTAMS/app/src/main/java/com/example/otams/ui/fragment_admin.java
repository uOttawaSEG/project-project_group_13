//Claude made changes to this file

package com.example.otams.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.databinding.FragmentAdminBinding;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class fragment_admin extends Fragment {

    private FirebaseManager firebaseManager;
    private FragmentAdminBinding binding;
    private RecyclerView usersRecyclerView;
    private ProgressBar loadingProgressBar;
    private TextView emptyView;
    private UserAdapter userAdapter;
    private List<UserInfo> usersList;
    private boolean showingRejected = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Admin");
        firebaseManager = FirebaseManager.getInstance();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuUtils.setupLogoutMenu(this, firebaseManager);

        usersRecyclerView = view.findViewById(R.id.users_list);
        loadingProgressBar = view.findViewById(R.id.loading);
        emptyView = view.findViewById(R.id.empty_view);

        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(usersList);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersRecyclerView.setAdapter(userAdapter);

        loadUsersByStatus("PENDING");

        RadioGroup roleGroup = view.findViewById(R.id.criteria);

        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            clearView();
            if (checkedId == R.id.radio_pending) {
                showingRejected = false;
                loadUsersByStatus("PENDING");
            } else if (checkedId == R.id.radio_rejected) {
                showingRejected = true;
                loadUsersByStatus("REJECTED");
            }

        });
    }

    private void clearView() {
        // Clear current data
        usersList.clear();
        userAdapter.notifyDataSetChanged();

        // Hide empty view and show loading (optional)
        emptyView.setVisibility(View.GONE);
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void loadUsersByStatus(String status) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        firebaseManager.getFirestore().collection("users").whereEqualTo("status", status).addSnapshotListener((querySnapshot, e) -> {
            // Handle errors first
            if (e != null) {
                loadingProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            // Handle null snapshot (happens if listener is removed or query is invalid)
            if (querySnapshot == null) {
                loadingProgressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(R.string.no_data_available);
                return;
            }

            loadingProgressBar.setVisibility(View.GONE);

            usersList.clear();

            for (QueryDocumentSnapshot document : querySnapshot) {
                String uid = document.getId();
                String email = document.getString("email");
                String firstName = document.getString("first_name");
                String lastName = document.getString("last_name");
                String phoneNumber = document.getString("phone_number");
                String role = document.getString("role");
                String additionalInfo = "";
                if (Objects.equals(role, "STUDENT")) {
                    additionalInfo = document.getString("program_of_study");
                } else if (Objects.equals(role, "TUTOR")) {
                    additionalInfo = document.getString("courses_offered") + "\n" + document.getString("highest_degree");

                }
                usersList.add(new UserInfo(uid, email, firstName, lastName, phoneNumber, role, status, additionalInfo));
            }

            if (usersList.isEmpty()) {
                emptyView.setText(showingRejected ? "No rejected users found." : "No pending users found.");
                emptyView.setVisibility(View.VISIBLE);
            } else {
                userAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updateUserStatus(String uid, String newStatus) {
        firebaseManager.getFirestore().collection("users").document(uid).update("status", newStatus).addOnSuccessListener(aVoid -> {
            String message = "APPROVED".equals(newStatus) ? "User approved and activated" : "User status updated to " + newStatus;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            loadUsersByStatus(showingRejected ? "REJECTED" : "PENDING");
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error updating status: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private static class UserInfo {
        String uid;
        String email;
        String firstName;
        String lastName;
        String phoneNumber;
        String role;
        String status;
        String additionalInfo;

        UserInfo(String uid, String email, String firstName, String lastName, String phoneNumber, String role, String status, String additionalInfo) {
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

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        private final List<UserInfo> users;

        UserAdapter(List<UserInfo> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserInfo user = users.get(position);
            holder.emailTextView.setText(user.email);
            holder.nameTextView.setText(String.format("Name: %s %s", user.firstName, user.lastName));
            holder.phoneTextView.setText(String.format("Phone: %s", user.phoneNumber));
            holder.roleTextView.setText(String.format("Role: %s", user.role));
            holder.statusTextView.setText(String.format("Status: %s", user.status));

            if ("PENDING".equals(user.status)) {
                holder.actionButtons.setVisibility(View.VISIBLE);
                holder.approveButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);
                holder.approveButton.setOnClickListener(v -> {
                    updateUserStatus(user.uid, "APPROVED");
                    clearView();
                });
                holder.rejectButton.setOnClickListener(v -> {
                    updateUserStatus(user.uid, "REJECTED");
                    clearView();
                });
            } else if ("REJECTED".equals(user.status)) {
                holder.actionButtons.setVisibility(View.VISIBLE);
                holder.approveButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.GONE);
                holder.approveButton.setOnClickListener(v -> {
                    updateUserStatus(user.uid, "APPROVED");
                    clearView();
                });
            }
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
                statusTextView = itemView.findViewById(R.id.user_status);
                actionButtons = itemView.findViewById(R.id.action_buttons);
                approveButton = itemView.findViewById(R.id.approve_button);
                rejectButton = itemView.findViewById(R.id.reject_button);
            }
        }
    }
}
