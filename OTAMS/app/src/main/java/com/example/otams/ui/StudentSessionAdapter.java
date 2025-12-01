package com.example.otams.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.example.otams.data.Session;
import com.google.type.DateTime;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class StudentSessionAdapter extends RecyclerView.Adapter<StudentSessionAdapter.SessionViewHolder> {
    private final List<Session> sessions;
    private final SessionClickListener listener;

    public StudentSessionAdapter(List<Session> sessions, SessionClickListener listener) {
        this.sessions = sessions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_session, parent, false);
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
            String studentId = ""; // Get current student ID

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
            boolean showRate = "accepted".equals(studentStatus) &&
                    session.getStartTime().toDate().before(new Date(System.currentTimeMillis()));

            cancelBtn.setVisibility(showCancel ? View.VISIBLE : View.GONE);
            rateBtn.setVisibility(showRate ? View.VISIBLE : View.GONE);

            cancelBtn.setOnClickListener(v -> listener.onCancelClick(session));
            rateBtn.setOnClickListener(v -> listener.onRateClick(session));
        }

        private void loadTutorName(String tutorId) {
            // Use FirebaseManager to get tutor name
            tutorName.setText("Loading...");
        }
    }
}