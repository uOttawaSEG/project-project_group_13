package com.example.otams.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.otams.data.FirebaseManager;
import com.google.firebase.auth.FirebaseAuth;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.otams.R;
import com.example.otams.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class fragment_login extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        // Sign out any existing user when returning to login screen
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        requireActivity().setTitle("LOGIN");

        firebaseManager = FirebaseManager.getInstance();
        firebaseManager.signOut();

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        loadingProgressBar.setVisibility(View.GONE);
        usernameEditText.setText("");
        passwordEditText.setText("");


        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();

                Bundle bundle = new Bundle();
                bundle.putString("username", username);

                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_loginFragment_to_fragment_signup, bundle);
            }
        });

        //login button pressed
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
                    NavController navController = Navigation.findNavController(view);
                    navController.navigate(R.id.action_loginFragment_to_fragment_homepage);
                } else {
                    Toast.makeText(getContext(), "Login failed: " +
                            Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}