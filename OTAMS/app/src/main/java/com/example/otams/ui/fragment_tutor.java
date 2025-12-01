package com.example.otams.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.example.otams.data.Tutor;
import com.example.otams.databinding.FragmentTutorBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

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
        Log.d("fragment_tutor", "onCreateView: " + firebaseManager);

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

    private void loadSessions() {
        past_sessions.clear();
        future_sessions.clear();

        Log.d("fragment_tutor", "Starting to load sessions");
        Log.d("fragment_tutor", "Current user: " + (firebaseManager.getCurrentUser() != null ?
                firebaseManager.getCurrentUser().getUid() : "null"));

        // Use atomic integer to track completion of both async operations
        final AtomicInteger completedTasks = new AtomicInteger(0);
        final int totalTasks = 2;

        firebaseManager.fetchPastSessions(new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> sessions) {
                past_sessions.clear();
                past_sessions.addAll(sessions);
                Log.d("fragment_tutor", "Past sessions loaded: " + past_sessions.size());

                int completed = completedTasks.incrementAndGet();
                if (completed == totalTasks) {
                    updateAdapterAfterLoad();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("fragment_tutor", "Failed to load past sessions", e);
                Toast.makeText(requireContext(), "Failed to load past sessions", Toast.LENGTH_SHORT).show();

                int completed = completedTasks.incrementAndGet();
                if (completed == totalTasks) {
                    updateAdapterAfterLoad();
                }
            }
        });

        firebaseManager.fetchFutureSessions(new FirebaseManager.SessionFetchCallback() {
            @Override
            public void onSessionsFetched(List<Session> sessions) {
                future_sessions.clear();
                future_sessions.addAll(sessions);
                Log.d("fragment_tutor", "Future sessions loaded: " + future_sessions.size());

                int completed = completedTasks.incrementAndGet();
                if (completed == totalTasks) {
                    updateAdapterAfterLoad();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("fragment_tutor", "Failed to load future sessions", e);
                Toast.makeText(requireContext(), "Failed to load future sessions", Toast.LENGTH_SHORT).show();

                int completed = completedTasks.incrementAndGet();
                if (completed == totalTasks) {
                    updateAdapterAfterLoad();
                }
            }
        });
    }

    private void updateAdapterAfterLoad() {
        if (!isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            int selectedId = binding.sessionLabel.getCheckedRadioButtonId();
            if (selectedId == R.id.radio_past) {
                adapter.updateData(past_sessions);
                Log.d("fragment_tutor", "Updated adapter with past sessions: " + past_sessions.size());
            } else if (selectedId == R.id.radio_future) {
                adapter.updateData(future_sessions);
                Log.d("fragment_tutor", "Updated adapter with future sessions: " + future_sessions.size());
            }
        });
    }

    private void setupRadioGroupListener() {
        binding.sessionLabel.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_past) {
                adapter.updateData(past_sessions);
            } else if (checkedId == R.id.radio_future) {
                adapter.updateData(future_sessions);
            }
        });
    }

    private void setupCreateAvailabilityButton() {
        binding.createAvailabilityButton.setOnClickListener(v -> showCreateAvailabilityDialog());
    }

    private void setupRecyclerView() {
        adapter = new SessionAdapter(new ArrayList<>(), new SessionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Session session) {
                onSessionClicked(session);
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

    private void showHalfHourPicker(EditText targetInput) {
        // Hours 0–23
        String[] hours = new String[24];
        for (int i = 0; i < 24; i++) {
            hours[i] = String.format("%02d", i);
        }

        // Minutes restricted to 00 and 30
        String[] minutes = {"00", "30"};

        // Build spinners
        Spinner hourSpinner = new Spinner(requireContext());
        Spinner minuteSpinner = new Spinner(requireContext());

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, hours);
        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, minutes);

        hourSpinner.setAdapter(hourAdapter);
        minuteSpinner.setAdapter(minuteAdapter);

        // Layout to hold both spinners side by side
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(hourSpinner);
        layout.addView(minuteSpinner);

        // Show dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Time")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    String formattedTime = hourSpinner.getSelectedItem() + ":" +
                            minuteSpinner.getSelectedItem();
                    targetInput.setText(formattedTime);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onSessionClicked(Session session) {
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putParcelable("session", session);
        navController.navigate(R.id.action_fragment_tutor_to_fragment_tutor_expanded, bundle);
    }

    private void showStudentListDialog(Session session) {
        ArrayList<String> studentIds = session.getEnrolledStudents();

        if (studentIds == null || studentIds.isEmpty()) {
            Toast.makeText(requireContext(), "No students enrolled.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseManager.getFirestore().collection("student").whereIn(FieldPath.documentId(), studentIds).get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> studentNames = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String firstName = doc.getString("first_Name");
                        String lastName = doc.getString("last_Name");
                        String email = doc.getString("email");

                        // Combine nicely even if one part is missing
                        String displayName;
                        if (firstName != null && lastName != null) {
                            displayName = firstName + " " + lastName;
                        } else displayName = Objects.requireNonNullElseGet(firstName, () -> email != null ? email : "Unknown Student");

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
                        String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1,
                                selectedDay);
                        dateInput.setText(formattedDate);
                    }, year, month, day);

            datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
            datePickerDialog.show();
        });

        startTimeInput.setOnClickListener(v -> showHalfHourPicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showHalfHourPicker(endTimeInput));

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

                // Update Firestore document using session ID
                firebaseManager.getFirestore().collection("sessions").document(session.getSessionId())
                        .set(session).addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Availability slot updated successfully",
                                    Toast.LENGTH_SHORT).show();
                            // Refresh the sessions list
                            loadSessions();
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

        startTimeInput.setOnClickListener(v -> showHalfHourPicker(startTimeInput));
        endTimeInput.setOnClickListener(v -> showHalfHourPicker(endTimeInput));

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
                                    documentReference.update("sessionId", documentReference.getId())
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(requireContext(),
                                                                "Availability slot created successfully", Toast.LENGTH_SHORT)
                                                        .show();
                                                // Refresh the sessions list
                                                loadSessions();
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