package com.example.otams.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Session;
import com.example.otams.databinding.FragmentStudentExpandedBinding;

import java.util.ArrayList;
import java.util.List;

public class fragment_student_expanded extends Fragment {
    private FragmentStudentExpandedBinding binding;
    private FirebaseManager firebaseManager;
    private AvailableSessionsAdapter adapter;
    private List<Session> availableSessions = new ArrayList<>();
    private Session currentSession; // Add this to fix the error in sendSessionRequest

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentExpandedBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Search Sessions");
        firebaseManager = FirebaseManager.getInstance();

        setupRecyclerView();
        setupSearchFunctionality();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new AvailableSessionsAdapter(availableSessions, new AvailableSessionsAdapter.SessionClickListener() {
            @Override
            public void onRequestClick(Session session) {
                requestSession(session);
            }
        });
        binding.searchResultsRecyclerView.setAdapter(adapter);
        binding.searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearchFunctionality() {
        binding.searchButton.setOnClickListener(v -> performSearch());

        // Add real-time search as user types
        binding.courseSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 3) {
                    performSearch();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Enable search on enter key
        binding.courseSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });
    }

    private void performSearch() {
        String courseCode = binding.courseSearchInput.getText().toString().trim();

        if (courseCode.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a course code", Toast.LENGTH_SHORT).show();
            return;
        }

        String studentId = firebaseManager.getCurrentUser().getUid();

        binding.searchResultsRecyclerView.setVisibility(View.GONE);
        binding.emptyState.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        firebaseManager.searchSessionsByCourse(courseCode, studentId,
                new FirebaseManager.SessionFetchCallback() {
                    @Override
                    public void onSessionsFetched(List<Session> sessions) {
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);

                            availableSessions.clear();
                            availableSessions.addAll(sessions);
                            adapter.notifyDataSetChanged();

                            if (sessions.isEmpty()) {
                                binding.emptyState.setVisibility(View.VISIBLE);
                                binding.searchResultsRecyclerView.setVisibility(View.GONE);
                                binding.emptyStateText.setText("No available sessions found for " + courseCode);
                            } else {
                                binding.emptyState.setVisibility(View.GONE);
                                binding.searchResultsRecyclerView.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void requestSession(Session session) {
        String studentId = firebaseManager.getCurrentUser().getUid();

        // Check for time conflicts first
        checkForTimeConflicts(session, studentId);
    }

    private void checkForTimeConflicts(Session targetSession, String studentId) {
        // Store the session for later use
        currentSession = targetSession;

        // Get all sessions where student is already enrolled or pending
        firebaseManager.fetchStudentSessions(studentId, new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> studentSessions) {
                requireActivity().runOnUiThread(() -> {
                    boolean hasConflict = false;

                    for (Session existingSession : studentSessions) {
                        // Check if student is in accepted or pending status
                        String status = existingSession.getStudentStatus(studentId);
                        if ("accepted".equals(status) || "pending".equals(status)) {
                            // Check for time overlap
                            if (hasTimeConflict(existingSession, targetSession)) {
                                hasConflict = true;
                                break;
                            }
                        }
                    }

                    if (hasConflict) {
                        Toast.makeText(requireContext(),
                                "This session conflicts with an existing booking",
                                Toast.LENGTH_LONG).show();
                    } else {
                        confirmSessionRequest(targetSession, studentId);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                requireActivity().runOnUiThread(() -> {
                    // If we can't check conflicts, still allow the request but warn user
                    Toast.makeText(requireContext(),
                            "Unable to check for conflicts. Proceed with caution.",
                            Toast.LENGTH_SHORT).show();
                    confirmSessionRequest(targetSession, studentId);
                });
            }
        });
    }

    private boolean hasTimeConflict(Session session1, Session session2) {
        return session1.getStartTime().toDate().before(session2.getEndTime().toDate()) &&
                session2.getStartTime().toDate().before(session1.getEndTime().toDate());
    }

    private void confirmSessionRequest(Session session, String studentId) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Request Session")
                .setMessage("Request session for " + session.getCourseCode() + "?\n" +
                        "Date: " + formatDate(session.getStartTime().toDate()) + "\n" +
                        "Time: " + formatTime(session.getStartTime().toDate()) + " - " +
                        formatTime(session.getEndTime().toDate()))
                .setPositiveButton("Request", (dialog, which) -> {
                    sendSessionRequest(session.getSessionId(), studentId, session);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendSessionRequest(String sessionId, String studentId, Session session) {
        firebaseManager.requestSession(sessionId, studentId,
                new FirebaseManager.StudentStatusCallback() {
                    @Override
                    public void onSuccess() {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    session.isAutoApprove() ?
                                            "Session booked successfully!" :
                                            "Session request submitted. Waiting for tutor approval.",
                                    Toast.LENGTH_LONG).show();

                            // Remove the session from search results
                            for (int i = 0; i < availableSessions.size(); i++) {
                                if (availableSessions.get(i).getSessionId().equals(sessionId)) {
                                    availableSessions.remove(i);
                                    adapter.notifyItemRemoved(i);
                                    break;
                                }
                            }

                            // Show empty state if no more sessions
                            if (availableSessions.isEmpty()) {
                                binding.emptyState.setVisibility(View.VISIBLE);
                                binding.searchResultsRecyclerView.setVisibility(View.GONE);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "Failed to request session: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuUtils.setupLogoutMenu(this, firebaseManager);
    }

    // Helper formatting methods
    private String formatDate(java.util.Date date) {
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date);
    }

    private String formatTime(java.util.Date date) {
        return new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date);
    }
}