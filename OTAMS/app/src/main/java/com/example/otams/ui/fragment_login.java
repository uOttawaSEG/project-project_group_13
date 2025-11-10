package com.example.otams.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.data.FirebaseManager;
import com.example.otams.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class fragment_login extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        firebaseManager = FirebaseManager.getInstance();

        firebaseManager.signOut();

        requireActivity().setTitle("LOGIN");

        // Clear any previous text
        binding.username.setText("");
        binding.password.setText("");

        return binding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();

        // Clear text fields every time this fragment becomes visible
        binding.username.setText("");
        binding.password.setText("");

        firebaseManager.signOut();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loadingProgressBar.setVisibility(View.GONE);


        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("username", username);

                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_loginFragment_to_fragment_signup, bundle);
            }
        });

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

        binding.username.addTextChangedListener(watcher);
        binding.password.addTextChangedListener(watcher);

        // login button pressed
        try {
            loginButton.setOnClickListener(v -> {
                String email = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadingProgressBar.setVisibility(View.VISIBLE);

                firebaseManager.signIn(email, password, task -> {
                    loadingProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseManager.getCurrentUser();
                        Toast.makeText(getContext(), "Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        // Navigate to next screen here
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_loginFragment_to_fragment_homepage);
                    } else {
                        Toast.makeText(getContext(), "Login failed: " +
                                Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate", e);
        }
    }

    private void validateForm() {
        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;

        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        boolean isValid = true;

        if (email.isEmpty()) {
            usernameEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            usernameEditText.setError("Enter a valid email address");
            isValid = false;
        } else {
            usernameEditText.setError(null);
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        // Enable or disable the login button based on validation
        loginButton.setEnabled(isValid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}