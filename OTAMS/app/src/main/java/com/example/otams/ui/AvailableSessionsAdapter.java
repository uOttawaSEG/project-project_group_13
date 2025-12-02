package com.example.otams.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Session;
import com.example.otams.data.Tutor;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AvailableSessionsAdapter extends RecyclerView.Adapter<AvailableSessionsAdapter.SessionViewHolder> {
    private final List<Session> sessions;
    private final SessionClickListener listener;
    private final FirebaseManager firebaseManager;

    public AvailableSessionsAdapter(List<Session> sessions, SessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
        this.firebaseManager = FirebaseManager.getInstance();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_session, parent, false);
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

    class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView courseCode, sessionDate, sessionTime, sessionLocation;
        private final TextView tutorName, tutorRating, autoApproveIndicator;
        private final Button requestButton;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCode = itemView.findViewById(R.id.course_code);
            sessionDate = itemView.findViewById(R.id.session_date);
            sessionTime = itemView.findViewById(R.id.session_time);
            sessionLocation = itemView.findViewById(R.id.session_location);
            tutorName = itemView.findViewById(R.id.tutor_name);
            tutorRating = itemView.findViewById(R.id.tutor_rating);
            autoApproveIndicator = itemView.findViewById(R.id.auto_approve_indicator);
            requestButton = itemView.findViewById(R.id.request_button);
        }

        public void bind(Session session) {
            courseCode.setText(session.getCourseCode());
            sessionDate.setText(formatDate(session.getStartTime().toDate()));
            sessionTime.setText(formatTimeRange(session.getStartTime().toDate(),
                    session.getEndTime().toDate()));
            sessionLocation.setText("Location: " + session.getLocation());

            // Show auto-approve indicator
            if (session.isAutoApprove()) {
                autoApproveIndicator.setVisibility(View.VISIBLE);
            } else {
                autoApproveIndicator.setVisibility(View.GONE);
            }

            // Load tutor information
            loadTutorInfo(session.getTutor());

            requestButton.setOnClickListener(v -> listener.onRequestClick(session));
        }

        private void loadTutorInfo(String tutorId) {
            tutorName.setText("Tutor: Loading...");
            tutorRating.setText("Rating: Loading...");

            firebaseManager.getTutorWithRating(tutorId, new FirebaseManager.TutorFetchCallback() {
                @Override
                public void onTutorFetched(Tutor tutor) {
                    String name = tutor.getFirst_name() + " " + tutor.getLast_name();
                    tutorName.setText("Tutor: " + name);

                    double avgRating = tutor.getAverage_rating();
                    int ratingCount = tutor.getRating_count();

                    if (ratingCount > 0) {
                        tutorRating.setText(String.format("Rating: %.1f/5 (%d reviews)",
                                avgRating, ratingCount));
                    } else {
                        tutorRating.setText("No ratings yet");
                    }
                }

                @Override
                public void onError(Exception e) {
                    tutorName.setText("Tutor: Unknown");
                    tutorRating.setText("Rating: Unavailable");
                }
            });
        }
    }

    public interface SessionClickListener {
        void onRequestClick(Session session);
    }

    // Helper formatting methods
    private String formatDate(java.util.Date date) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(date);
    }

    private String formatTimeRange(java.util.Date start, java.util.Date end) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(start) + " - " + timeFormat.format(end);
    }
}