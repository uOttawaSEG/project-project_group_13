package com.example.otams.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Session;
import com.example.otams.databinding.FragmentStudentBinding;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

;

public class fragment_student extends Fragment {
    private FragmentStudentBinding binding;
    private FirebaseManager firebaseManager;
    private StudentSessionAdapter adapter;
    private List<Session> studentSessions = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        adapter = new StudentSessionAdapter(this.requireContext(), studentSessions, new StudentSessionAdapter.SessionClickListener() {
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

        firebaseManager.cancelSession(session.getSessionId(), studentId, new FirebaseManager.StudentStatusCallback() {
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

        showRatingDialog(session.getTutor());
    }

    private void showRatingDialog(String tutorId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Find views
        TextView tutorNameView = dialogView.findViewById(R.id.tutor_name);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button submitButton = dialogView.findViewById(R.id.submit_button);

        // Load tutor name
        tutorNameView.setText("Tutor: Loading...");
        firebaseManager.getTutorNameTask(tutorId).addOnSuccessListener(tutorName -> {
            tutorNameView.setText("Tutor: " + tutorName);
        }).addOnFailureListener(e -> {
            tutorNameView.setText("Tutor: Error");
        });

        // Set button click listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        submitButton.setOnClickListener(v -> {
            // This won't work without implementing rating selection
            Toast.makeText(requireContext(), "Select a rating first", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void submitRating(String tutorId, int rating) {
        String studentId = firebaseManager.getCurrentUser().getUid();

        firebaseManager.rateTutor(tutorId, studentId, rating, new FirebaseManager.StudentStatusCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to submit rating", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToSearch() {
        // Navigate to search fragment
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_fragment_student_to_fragment_student_expanded);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuUtils.setupLogoutMenu(this, firebaseManager);
    }

    public static class StudentSessionAdapter extends RecyclerView.Adapter<StudentSessionAdapter.SessionViewHolder> {
        private final List<Session> sessions;
        private final SessionClickListener listener;
        private final Context context;


        public StudentSessionAdapter(Context context, List<Session> sessions, SessionClickListener listener) {
            this.sessions = sessions;
            this.listener = listener;
            this.context = context;
        }

        @NonNull
        @Override
        public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_session, parent, false);
            return new SessionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
            holder.bind(sessions.get(position));
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        // Helper methods for formatting
        private String formatDate(java.util.Date date) {
            return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
        }

        private String formatTime(java.util.Date date) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
        }

        private String getStatusText(String status) {
            switch (status) {
                case "accepted":
                    return "Approved";
                case "pending":
                    return "Pending";
                case "rejected":
                    return "Rejected";
                case "cancelled":
                    return "Cancelled";
                default:
                    return "Unknown";
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "accepted":
                    return 0xFFD4EDDA; // Green
                case "pending":
                    return 0xFFFFF3CD;  // Yellow
                case "rejected":
                    return 0xFFF8D7DA; // Red
                case "cancelled":
                    return 0xFFE2E3E5; // Gray
                default:
                    return 0xFFF8F9FA; // Light gray
            }
        }

        public interface SessionClickListener {
            void onCancelClick(Session session);

            void onRateClick(Session session);
        }

        class SessionViewHolder extends RecyclerView.ViewHolder {
            private final TextView courseCode, date, time, status, tutorName;
            private final Button cancelBtn, rateBtn;

            public SessionViewHolder(@NonNull View itemView) {
                super(itemView);
                courseCode = itemView.findViewById(R.id.session_course_code);
                date = itemView.findViewById(R.id.session_date);
                time = itemView.findViewById(R.id.session_time);
                status = itemView.findViewById(R.id.session_status);
                tutorName = itemView.findViewById(R.id.tutor_name);
                cancelBtn = itemView.findViewById(R.id.cancel_button);
                rateBtn = itemView.findViewById(R.id.rate_button);
            }

            public void bind(Session session) {
                FirebaseUser user = FirebaseManager.getInstance().getCurrentUser();
                String studentId = "";
                if (user != null) {
                    studentId = user.getUid();
                } else {
                    Log.e("Firebase", "User is not logged in");
                }


                courseCode.setText(session.getCourseCode());
                date.setText(formatDate(session.getStartTime().toDate()));
                time.setText(formatTime(session.getStartTime().toDate()));

                String studentStatus = session.getStudentStatus(studentId);
                status.setText(getStatusText(studentStatus));
                status.setBackgroundColor(getStatusColor(studentStatus));

                // Load tutor name
                loadTutorName(session.getTutor());

                // Show/hide buttons based on status and time
                boolean showCancel = session.canStudentCancel(studentId);
                boolean showRate = "accepted".equals(studentStatus) && session.getStartTime().toDate().before(new Date(System.currentTimeMillis()));

                cancelBtn.setVisibility(showCancel ? View.VISIBLE : View.GONE);
                rateBtn.setVisibility(showRate ? View.VISIBLE : View.GONE);

                cancelBtn.setOnClickListener(v -> listener.onCancelClick(session));
                rateBtn.setOnClickListener(v -> listener.onRateClick(session));
            }

            private void loadTutorName(String tutorId) {
                // Use FirebaseManager to get tutor name
                TextView tutorNameView = itemView.findViewById(R.id.tutor_name);
                tutorNameView.setText("Tutor: Loading...");

                FirebaseManager firebaseManager = FirebaseManager.getInstance();
                firebaseManager.getTutorNameTask(tutorId).addOnSuccessListener(tutorName -> {
                    tutorNameView.setText("Tutor: " + tutorName);
                }).addOnFailureListener(e -> {
                    tutorNameView.setText("Tutor: Error");
                });
            }
        }

    }
}
