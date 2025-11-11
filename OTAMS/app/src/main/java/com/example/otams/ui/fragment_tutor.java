package com.example.otams.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Session;
import com.example.otams.data.Tutor;
import com.example.otams.databinding.FragmentTutorBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class fragment_tutor extends Fragment {
    private ArrayList<Session> future_sessions = new ArrayList<>();
    private ArrayList<Session> past_sessions = new ArrayList<>();
    private FirebaseManager firebaseManager;
    private FragmentTutorBinding binding;
    private SessionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTutorBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Tutor");

        firebaseManager = FirebaseManager.getInstance();

        setupCreateAvailabilityButton();
        setupRecyclerView();
        setupRadioGroupListener();
        loadSessions();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuUtils.setupLogoutMenu(this, firebaseManager);
    }

    private void setupRadioGroupListener() {
        binding.sessionLabel.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_pending) {
                // Show past sessions
                adapter.updateData(past_sessions);
                loadSessions();
            } else if (checkedId == R.id.radio_rejected) {
                // Show future sessions
                adapter.updateData(future_sessions);
                loadSessions();
            }
        });
    }

    private void loadSessions() {
        past_sessions.clear();
        future_sessions.clear();

        firebaseManager.fetchPastSessions(new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> sessions) {
                past_sessions.addAll(sessions);
                if (binding.sessionLabel.getCheckedRadioButtonId() == R.id.radio_pending) {
                    adapter.updateData(past_sessions);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to load past sessions", Toast.LENGTH_SHORT).show();
            }
        });

        firebaseManager.fetchFutureSessions(new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> sessions) {
                future_sessions.addAll(sessions);
                if (binding.sessionLabel.getCheckedRadioButtonId() == R.id.radio_rejected) {
                    adapter.updateData(future_sessions);
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(requireContext(), "Failed to load future sessions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCreateAvailabilityButton() {
        binding.createAvailabilityButton.setOnClickListener(v -> showCreateAvailabilityDialog());
    }

    private void setupRecyclerView() {
        adapter = new SessionAdapter(past_sessions, new SessionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Session session) {
                // Handle click
            }

            @Override
            public void onStudentListClick(Session session) {
                showStudentListDialog(session);
            }

            @Override
            public void onEditClick(Session session) {
                showEditAvailabilityDialog(session);
            }
        });
        binding.pastSessionsList.setAdapter(adapter);
        binding.pastSessionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void onSessionClicked(Session session) {
        // Toast.makeText(requireContext(), "Clicked session: " + session.getCourse(),
        // Toast.LENGTH_SHORT).show();
        // TODO: Open session details or do something with the clicked session
    }

    private void showStudentListDialog(Session session) {
        List<String> studentIds = session.getStudents(); // your Session model should return List<String>

        if (studentIds == null || studentIds.isEmpty()) {
            Toast.makeText(requireContext(), "No students enrolled.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.getFirestore().collection("users").whereIn(FieldPath.documentId(), studentIds).get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> studentNames = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String firstName = doc.getString("first_Name");
                        String lastName = doc.getString("last_Name");
                        String email = doc.getString("username");

                        // Combine nicely even if one part is missing
                        String displayName;
                        if (firstName != null && lastName != null) {
                            displayName = firstName + " " + lastName;
                        } else if (firstName != null) {
                            displayName = firstName;
                        } else {
                            displayName = email != null ? email : "Unknown Student";
                        }

                        studentNames.add(displayName);
                    }

                    if (studentNames.isEmpty()) {
                        Toast.makeText(requireContext(), "No valid student records found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Display students in a dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Student List");
                    builder.setItems(studentNames.toArray(new String[0]), null);
                    builder.setPositiveButton("OK", null);
                    builder.show();

                }).addOnFailureListener(e -> {
                    Log.e("fragment_tutor", "Failed to load students", e);
                    Toast.makeText(requireContext(), "Failed to load student list.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditAvailabilityDialog(Session session) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Edit Availability Slot");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_slot, null);
        builder.setView(dialogView);

        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText startTimeInput = dialogView.findViewById(R.id.startTimeInput);
        EditText endTimeInput = dialogView.findViewById(R.id.endTimeInput);
        EditText subjectInput = dialogView.findViewById(R.id.subjectInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        CheckBox autoApproveCheck = dialogView.findViewById(R.id.autoApproveCheck);

        dateInput.setInputType(InputType.TYPE_NULL);
        startTimeInput.setInputType(InputType.TYPE_NULL);
        endTimeInput.setInputType(InputType.TYPE_NULL);

        // Pre-fill the fields from the session data
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        dateInput.setText(dateFormat.format(session.getStartTime().toDate()));
        startTimeInput.setText(timeFormat.format(session.getStartTime().toDate()));
        endTimeInput.setText(timeFormat.format(session.getEndTime().toDate()));
        subjectInput.setText(session.getCourseCode());
        locationInput.setText(session.getLocation());
        autoApproveCheck.setChecked(session.isAutoApprove());

        // Date picker for dateInput
        dateInput.setOnClickListener(v -> {
            final Calendar today = Calendar.getInstance();
            int year = today.get(Calendar.YEAR);
            int month = today.get(Calendar.MONTH);
            int day = today.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1,
                                selectedDay);
                        dateInput.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
            datePickerDialog.show();
        });

        // Time picker for startTimeInput
        startTimeInput.setOnClickListener(v -> {
            final Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view, selectedHour, selectedMinute) -> {
                        int snappedMinute = (selectedMinute >= 30) ? 30 : 0;
                        String formattedTime = String.format("%02d:%02d", selectedHour, snappedMinute);
                        startTimeInput.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();

            NumberPicker minutePicker = timePickerDialog
                    .findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
            if (minutePicker != null) {
                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(1);
                minutePicker.setDisplayedValues(new String[] { "00", "30" });
            }
        });

        // Time picker for endTimeInput
        endTimeInput.setOnClickListener(v -> {
            final Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view, selectedHour, selectedMinute) -> {
                        int snappedMinute = (selectedMinute >= 30) ? 30 : 0;
                        String formattedTime = String.format("%02d:%02d", selectedHour, snappedMinute);
                        endTimeInput.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();

            NumberPicker minutePicker = timePickerDialog
                    .findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
            if (minutePicker != null) {
                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(1);
                minutePicker.setDisplayedValues(new String[] { "00", "30" });
            }
        });

        builder.setPositiveButton("Update", (dialog, which) -> {
            String date = dateInput.getText().toString().trim();
            String start = startTimeInput.getText().toString().trim();
            String end = endTimeInput.getText().toString().trim();
            String course = subjectInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            boolean autoApprove = autoApproveCheck.isChecked();

            if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (end.compareTo(start) <= 0) {
                Toast.makeText(requireContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            try {
                Date startDateTime = sdf.parse(date + " " + start);
                Date endDateTime = sdf.parse(date + " " + end);

                Timestamp startTimestamp = new Timestamp(startDateTime);
                Timestamp endTimestamp = new Timestamp(endDateTime);

                // Update the session object
                session.setCourseCode(course);
                session.setAutoApprove(autoApprove);
                session.setLocation(location);
                session.setStartTime(startTimestamp);
                session.setEndTime(endTimestamp);

                // Update Firestore document using session ID (you must have it)
                firebaseManager.getFirestore().collection("sessions").document(session.getSessionId())
                        .set(session).addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Availability slot updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            // Refresh UI lists if needed:
                            if (binding.sessionLabel.getCheckedRadioButtonId() == R.id.radio_rejected) {
                                adapter.updateData(future_sessions);
                            } else {
                                adapter.updateData(past_sessions);
                            }
                        }).addOnFailureListener(e -> {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "Failed to update availability slot", Toast.LENGTH_SHORT)
                                    .show();
                        });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCreateAvailabilityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Create Availability Slot");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_slot, null);
        builder.setView(dialogView);

        EditText dateInput = dialogView.findViewById(R.id.dateInput);
        EditText startTimeInput = dialogView.findViewById(R.id.startTimeInput);
        EditText endTimeInput = dialogView.findViewById(R.id.endTimeInput);
        EditText subjectInput = dialogView.findViewById(R.id.subjectInput);
        EditText locationInput = dialogView.findViewById(R.id.locationInput);
        CheckBox autoApproveCheck = dialogView.findViewById(R.id.autoApproveCheck);

        dateInput.setInputType(InputType.TYPE_NULL);
        startTimeInput.setInputType(InputType.TYPE_NULL);
        endTimeInput.setInputType(InputType.TYPE_NULL);

        // Date picker for dateInput
        dateInput.setOnClickListener(v -> {
            final Calendar today = Calendar.getInstance();
            int year = today.get(Calendar.YEAR);
            int month = today.get(Calendar.MONTH);
            int day = today.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1,
                                selectedDay);
                        dateInput.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
            datePickerDialog.show();
        });

        // Time picker for startTimeInput
        startTimeInput.setOnClickListener(v -> {
            final Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view, selectedHour, selectedMinute) -> {
                        int snappedMinute = (selectedMinute >= 30) ? 30 : 0;
                        String formattedTime = String.format("%02d:%02d", selectedHour, snappedMinute);
                        startTimeInput.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();

            NumberPicker minutePicker = timePickerDialog
                    .findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
            if (minutePicker != null) {
                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(1);
                minutePicker.setDisplayedValues(new String[] { "00", "30" });
            }
        });

        // Time picker for endTimeInput
        endTimeInput.setOnClickListener(v -> {
            final Calendar now = Calendar.getInstance();
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                    (view, selectedHour, selectedMinute) -> {
                        int snappedMinute = (selectedMinute >= 30) ? 30 : 0;
                        String formattedTime = String.format("%02d:%02d", selectedHour, snappedMinute);
                        endTimeInput.setText(formattedTime);
                    }, hour, minute, true);

            timePickerDialog.show();

            NumberPicker minutePicker = timePickerDialog
                    .findViewById(Resources.getSystem().getIdentifier("minute", "id", "android"));
            if (minutePicker != null) {
                minutePicker.setMinValue(0);
                minutePicker.setMaxValue(1);
                minutePicker.setDisplayedValues(new String[] { "00", "30" });
            }
        });

        builder.setPositiveButton("Create", (dialog, which) -> {
            String date = dateInput.getText().toString().trim();
            String start = startTimeInput.getText().toString().trim();
            String end = endTimeInput.getText().toString().trim();
            String course = subjectInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            boolean autoApprove = autoApproveCheck.isChecked();

            if (date.isEmpty() || start.isEmpty() || end.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (end.compareTo(start) <= 0) {
                Toast.makeText(requireContext(), "End time must be after start time.", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            try {
                Date startDateTime = sdf.parse(date + " " + start);
                Date endDateTime = sdf.parse(date + " " + end);

                Timestamp startTimestamp = new Timestamp(startDateTime);
                Timestamp endTimestamp = new Timestamp(endDateTime);

                String uid = firebaseManager.getCurrentUser().getUid();

                firebaseManager.getUserProfile().addOnSuccessListener(profileObj -> {
                    if (profileObj instanceof Tutor) {
                        Session newSession = new Session(course, autoApprove, location, startTimestamp, endTimestamp,
                                uid);

                        firebaseManager.getFirestore().collection("sessions").add(newSession)
                                .addOnSuccessListener(documentReference -> {
                                    newSession.setSessionId(documentReference.getId());

                                    // Update the session document itself
                                    documentReference.update("sessionID", documentReference.getId());

                                    // Also update tutor document to include this new session
                                    firebaseManager.getFirestore().collection("users").document(uid)
                                            .update("future_Session", FieldValue.arrayUnion(documentReference.getId()))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(),
                                                        "Availability slot created successfully", Toast.LENGTH_SHORT)
                                                        .show();
                                            }).addOnFailureListener(e -> {
                                                e.printStackTrace();
                                                Toast.makeText(requireContext(), "Failed to update tutor record",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                }).addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    Toast.makeText(requireContext(), "Failed to create availability slot",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                }).addOnFailureListener(e -> {
                    Log.d("fragment_tutor", "showCreateAvailabilityDialog: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Error getting user profile", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Invalid date or time format", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Adapter class
    public static class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
        private final OnItemClickListener listener;
        private List<Session> sessions;

        public SessionAdapter(List<Session> sessions, OnItemClickListener listener) {
            this.sessions = sessions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
            return new SessionViewHolder(view);
        }

        public void updateData(List<Session> newSessions) {
            this.sessions = newSessions;
            notifyDataSetChanged();
        }

        @Override
        public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
            holder.bind(sessions.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        public interface OnItemClickListener {
            void onItemClick(Session session);

            void onStudentListClick(Session session);

            void onEditClick(Session session);
        }

        public static class SessionViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvTime, tvLocation;
            Button btnStudentList, btnEdit;

            public SessionViewHolder(@NonNull View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.session_date);
                tvTime = itemView.findViewById(R.id.session_time);
                tvLocation = itemView.findViewById(R.id.session_location);
                btnStudentList = itemView.findViewById(R.id.student_list_button);
                btnEdit = itemView.findViewById(R.id.edit_button);
            }

            public void bind(Session session, OnItemClickListener listener) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String dateStr = dateFormat.format(session.getStartTime().toDate());
                tvDate.setText(dateStr);

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timeRange = timeFormat.format(session.getStartTime().toDate()) + " - "
                        + timeFormat.format(session.getEndTime().toDate());
                tvTime.setText(timeRange);

                tvLocation.setText(session.getLocation());

                itemView.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onItemClick(session);
                });

                btnStudentList.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onStudentListClick(session);
                });

                btnEdit.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onEditClick(session);
                });
            }
        }
    }
}
