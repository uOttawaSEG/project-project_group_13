package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.appbar.MaterialToolbar;

public class fragment_signup extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
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
        toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());


        RadioGroup roleGroup = view.findViewById(R.id.role_group);
        LinearLayout studentFields = view.findViewById(R.id.student_fields);
        LinearLayout tutorFields = view.findViewById(R.id.tutor_fields);

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

            if (username != null) signupUsername.setText(username);
            if (password != null) signupPassword.setText(password);
        }


    }
}
