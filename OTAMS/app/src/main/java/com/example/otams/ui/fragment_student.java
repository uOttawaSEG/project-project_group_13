package com.example.otams.ui;

import android.os.Bundle;
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
import com.example.otams.databinding.FragmentStudentBinding;

import java.util.ArrayList;
import java.util.List;

;

public class fragment_student extends Fragment {
    private FragmentStudentBinding binding;
    private FirebaseManager firebaseManager;
    private StudentSessionAdapter adapter;
    private List<Session> studentSessions = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStudentBinding.inflate(inflater, container, false);
        requireActivity().setTitle("My Sessions");
        firebaseManager = FirebaseManager.getInstance();

        setupRecyclerView();
        loadStudentSessions();
        binding.searchSessionsButton.setOnClickListener(v -> navigateToSearch());
        binding.swipeRefresh.setOnRefreshListener(this::loadStudentSessions);

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new StudentSessionAdapter(studentSessions, new StudentSessionAdapter.SessionClickListener() {
            @Override
            public void onCancelClick(Session session) {
                cancelSession(session);
            }

            @Override
            public void onRateClick(Session session) {
                rateTutor(session);
            }
        });
        binding.sessionsRecyclerView.setAdapter(adapter);
        binding.sessionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadStudentSessions() {
        String studentId = firebaseManager.getCurrentUser().getUid();

        binding.swipeRefresh.setRefreshing(true);
        firebaseManager.fetchStudentSessions(studentId, new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> sessions) {
                binding.swipeRefresh.setRefreshing(false);
                studentSessions.clear();
                studentSessions.addAll(sessions);
                adapter.notifyDataSetChanged();

                if (sessions.isEmpty()) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.sessionsRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.GONE);
                    binding.sessionsRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Exception e) {
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load sessions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelSession(Session session) {
        String studentId = firebaseManager.getCurrentUser().getUid();

        if (!session.canStudentCancel(studentId)) {
            Toast.makeText(requireContext(), "Cannot cancel this session", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.cancelSession(session.getSessionId(), studentId,
                new FirebaseManager.StudentStatusCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Session cancelled", Toast.LENGTH_SHORT).show();
                        loadStudentSessions();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(requireContext(), "Failed to cancel session", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void rateTutor(Session session) {
        // Implement rating dialog
        showRatingDialog(session.getTutor());
    }

    private void showRatingDialog(String tutorId) {
        // Create rating dialog with 1-5 stars
        // After rating, call firebaseManager.rateTutor()
    }

    private void navigateToSearch() {
        // Navigate to search fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuUtils.setupLogoutMenu(this, firebaseManager);
    }
}