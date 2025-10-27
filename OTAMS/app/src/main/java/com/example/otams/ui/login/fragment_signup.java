package com.example.otams.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otams.R;
import com.example.otams.data.RegisterDataSource;
import com.example.otams.data.Result;
import com.example.otams.data.Student;
import com.example.otams.data.Tutor;
import com.example.otams.data.User;
import com.example.otams.databinding.FragmentSignupBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

public class fragment_signup extends Fragment {
    private FragmentSignupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ProgressBar loadingProgressBar = binding.loading;

        loadingProgressBar.setVisibility(View.GONE);

        MaterialToolbar toolbar = binding.toolbar;
        toolbar.setTitle("Register"); // Set your title

        // Tell Android this toolbar should act as the ActionBar
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        activity.setSupportActionBar(toolbar);

        // Enable back (Up) button
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Handle back navigation manually
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        RadioGroup roleGroup = binding.roleGroup;
        LinearLayout studentFields = binding.studentFields;
        LinearLayout tutorFields = binding.tutorFields;

        // Default state: Student selected
        studentFields.setVisibility(View.VISIBLE);
        tutorFields.setVisibility(View.GONE);

        // Listen for role selection changes
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_student) {
                studentFields.setVisibility(View.VISIBLE);
                tutorFields.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_tutor) {
                studentFields.setVisibility(View.GONE);
                tutorFields.setVisibility(View.VISIBLE);
            }
        });

        EditText signupUsername = view.findViewById(R.id.email);
        EditText signupPassword = view.findViewById(R.id.password);

        Bundle args = getArguments();
        if (args != null) {
            String username = args.getString("username");
            String password = args.getString("password");

            if (username != null)
                signupUsername.setText(username);
            if (password != null)
                signupPassword.setText(password);
        }

        // Co-pilot suggested this
        EditText firstName = view.findViewById(R.id.first_name);
        EditText lastName = view.findViewById(R.id.last_name);
        EditText phone = view.findViewById(R.id.phone);
        EditText program = view.findViewById(R.id.program);
        EditText coursesEnrolled = view.findViewById(R.id.courses_enrolled);
        EditText coursesOffered = view.findViewById(R.id.courses_offered);
        EditText highestDegree = view.findViewById(R.id.highest_degree);

        Button registerButton = view.findViewById(R.id.register_button);

        String first = firstName.getText().toString().trim();
        String last = lastName.getText().toString().trim();
        String phoneStr = phone.getText().toString().trim();
        String programStr = program.getText().toString().trim();
        String enrolledStr = coursesEnrolled.getText().toString().trim();
        String offeredStr = coursesOffered.getText().toString().trim();
        String degreeStr = highestDegree.getText().toString().trim();
        String emailStr = signupUsername.getText().toString().trim();
        String passwordStr = signupPassword.getText().toString().trim();

        // Validation trigger
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean isStudent = roleGroup.getCheckedRadioButtonId() == R.id.radio_student;

                String first = firstName.getText().toString().trim();
                String last = lastName.getText().toString().trim();
                String phoneStr = phone.getText().toString().trim();
                String programStr = program.getText().toString().trim();
                String enrolledStr = coursesEnrolled.getText().toString().trim();
                String offeredStr = coursesOffered.getText().toString().trim();
                String degreeStr = highestDegree.getText().toString().trim();
                String emailStr = signupUsername.getText().toString().trim();
                String passwordStr = signupPassword.getText().toString().trim();

                boolean commonValid = !first.isEmpty()
                        && !last.isEmpty()
                        && !emailStr.isEmpty()
                        && !passwordStr.isEmpty()
                        && !phoneStr.isEmpty();

                boolean studentValid = isStudent
                        && !programStr.isEmpty()
                        && !enrolledStr.isEmpty();

                boolean tutorValid = !isStudent
                        && !offeredStr.isEmpty()
                        && !degreeStr.isEmpty();

                registerButton.setEnabled(commonValid && (studentValid || tutorValid));
            }
        };

        // Attach watcher to all fields
        firstName.addTextChangedListener(watcher);
        lastName.addTextChangedListener(watcher);
        signupUsername.addTextChangedListener(watcher);
        signupPassword.addTextChangedListener(watcher);
        phone.addTextChangedListener(watcher);
        program.addTextChangedListener(watcher);
        coursesEnrolled.addTextChangedListener(watcher);
        coursesOffered.addTextChangedListener(watcher);
        highestDegree.addTextChangedListener(watcher);

        // Also re-validate on role change
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            studentFields.setVisibility(checkedId == R.id.radio_student ? View.VISIBLE : View.GONE);
            tutorFields.setVisibility(checkedId == R.id.radio_tutor ? View.VISIBLE : View.GONE);
            watcher.afterTextChanged(null); // trigger re-validation
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                loadingProgressBar.setVisibility(View.VISIBLE);

                // Extract fresh input values
                String first = firstName.getText().toString().trim();
                String last = lastName.getText().toString().trim();
                String phoneStr = phone.getText().toString().trim();
                String programStr = program.getText().toString().trim();
                String enrolledStr = coursesEnrolled.getText().toString().trim();
                String offeredStr = coursesOffered.getText().toString().trim();
                String degreeStr = highestDegree.getText().toString().trim();
                String emailStr = signupUsername.getText().toString().trim();
                String passwordStr = signupPassword.getText().toString().trim();

                User userProfile;
                if (roleGroup.getCheckedRadioButtonId() == R.id.radio_student) {
                    userProfile = new Student(emailStr, passwordStr, first, last, phoneStr, programStr);
                } else {
                    userProfile = new Tutor(emailStr, passwordStr, first, last, phoneStr, offeredStr, degreeStr);
                }

                // Call RegisterDataSource
                RegisterDataSource registerDataSource = new RegisterDataSource();
                registerDataSource.register(emailStr, passwordStr, userProfile)
                        .thenAccept(result -> {
                            loadingProgressBar.setVisibility(View.GONE);
                            if (result instanceof Result.Success) {
                                Toast.makeText(getContext(), "User registered successfully!", Toast.LENGTH_SHORT)
                                        .show();
                                NavHostFragment.findNavController(fragment_signup.this).navigateUp();
                            } else if (result instanceof Result.Error) {
                                Exception e = ((Result.Error) result).getError();
                                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

    }
}
