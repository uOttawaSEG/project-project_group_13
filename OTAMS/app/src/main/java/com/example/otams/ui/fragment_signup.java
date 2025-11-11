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
        binding.registerButton.setOnClickListener(v -> handleRegistration());

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
    }

    // --- FORM VALIDATION ---
    private void validateForm() {
        if (binding == null) return;

        int selectedRole = binding.roleGroup.getCheckedRadioButtonId();
        boolean roleSelected = selectedRole != -1;
        boolean isStudent = selectedRole == R.id.radio_student;

        String first = binding.firstName.getText().toString().trim();
        String last = binding.lastName.getText().toString().trim();
        String phone = binding.phone.getText().toString().trim();
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        boolean isValid = true;

        // First Name
        if (first.isEmpty()) {
            binding.firstName.setError("First name required");
            isValid = false;
        } else {
            binding.firstName.setError(null);
        }

        // Last Name
        if (last.isEmpty()) {
            binding.lastName.setError("Last name required");
            isValid = false;
        } else {
            binding.lastName.setError(null);
        }

        // Phone
        if (phone.isEmpty()) {
            binding.phone.setError("Phone required");
            isValid = false;
        } else {
            binding.phone.setError(null);
        }

        // Email validation
        if (email.isEmpty()) {
            binding.email.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.setError("Enter a valid email address");
            isValid = false;
        } else {
            binding.email.setError(null);
        }

        // Password validation
        if (password.isEmpty()) {
            binding.password.setError("Password is required");
            isValid = false;
        } else if (password.length() < 8) {
            binding.password.setError("Password must be at least 8 characters");
            isValid = false;
        } else {
            binding.password.setError(null);
        }

        // Role-specific fields
        boolean studentValid = false;
        boolean tutorValid = false;

        if (isStudent) {
            studentValid = !binding.program.getText().toString().trim().isEmpty();
            if (!studentValid) binding.program.setError("Program required");
            else binding.program.setError(null);
        } else if (roleSelected) {
            tutorValid = !binding.coursesOffered.getText().toString().trim().isEmpty() && !binding.highestDegree.getText().toString().trim().isEmpty();

            if (binding.coursesOffered.getText().toString().trim().isEmpty()) binding.coursesOffered.setError("Courses required");
            else binding.coursesOffered.setError(null);

            if (binding.highestDegree.getText().toString().trim().isEmpty()) binding.highestDegree.setError("Degree required");
            else binding.highestDegree.setError(null);
        }

        // Enable button only if valid
        binding.registerButton.setEnabled(isValid && roleSelected && (studentValid || tutorValid));
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
}
