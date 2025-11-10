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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.data.Student;
import com.example.otams.data.Tutor;
import com.example.otams.data.User;
import com.example.otams.data.UserStatus;
import com.example.otams.databinding.FragmentSignupBinding;

import java.util.Objects;

public class fragment_signup extends Fragment {

    private FragmentSignupBinding binding;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        requireActivity().setTitle("Register");
        firebaseManager = FirebaseManager.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.loading.setVisibility(View.GONE);

        // Default role: Student
        binding.studentFields.setVisibility(View.VISIBLE);
        binding.tutorFields.setVisibility(View.GONE);

        // Handle role switch visibility
        binding.roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_student) {
                binding.studentFields.setVisibility(View.VISIBLE);
                binding.tutorFields.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_tutor) {
                binding.studentFields.setVisibility(View.GONE);
                binding.tutorFields.setVisibility(View.VISIBLE);
            }
            validateForm(); // recheck button state
        });

        // Pre-fill fields from arguments (optional)
        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString("username");
            String password = args.getString("password");
            if (username != null) binding.email.setText(username);
            if (password != null) binding.password.setText(password);
        }

        // Attach a single TextWatcher to all fields
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateForm();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };

        binding.firstName.addTextChangedListener(watcher);
        binding.lastName.addTextChangedListener(watcher);
        binding.phone.addTextChangedListener(watcher);
        binding.program.addTextChangedListener(watcher);
        binding.coursesOffered.addTextChangedListener(watcher);
        binding.highestDegree.addTextChangedListener(watcher);
        binding.email.addTextChangedListener(watcher);
        binding.password.addTextChangedListener(watcher);

        binding.registerButton.setOnClickListener(v -> handleRegistration());
    }

    // --- FORM VALIDATION ---
    private void validateForm() {
        boolean isStudent = binding.roleGroup.getCheckedRadioButtonId() == R.id.radio_student;

        String first = binding.firstName.getText().toString().trim();
        String last = binding.lastName.getText().toString().trim();
        String phone = binding.phone.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        boolean commonValid = !first.isEmpty() && !last.isEmpty() && !email.isEmpty() && !password.isEmpty() && !phone.isEmpty();

        boolean studentValid = isStudent && !binding.program.getText().toString().trim().isEmpty();
        boolean tutorValid = !isStudent && !binding.coursesOffered.getText().toString().trim().isEmpty() && !binding.highestDegree.getText().toString().trim().isEmpty();

        binding.registerButton.setEnabled(commonValid && (studentValid || tutorValid));
    }

    // --- REGISTRATION LOGIC ---
    private void handleRegistration() {
        binding.loading.setVisibility(View.VISIBLE);

        String first = binding.firstName.getText().toString().trim();
        String last = binding.lastName.getText().toString().trim();
        String phone = binding.phone.getText().toString().trim();
        String program = binding.program.getText().toString().trim();
        String offered = binding.coursesOffered.getText().toString().trim();
        String degree = binding.highestDegree.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        boolean isStudent = binding.roleGroup.getCheckedRadioButtonId() == R.id.radio_student;

        User userProfile;
        if (isStudent) {
            userProfile = new Student(email, password, first, last, phone, program);
        } else {
            userProfile = new Tutor(email, password, first, last, phone, degree, offered);
        }

        userProfile.setStatus(UserStatus.PENDING);

        firebaseManager.signUp(email, password, task -> {
            if (task.isSuccessful()) {
                String uid = Objects.requireNonNull(task.getResult().getUser()).getUid();
                firebaseManager.saveUserProfile(uid, userProfile, aVoid -> {
                    binding.loading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Registration successful. Wait for approval.", Toast.LENGTH_LONG).show();
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.loginFragment);
                }, e -> {
                    binding.loading.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to save user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } else {
                binding.loading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // avoid memory leaks
    }

}
