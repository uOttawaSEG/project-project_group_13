package com.example.otams.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Session;
import com.example.otams.databinding.FragmentTutorExpandedBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;

import java.util.List;
import java.util.Objects;

public class fragment_tutor_expanded extends Fragment {
    private FirebaseManager firebaseManager;
    private FragmentTutorExpandedBinding binding;
    private Session currentSession;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTutorExpandedBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Session Details");
        firebaseManager = FirebaseManager.getInstance();

        // Get session from arguments
        Bundle args = getArguments();
        if (args != null) {
            currentSession = (Session) args.getParcelable("session");
            if (currentSession != null) {
                displaySessionDetails();
                loadStudentLists();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuUtils.setupLogoutMenu(this, firebaseManager);
    }

    private void displaySessionDetails() {
        if (currentSession == null) return;

        binding.sessionCourseCode.setText(currentSession.getCourseCode());
        binding.sessionLocation.setText(currentSession.getLocation());
        binding.sessionDate.setText(formatDate(currentSession.getStartTime().toDate()));
        binding.sessionTime.setText(formatTimeRange(currentSession.getStartTime().toDate(), currentSession.getEndTime().toDate()));
        binding.autoApproveStatus.setText(currentSession.isAutoApprove() ? "Auto Approve: Enabled" : "Auto Approve: Disabled");
    }

    private void loadStudentLists() {
        if (currentSession == null) return;

        // Load pending students
        loadStudentsByStatus(currentSession.getPendingStudents(), binding.pendingStudentsContainer, "Pending Students", true);

        // Load accepted students
        loadStudentsByStatus(currentSession.getAcceptedStudents(), binding.acceptedStudentsContainer, "Accepted Students", false);

        // Load rejected students
        loadStudentsByStatus(currentSession.getRejectedStudents(), binding.rejectedStudentsContainer, "Rejected Students", false);
    }

    private void loadStudentsByStatus(List<String> studentIds, LinearLayout container, String sectionTitle, boolean showActions) {
        container.removeAllViews();

        if (studentIds == null || studentIds.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("No " + sectionTitle.toLowerCase());
            emptyText.setPadding(16, 16, 16, 16);
            container.addView(emptyText);
            return;
        }

        // Add section title
        TextView title = new TextView(requireContext());
        title.setText(sectionTitle);
        title.setTextSize(18);
        title.setPadding(16, 16, 16, 8);
        container.addView(title);

        firebaseManager.getFirestore().collection("users").whereIn(FieldPath.documentId(), studentIds).get().addOnSuccessListener(querySnapshot -> {
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                String studentId = doc.getId();
                String firstName = doc.getString("first_name");
                String lastName = doc.getString("last_name");
                String email = doc.getString("email");

                String displayName = formatDisplayName(firstName, lastName, email);
                addStudentToContainer(container, studentId, displayName, showActions);
            }
        }).addOnFailureListener(e -> {
            Log.e("TutorExpanded", "Error loading students: " + e.getMessage());
            Toast.makeText(requireContext(), "Failed to load students", Toast.LENGTH_SHORT).show();
        });
    }

    private void addStudentToContainer(LinearLayout container, String studentId, String displayName, boolean showActions) {
        LinearLayout studentLayout = new LinearLayout(requireContext());
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);
        studentLayout.setPadding(16, 8, 16, 8);

        TextView studentName = new TextView(requireContext());
        studentName.setText(displayName);
        studentName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        studentLayout.addView(studentName);

        if (showActions) {
            Button acceptBtn = new Button(requireContext());
            acceptBtn.setText("Accept");
            acceptBtn.setOnClickListener(v -> acceptStudent(studentId));
            studentLayout.addView(acceptBtn);

            Button rejectBtn = new Button(requireContext());
            rejectBtn.setText("Reject");
            rejectBtn.setOnClickListener(v -> rejectStudent(studentId));
            studentLayout.addView(rejectBtn);
        }

        container.addView(studentLayout);
    }

    private void acceptStudent(String studentId) {
        if (currentSession == null) return;

        firebaseManager.acceptStudentToSession(currentSession.getSessionId(), studentId, new FirebaseManager.StudentStatusCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Student accepted", Toast.LENGTH_SHORT).show();
                refreshStudentLists();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to accept student", Toast.LENGTH_SHORT).show();
                Log.e("TutorExpanded", "Error accepting student: " + e.getMessage());
            }
        });
    }

    private void rejectStudent(String studentId) {
        if (currentSession == null) return;

        firebaseManager.rejectStudentFromSession(currentSession.getSessionId(), studentId, new FirebaseManager.StudentStatusCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Student rejected", Toast.LENGTH_SHORT).show();
                refreshStudentLists();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to reject student", Toast.LENGTH_SHORT).show();
                Log.e("TutorExpanded", "Error rejecting student: " + e.getMessage());
            }
        });
    }

    private void refreshStudentLists() {
        // Reload the session data to get updated student lists
        firebaseManager.getFirestore().collection("sessions").document(currentSession.getSessionId()).get().addOnSuccessListener(documentSnapshot -> {
            Session updatedSession = documentSnapshot.toObject(Session.class);
            if (updatedSession != null) {
                currentSession = updatedSession;
                loadStudentLists();
            }
        }).addOnFailureListener(e -> {
            Log.e("TutorExpanded", "Error refreshing session: " + e.getMessage());
        });
    }

    // Helper methods for formatting
    private String formatDisplayName(String firstName, String lastName, String email) {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return Objects.requireNonNullElse(email, "Unknown Student");
        }
    }

    private String formatDate(java.util.Date date) {
        return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(date);
    }

    private String formatTimeRange(java.util.Date start, java.util.Date end) {
        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return timeFormat.format(start) + " - " + timeFormat.format(end);
    }
}